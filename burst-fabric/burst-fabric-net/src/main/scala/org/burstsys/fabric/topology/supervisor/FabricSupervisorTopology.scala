/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.topology.supervisor

import org.burstsys.fabric.configuration.burstFabricTopologyHomogeneous
import org.burstsys.fabric.container.FabricSupervisorService
import org.burstsys.fabric.container.metrics.FabricAssessment
import org.burstsys.fabric.container.supervisor.FabricSupervisorContainer
import org.burstsys.fabric.container.supervisor.FabricSupervisorListener
import org.burstsys.fabric.net.message.assess.FabricNetAssessRespMsg
import org.burstsys.fabric.net.message.assess.FabricNetTetherMsg
import org.burstsys.fabric.net.server.connection.FabricNetServerConnection
import org.burstsys.fabric.topology.model.node.FabricNode
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerProxy
import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.git
import org.burstsys.vitals.healthcheck.VitalsComponentHealth
import org.burstsys.vitals.healthcheck.VitalsHealthMarginal
import org.burstsys.vitals.healthcheck.VitalsHealthUnhealthy
import org.burstsys.vitals.logging.burstLocMsg
import org.burstsys.vitals.logging.burstStdMsg
import org.burstsys.vitals.net.VitalsHostAddress
import org.burstsys.vitals.reporter.instrument.prettyTimeFromNanos
import org.burstsys.vitals.sysinfo.{SystemInfoComponent, SystemInfoService}

import java.lang
import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters._

/**
  * Maintain a ''topology'' of active workers in various states of dynamic ''health'' on the ''supervisor''
  * This involved the following elements:
  * <ol>
  * <li>'''Tethering:''' [[org.burstsys.fabric.net.client.FabricNetClient]] (`worker`) regularly sends an
  * async ''tether'' message to [[org.burstsys.fabric.net.server.FabricNetServer]] (supervisor) that indicates
  * its alive. This acts as a heartbeat from worker to supervisor that is evaluated by both. </li>
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
      VitalsComponentHealth(message = s"healthy_workers=${_healthyWorkers.size} unhealth_workers=${_unhealthyWorkers.size}")
    }
  }

  burstFabricTopologyHomogeneous.listeners += recheckWorkers

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // state
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _listenerSet: ConcurrentHashMap.KeySetView[FabricTopologyListener, lang.Boolean] = ConcurrentHashMap.newKeySet[FabricTopologyListener]()

  private[this]
  val _healthyWorkers: ConcurrentHashMap[FabricNode, FabricWorkerProxy] = new ConcurrentHashMap[FabricNode, FabricWorkerProxy]()

  private[this]
  val _unhealthyWorkers: ConcurrentHashMap[FabricNode, FabricWorkerProxy] = new ConcurrentHashMap[FabricNode, FabricWorkerProxy]()

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  override def getWorker(key: FabricWorkerNode, mustBeConnected: Boolean = true): Option[FabricWorkerProxy] = {
    _healthyWorkers.get(key) match {
      case null =>
        if (!mustBeConnected) {
          _unhealthyWorkers.get(key) match {
            case null =>
              None
            case worker =>
              Some(worker)
          }
        } else
          None
      case workerProxy =>
        if (workerProxy.isConnected || !mustBeConnected)
          Some(workerProxy)
        else
          None
    }

  }

  override def talksTo(listeners: FabricTopologyListener*): this.type = {
    _listenerSet.addAll(listeners.asJava)
    this
  }

  override def allWorkers: Array[FabricWorkerNode] = {
    log trace burstStdMsg("all workers")
    (_healthyWorkers.values().asScala ++ _unhealthyWorkers.values().asScala).toArray
  }

  override def healthyWorkers: Array[FabricWorkerNode] = {
    _healthyWorkers.values.asScala.toArray
  }

  override def bestWorkers(count: Int): Array[FabricWorkerNode] = {
    healthyWorkers.take(count)
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // FabricNetServerListener
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  override def onDisconnect(connection: FabricNetServerConnection): Unit = {
    lazy val tag = s"FabricSupervisorTopology.onNetServerDisconnect(${connection.link})"
    log debug tag
    _healthyWorkers.synchronized {
      _healthyWorkers.get(connection.clientKey) match {
        case null =>
          log warn s"FAB_TOPO_WORKER_NOT_CONNECTED $tag"
        case workerProxy =>
          log info s"FAB_TOPO_WORKER_DISCONNECT (mark unhealthy...) $workerProxy $tag"
          lostWorker(workerProxy)
      }
    }
    validateConnections
  }

  override def onNetServerTetherMsg(connection: FabricNetServerConnection, msg: FabricNetTetherMsg): Unit = {
    log debug burstLocMsg(s"${connection.link} $msg")
    val fabricWorker = fetchWorker(connection, msg.gitCommit, None)
    fabricWorker.lastUpdateTime = System.currentTimeMillis()
    fabricWorker.tetherSkewMs = fabricWorker.lastUpdateTime - msg.tetherSendEpoch
  }

  override def onNetServerAssessRespMsg(connection: FabricNetServerConnection, msg: FabricNetAssessRespMsg): Unit = {
    val fabricWorker = fetchWorker(connection, msg.gitCommit, Some(msg.assessment))
    fabricWorker.lastUpdateTime = System.currentTimeMillis()
    fabricWorker.assessLatencyNanos = msg.elapsedNanos
    val isFirstWorkerAssessment: Boolean = (fabricWorker.assessment == null) && (msg.assessment != null)
    fabricWorker.assessment = msg.assessment
    if (isFirstWorkerAssessment)
      updateAddWorker(fabricWorker)

    log debug burstLocMsg(s"${connection.link} $msg response in ${prettyTimeFromNanos(fabricWorker.assessLatencyNanos)}")
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // internal
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  private def addWorker(worker: FabricWorkerProxy): Unit = {
    lazy val tag = s"FabricSupervisorTopology.addWorker(${worker.link})"
    log debug tag
    val key = worker.forExport
    if (_unhealthyWorkers.contains(key)) {
      _unhealthyWorkers.remove(key)
    }
    _healthyWorkers.put(key, worker)
    validateConnections
  }

  private def updateAddWorker(worker: FabricWorkerProxy): Unit = {
    _listenerSet.forEach(_.onTopologyWorkerGain(worker))
    _listenerSet.forEach(_.onTopologyWorkerGained(worker))
  }

  private def lostWorker(worker: FabricWorkerProxy): Unit = {
    lazy val tag = s"FabricSupervisorTopology.lostWorker(${worker.link})"
    log debug tag
    val key = worker.forExport
    _healthyWorkers.remove(key)
    _unhealthyWorkers.put(key, worker)
    _listenerSet.forEach(_.onTopologyWorkerLoss(worker))
    _listenerSet.forEach(_.onTopologyWorkerLost(worker))
  }

  private def recheckWorkers(newValue: Option[Boolean]): Unit = {
    lazy val tag = s"FabricSupervisorTopology.recheckWorkers()"
    log debug tag
    val commitId = git.commitId
    val homogeneityRequired = newValue.getOrElse(burstFabricTopologyHomogeneous.get)

    _healthyWorkers.synchronized {
      val workers = allWorkers
      workers.foreach { worker =>
        _healthyWorkers.get(worker) match {
          case null =>
            _unhealthyWorkers.get(worker) match {
              case proxy =>
                if (!homogeneityRequired && proxy.isConnected)
                  addWorker(proxy)

              case null =>
                log warn s"TOPO_WORKER_BAD_STATE worker=$worker (neither healthy nor unhealthy) $tag"
            }
          case proxy =>
            if (homogeneityRequired && proxy.commitId != commitId)
              lostWorker(proxy)
        }
      }
    }
  }

  private def fetchWorker(connection: FabricNetServerConnection, gitCommit: String, assessment: Option[FabricAssessment]): FabricWorkerProxy = {
    lazy val tag = s"FabricSupervisorTopology.fetchWorker(${connection.link}, gitCommit=$gitCommit)"
    log debug s"TOPO_FETCH_WORKER $tag"
    val workerKey = connection.clientKey
    val requireHomogeneity = burstFabricTopologyHomogeneous.get

    _healthyWorkers.synchronized {
      _healthyWorkers.get(workerKey) match {
        case null =>
          _unhealthyWorkers.remove(workerKey) match {
            case null =>
              log info s"TOPO_NEW_WORKER $workerKey $tag"
            case worker =>
              if (!requireHomogeneity || worker.commitId == git.commitId)
                log info s"TOPO_RECONNECT_WORKER $workerKey $tag"
          }
          val worker = FabricWorkerProxy(connection, gitCommit)
          if (assessment.isDefined)
            worker.assessment = assessment.get
          if (!requireHomogeneity || gitCommit == git.commitId) {
            addWorker(worker)
          } else {
            // not lost worker here because it wasn't in _healthyWorkers
            _unhealthyWorkers.put(workerKey, worker)
          }
          worker
        case fetched =>
          fetched
      }
    }
  }

  def validateConnections: Boolean = {
    val active = container.activeConnections
    _healthyWorkers.synchronized {
      var valid = active.length == _healthyWorkers.size()
      valid = valid && active.forall(c => _healthyWorkers.get(c.clientKey) != null)
      if (!valid) {
        log debug burstStdMsg(s"toplogy mismatch health(${healthyWorkers.mkString(",")}) connections(${active.mkString(",")})")
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
   * @return Case classs that will be serialized to Json
   */
  override def status: AnyRef = {
    case class TopologyStatus(healthyWorkers: Array[FabricWorkerNode] = healthyWorkers.map(_.forExport))
    TopologyStatus()
  }
}