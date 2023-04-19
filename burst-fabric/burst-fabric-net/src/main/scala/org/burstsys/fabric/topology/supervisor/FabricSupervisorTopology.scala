/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.topology.supervisor

import org.burstsys.fabric.configuration.{burstFabricTopologyHomogeneous, burstFabricTopologyTardyThresholdMs}
import org.burstsys.fabric.container.FabricSupervisorService
import org.burstsys.fabric.container.supervisor.{FabricSupervisorContainer, FabricSupervisorListener}
import org.burstsys.fabric.net.message.AccessParameters
import org.burstsys.fabric.net.message.assess.{FabricNetAssessRespMsg, FabricNetHeartbeatMsg}
import org.burstsys.fabric.net.server.connection.FabricNetServerConnection
import org.burstsys.fabric.topology.model.node.FabricNode
import org.burstsys.fabric.topology.model.node.worker._
import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.git
import org.burstsys.vitals.healthcheck.{VitalsComponentHealth, VitalsHealthMarginal, VitalsHealthUnhealthy}
import org.burstsys.vitals.logging.{burstLocMsg, burstStdMsg}
import org.burstsys.vitals.reporter.instrument.prettyTimeFromNanos
import org.burstsys.vitals.sysinfo.{SystemInfoComponent, SystemInfoService}

import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters._

/**
  * Maintain a ''topology'' of active workers in various states of dynamic ''health'' on the ''supervisor''
  * This involved the following elements:
  * <ol>
  * <li>'''Heartbeat:''' [[org.burstsys.fabric.net.client.FabricNetClient]] (`worker`) regularly sends an
  * async ''heartbeat'' message to [[org.burstsys.fabric.net.server.FabricNetServer]] (supervisor) that indicates
  * its alive.</li>
  * <li>'''Assessing:''' [[org.burstsys.fabric.net.server.FabricNetServer]] (`supervisor`) regularly sends a
  * sync (RPC) ''assess'' request message to [[org.burstsys.fabric.net.client.FabricNetClient]] (`worker`) requesting
  * health or load stats which is returned as an ''assess'' response.
  * This acts as a heartbeat from supervisor to worker that is evaluated only on the supervisor.
  * Both the contents of the assessment ''and'' the speed and reliability of the response are used as status info</li>
  * <li>'''Ranking:''' [[org.burstsys.fabric.net.server.FabricNetServer]] (`supervisor`) dynamically
  * updates its list of active workers and assigns health ranking and
  * moves workers from state to state in response to incoming tethers, and assessments</li>
  * <li>'''Lasso:''' [[org.burstsys.fabric.net.server.FabricNetServer]] (`supervisor`) chooses sets of workers
  * for loads/waves based on locality and rank </li>
  * </ol>
  */
trait FabricSupervisorTopology extends FabricSupervisorService {

  /**
    * return a set of healthy, least busy workers
    */
  def bestWorkers(count: Int): Array[FabricWorkerNode]

  /**
    * returns a set of all workers
    */
  def allWorkers: Array[FabricWorkerNode]

  /**
    * return a set of all healthy workers
    */
  def healthyWorkers: Array[FabricWorkerNode]

  /**
    * get a worker connection given a key
    */
  def getWorker(key: FabricWorkerNode, mustBeConnected: Boolean = true): Option[FabricWorkerProxy]

  /**
    * wire up a listener for topology events
    */
  def talksTo(listeners: FabricTopologyListener*): this.type

}

object FabricSupervisorTopology {

  def apply[T <: FabricSupervisorListener](container: FabricSupervisorContainer[T]): FabricSupervisorTopology =
    FabricSupervisorTopologyContext(container)

}

private final case
class FabricSupervisorTopologyContext[T <: FabricSupervisorListener](container: FabricSupervisorContainer[T])
  extends FabricSupervisorTopology with FabricSupervisorListener with SystemInfoComponent {

  override val serviceName: String = s"fabric-topology-service"

  override def modality: VitalsServiceModality = container.bootModality

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    ensureNotRunning
    log info startingMessage
    container.talksTo(this.asInstanceOf[T])
    SystemInfoService.registerComponent(this)
    this talksTo FabricTopologyReporter
    markRunning
    this
  }

  override
  def stop: this.type = {
    ensureRunning
    SystemInfoService.deregisterComponent(this)
    log info stoppingMessage
    markNotRunning
    this
  }

  override def componentHealth: VitalsComponentHealth = {
    if (!isRunning) {
      VitalsComponentHealth(VitalsHealthUnhealthy, "not running")
    } else if (healthyWorkers.isEmpty) {
      VitalsComponentHealth(VitalsHealthMarginal, "no healthy workers connected")
    } else {
      VitalsComponentHealth(message = s"workers=${_workers.size} healthy_workers=${healthyWorkers.length}")
    }
  }

  burstFabricTopologyHomogeneous.listeners += recheckWorkers

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // state
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  private val _listenerSet = ConcurrentHashMap.newKeySet[FabricTopologyListener]()

  private val _workers = new ConcurrentHashMap[FabricNode, FabricWorkerProxy]()

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  override def getWorker(key: FabricWorkerNode, onlyHealthy: Boolean = true): Option[FabricWorkerProxy] = {
    Option(_workers.get(key)).filter(!onlyHealthy || _.isHealthy)
  }

  override def talksTo(listeners: FabricTopologyListener*): this.type = {
    _listenerSet.addAll(listeners.asJava)
    this
  }

  override def allWorkers: Array[FabricWorkerNode] = {
    log trace burstStdMsg("all workers")
    _workers.values.toArray(Array.empty[FabricWorkerNode])
  }

  override def healthyWorkers: Array[FabricWorkerNode] = {
    _workers.values.stream.filter(_.isHealthy).toArray(new Array[FabricWorkerNode](_))
  }

  override def bestWorkers(count: Int): Array[FabricWorkerNode] = {
    healthyWorkers.take(count)
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // FabricNetServerListener
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  override def onDisconnect(connection: FabricNetServerConnection): Unit = {
    val tag = s"FabricSupervisorTopology.onNetServerDisconnect(${connection.link})"
    _workers.get(connection.clientKey) match {
      case null =>
        log warn s"FAB_TOPO_WORKER_NOT_CONNECTED ${connection.link}"
      case worker =>
        log info s"FAB_TOPO_WORKER_DISCONNECT $worker $tag"
        _workers.remove(worker.forExport)
        _listenerSet.forEach(_.onTopologyWorkerLoss(worker))
        _listenerSet.forEach(_.onTopologyWorkerLost(worker))
    }
    validateConnections("worker-disconnected")
  }

  override def onNetServerTetherMsg(connection: FabricNetServerConnection, msg: FabricNetHeartbeatMsg): Unit = {
    log debug burstLocMsg(s"${connection.link} $msg")
    val fabricWorker = fetchWorker(connection, msg.gitCommit, msg.parameters)
    fabricWorker.accessParameters = msg.parameters
    fabricWorker.lastUpdateTime = System.currentTimeMillis()
    fabricWorker.tetherSkewMs = fabricWorker.lastUpdateTime - msg.tetherSendEpoch
  }

  override def onNetServerAssessRespMsg(connection: FabricNetServerConnection, msg: FabricNetAssessRespMsg): Unit = {
    _workers.get(connection.clientKey) match {
      case null =>
        log warn burstLocMsg(s"FAB_TOPO_WAYWARD_ASSESS ${connection.link} $msg response in ${prettyTimeFromNanos(msg.elapsedNanos)}")

      case worker =>
        worker.lastUpdateTime = System.currentTimeMillis()
        worker.assessLatencyNanos = msg.elapsedNanos
        worker.assessment = msg.assessment
        log debug burstLocMsg(s"${connection.link} $msg response in ${prettyTimeFromNanos(worker.assessLatencyNanos)}")
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // internal
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  private def tardyDeadline: Long = System.currentTimeMillis - burstFabricTopologyTardyThresholdMs.get.toMillis

  private def notifyListeners(notification: FabricTopologyListener => Unit): Unit = {
    _listenerSet forEach { l =>
      try notification(l)
      catch safely {
        case t => log info s"Failed to notify listener $l. $t"
      }
    }
  }

  private def recheckWorkers(newValue: Option[Boolean]): Unit = {
    log debug s"FabricSupervisorTopology.recheckWorkers()"
    val homogeneityRequired = newValue.getOrElse(burstFabricTopologyHomogeneous.get)
    val markTardyBefore = tardyDeadline

    _workers.synchronized {
      _workers.values.asScala.foreach { worker =>
        worker.state match {
          // ignore unrecoverable states
          case FabricWorkerStateDead | FabricWorkerStateUnknown =>

          // check to see if exiled workers can be readmitted
          case FabricWorkerStateExiled =>
            if (worker.commitId == git.commitId || !homogeneityRequired) {
              val state = if (worker.lastUpdateTime > markTardyBefore) FabricWorkerStateTardy else FabricWorkerStateLive
              worker.changeState(FabricWorkerStateExiled, state)
            }

          // check any remaining workers to see if they should be exiled
          case state =>
            if (worker.commitId != git.commitId && homogeneityRequired)
              worker.changeState(state, FabricWorkerStateExiled)
        }
      }
    }  }

  private def fetchWorker(connection: FabricNetServerConnection, gitCommit: String, accessParameters: AccessParameters): FabricWorkerProxy = {
    val tag = s"FabricSupervisorTopology.fetchWorker(${connection.link}, gitCommit=$gitCommit)"
    log debug s"TOPO_FETCH_WORKER $tag"
    val workerKey = connection.clientKey
    val requireHomogeneity = burstFabricTopologyHomogeneous.get
    var workerAdded = false

    val worker = _workers.computeIfAbsent(workerKey, _ => {
      log info s"TOPO_NEW_WORKER $workerKey $tag"
      val newWorker = FabricWorkerProxy(connection, gitCommit, accessParameters)
      if (requireHomogeneity && gitCommit != git.commitId) {
        log info s"FAB_TOPO FETCH_WORKER_EXILED worker=${workerKey.nodeName}"
        newWorker.changeState(newWorker.state, FabricWorkerStateExiled)
      }
      workerAdded = true
      newWorker
    })
    if (workerAdded) {
      notifyListeners(_.onTopologyWorkerGain(worker))
      notifyListeners(_.onTopologyWorkerGained(worker))
      validateConnections("worker-connected")
    }
    worker
  }

  private def validateConnections(trigger: String): Boolean = {
    val active = container.activeConnections
    _workers.synchronized {
      var valid = active.length == _workers.size()
      valid = valid && active.forall(c => _workers.get(c.clientKey) != null)
      if (!valid) {
        val workers = healthyWorkers
        val healthy = workers.map(n => s"${n.nodeId}#${n.nodeAddress}").mkString(",")
        val connections = active.map(c => s"${c.remoteAddress}:${c.remotePort}").mkString(",")
        log debug burstStdMsg(s"topology mismatch trigger=$trigger nodes=${workers.length} conns=${active.length} healthy($healthy) connections($connections)")
      }
      valid
    }
  }

  /**
   * @return name of component
   */
  override def name: String = serviceName

  /**
   * System info about component.
   *
   * @return Case class that will be serialized to Json
   */
  override def status: AnyRef = {
    case class TopologyStatus(healthyWorkers: Array[FabricWorkerNode] = healthyWorkers.map(_.forExport))
    TopologyStatus()
  }
}
