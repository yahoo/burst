/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.worker

import org.burstsys.{brio, fabric, vitals}
import org.burstsys.fabric.configuration
import org.burstsys.fabric.container.model.{FabricContainer, FabricContainerContext}
import org.burstsys.fabric.data.worker.FabricWorkerData
import org.burstsys.fabric.data.worker.store._
import org.burstsys.fabric.execution.worker.FabricWorkerEngine
import org.burstsys.fabric.metadata.worker.FabricWorkerMetadata
import org.burstsys.fabric.net.client.FabricNetClient
import org.burstsys.fabric.topology.model.node.worker.FabricWorker
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsStandaloneServer, VitalsStandardServer}
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net.{getPublicHostAddress, getPublicHostName}

import scala.util.{Failure, Success}

/**
 * the one per JVM top level container for a Fabric Worker
 */
trait FabricWorkerContainer extends FabricContainer {

  /**
   * data management on the worker side
   *
   * @return
   */
  def data: FabricWorkerData

  /**
   * the worker execution engine
   *
   * @return
   */
  def engine: FabricWorkerEngine

  /**
   * the master metadata service
   *
   * @return
   */
  def metadata: FabricWorkerMetadata

  /**
   * the worker network client
   *
   * @return
   */
  def netClient: FabricNetClient

  /**
   * The local worker metadata instance
   *
   * @return
   */
  def worker: FabricWorker

  /**
   * wire up the an event handler for this container
   *
   * @return
   */
  def talksTo(listener: FabricWorkerListener): this.type

}

abstract class
FabricWorkerContainerContext extends FabricContainerContext with FabricWorkerContainer {

  override def serviceName: String = s"fabric-worker-container"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////

  final lazy
  val bootModality: VitalsServiceModality = if (configuration.burstFabricWorkerStandaloneProperty.getOrThrow)
    VitalsStandaloneServer else VitalsStandardServer

  private[this]
  val _netClient: FabricNetClient = FabricNetClient(this)

  private[this]
  val _data: FabricWorkerData = FabricWorkerData(this)

  private[this]
  val _engine: FabricWorkerEngine = FabricWorkerEngine(this)

  private[this]
  val _metadata: FabricWorkerMetadata = FabricWorkerMetadata(this)

  private[this]
  var _worker: FabricWorker = _

  private[this]
  var _listener: FabricWorkerListener = _

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def data: FabricWorkerData = _data

  final override
  def netClient: FabricNetClient = _netClient

  final override
  def engine: FabricWorkerEngine = _engine

  final override
  def metadata: FabricWorkerMetadata = _metadata

  final override
  def worker: FabricWorker = _worker

  override
  def talksTo(listener: FabricWorkerListener): this.type = {
    ensureNotRunning
    _listener = listener
    this
  }

  protected
  def registerWorker(): FabricWorker = {
    val isInCell = !configuration.burstFabricWorkerStandaloneProperty.get.getOrElse(false)
    val nodeMoniker = if (isInCell) configuration.burstFabricMonikerProperty.getOrThrow else f"W${System.nanoTime}%d"

    val cellMoniker = vitals.configuration.burstCellNameProperty.getOrThrow
    metadata.lookup.workerRegistration(cellMoniker, nodeMoniker, getPublicHostName, getPublicHostAddress) match {
      case Success(worker) => worker
      case Failure(t) =>
        log error s"unable to register worker $nodeMoniker into $cellMoniker"
        throw t
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    try {
      synchronized {
        ensureNotRunning

        _worker = registerWorker()
        if (containerId.isEmpty) {
          containerId = worker.nodeId
        }

        // start generic container
        super.start

        // start up local data service
        _data.start

        // start up local execution engine
        _engine.start

        // fabric protocol client (worker side)
        _netClient withEngine _engine
        _netClient.start

        // start all stores in classpath
        startWorkerStores(this)

        markRunning
      }
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        throw t
    }
    this
  }

  override
  def stop: this.type = {
    synchronized {
      ensureRunning
      _netClient.stop

      // start all stores in classpath
      stopWorkerStores(this)

      _engine.stop
      _data.stop

      // stop generic container
      super.stop

      markNotRunning
    }
    this
  }

}
