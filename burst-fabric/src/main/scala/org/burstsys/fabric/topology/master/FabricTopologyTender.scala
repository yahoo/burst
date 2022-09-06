/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.topology.master

import org.burstsys.fabric.configuration._
import org.burstsys.fabric.net.message.assess.{FabricNetAssessRespMsg, FabricNetTetherMsg}
import org.burstsys.fabric.net.server.FabricNetServerListener
import org.burstsys.fabric.net.server.connection.FabricNetServerConnection
import org.burstsys.fabric.topology.model.node.FabricNode
import org.burstsys.fabric.topology.model.node.worker.{FabricWorkerNode, FabricWorkerProxy}
import org.burstsys.vitals.git
import org.burstsys.vitals.instrument._
import org.burstsys.vitals.logging._

import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters._

/**
 * This is where workers are collected, ranked, organized, and distributed to interested parties
 * for scans
 */
trait FabricTopologyTender extends AnyRef with FabricNetServerListener {

  self: FabricMasterTopology =>

  burstFabricTopologyHomogeneous.listeners += recheckWorkers

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // state
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _listenerSet = ConcurrentHashMap.newKeySet[FabricTopologyListener].asScala

  private[this]
  val _healthyWorkers = new ConcurrentHashMap[FabricNode, FabricWorkerProxy].asScala

  private[this]
  val _unhealthyWorkers = new ConcurrentHashMap[FabricNode, FabricWorkerProxy].asScala

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def getWorker(key: FabricWorkerNode, mustBeConnected: Boolean = true): Option[FabricWorkerProxy] = {
    _healthyWorkers.get(key) match {
      case Some(workerProxy) =>
        if (workerProxy.connection.isConnected || !mustBeConnected) Some(workerProxy) else None
      case None =>
        if (!mustBeConnected) _unhealthyWorkers.get(key) else None
    }
  }

  final override
  def talksTo(listeners: FabricTopologyListener*): this.type = {
    _listenerSet ++= listeners
    this
  }

  final override
  def allWorkers: Array[FabricWorkerNode] = {
    log trace burstStdMsg("all workers")
    (_healthyWorkers.values ++ _unhealthyWorkers.values).toArray
  }

  final override
  def healthyWorkers: Array[FabricWorkerNode] = {
    _healthyWorkers.values.toArray

  }

  final override
  def bestWorkers(count: Int): Array[FabricWorkerNode] = {
    healthyWorkers.take(count)
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // FabricNetServerListener
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def onNetServerDisconnect(connection: FabricNetServerConnection): Unit = {
    lazy val tag = s"FabricTopologyTender.onNetServerDisconnect(${connection.link})"
    if (debugTopology)
      log info tag
    _healthyWorkers.synchronized {
      _healthyWorkers.get(connection.clientKey) match {
        case None => log warn s"FAB_TOPO_WORKER_NOT_CONNECTED $tag"
        case Some(workerProxy) =>
          log info s"FAB_TOPO_WORKER_DISCONNECT (mark unhealthy...) $workerProxy $tag"
          lostWorker(workerProxy)
      }
    }
  }

  final override
  def onNetServerTetherMsg(connection: FabricNetServerConnection, msg: FabricNetTetherMsg): Unit = {
    lazy val tag = s"FabricTopologyTender.onNetServerTetherMsg(${connection.link} $msg)"
    if (debugTopology)
      log info tag
    val fabricWorker = fetchWorker(connection, msg.gitCommit)
    fabricWorker.lastUpdateTime = System.currentTimeMillis()
    fabricWorker.tetherSkewMs = fabricWorker.lastUpdateTime - msg.tetherSendEpoch
  }

  final override
  def onNetServerAssessRespMsg(connection: FabricNetServerConnection, msg: FabricNetAssessRespMsg): Unit = {
    lazy val tag = s"FabricTopologyTender.onNetServerAssessRespMsg(${connection.link} $msg)"
    val fabricWorker = fetchWorker(connection, msg.gitCommit)
    fabricWorker.lastUpdateTime = System.currentTimeMillis()
    fabricWorker.assessLatencyNanos = msg.elapsedNanos
    fabricWorker.assessment = msg.assessment
    if (debugTopology)
      log debug s"$tag response in ${prettyTimeFromNanos(fabricWorker.assessLatencyNanos)}"
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // internal
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  private def addWorker(worker: FabricWorkerProxy): Unit = {
    lazy val tag = s"FabricTopologyTender.addWorker(${worker.connection.link})"
    if (debugTopology)
      log info tag
    val key = worker.forExport
    if (_unhealthyWorkers.contains(key)) {
      _unhealthyWorkers -= key
    }
    _healthyWorkers += key -> worker
    _listenerSet.foreach(_.onTopologyWorkerGain(key))
    _listenerSet.foreach(_.onTopologyWorkerGained(key))
  }

  private def lostWorker(worker: FabricWorkerProxy): Unit = {
    lazy val tag = s"FabricTopologyTender.lostWorker(${worker.connection.link})"
    if (debugTopology)
      log info tag
    val key = worker.forExport
    _healthyWorkers.remove(key)
    _unhealthyWorkers += key -> worker
    _listenerSet.foreach(_.onTopologyWorkerLoss(key))
    _listenerSet.foreach(_.onTopologyWorkerLost(key))
  }

  private def recheckWorkers(newValue: Option[Boolean]): Unit = {
    lazy val tag = s"FabricTopologyTender.recheckWorkers()"
    if (debugTopology)
      log info tag
    val commitId = git.commitId
    val homogeneityRequired = newValue.getOrElse(burstFabricTopologyHomogeneous.getOrThrow)

    _healthyWorkers.synchronized {
      val workers = allWorkers
      workers.foreach { worker =>
        _healthyWorkers.get(worker) match {
          case Some(proxy) =>
            if (homogeneityRequired && proxy.commitId != commitId)
              lostWorker(proxy)

          case None =>
            _unhealthyWorkers.get(worker) match {
              case Some(proxy) =>
                if (!homogeneityRequired && proxy.connection.isConnected)
                  addWorker(proxy)

              case None =>
                log warn s"TOPO_WORKER_BAD_STATE worker=$worker (neither healthy nor unhealthy) $tag"
            }
        }
      }
    }
  }

  private def fetchWorker(connection: FabricNetServerConnection, gitCommit: String): FabricWorkerProxy = {
    lazy val tag = s"FabricTopologyTender.fetchWorker(${connection.link}, gitCommit=$gitCommit)"
    if (debugTopology)
      log info s"TOPO_FETCH_WORKER $tag"
    val workerKey = connection.clientKey
    val requireHomogeneity = burstFabricTopologyHomogeneous.getOrThrow

    _healthyWorkers.synchronized {
      _healthyWorkers.get(workerKey) match {
        case Some(fetched) =>
          fetched

        case None =>
          _unhealthyWorkers.remove(workerKey) match {
            case None =>
              log info s"TOPO_NEW_WORKER $workerKey $tag"
            case Some(worker) =>
              if (!requireHomogeneity || worker.commitId == git.commitId)
                log info s"TOPO_RECONNECT_WORKER $workerKey $tag"
          }
          val worker = FabricWorkerProxy(connection, gitCommit)
          if (!requireHomogeneity || gitCommit == git.commitId) {
            addWorker(worker)
          } else {
            // not lost worker here because it wasn't in _healthyWorkers
            _unhealthyWorkers += workerKey -> worker
          }
          worker
      }
    }
  }

}
