/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.supervisor

import org.burstsys.fabric.configuration
import org.burstsys.fabric.container.model.{FabricContainer, FabricContainerContext}
import org.burstsys.fabric.data.supervisor.FabricSupervisorData
import org.burstsys.fabric.data.supervisor.store._
import org.burstsys.fabric.execution.supervisor.FabricSupervisorExecution
import org.burstsys.fabric.metadata.supervisor.FabricSupervisorMetadata
import org.burstsys.fabric.net.FabricNetworkConfig
import org.burstsys.fabric.net.server.FabricNetServer
import org.burstsys.fabric.topology.supervisor.FabricSupervisorTopology
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsStandaloneServer, VitalsStandardServer}

import scala.language.postfixOps

/**
 * the one per JVM top level container for a Fabric Supervisor
 */
trait FabricSupervisorContainer extends FabricContainer {

  /**
   * the supervisor network
   *
   * @return
   */
  def netServer: FabricNetServer

  /**
   * the supervisor topology service
   *
   * @return
   */
  def topology: FabricSupervisorTopology

  /**
   * the supervisor data service
   *
   * @return
   */
  def data: FabricSupervisorData

  /**
   * the supervisor metadata service
   *
   * @return
   */
  def metadata: FabricSupervisorMetadata

  /**
   * the supervisor execution service
   *
   * @return
   */
  def execution: FabricSupervisorExecution

  /**
   * wire up this container to talk to a listener
   *
   * @return
   */
  def talksTo(listener: FabricSupervisorListener): this.type

}

abstract
class FabricSupervisorContainerContext(netConfig: FabricNetworkConfig) extends FabricContainerContext
  with FabricSupervisorContainer {

  override def serviceName: String = s"fabric-supervisor-container"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final lazy
  val bootModality: VitalsServiceModality = if (configuration.burstFabricSupervisorStandaloneProperty.getOrThrow)
    VitalsStandaloneServer else VitalsStandardServer

  private[this]
  val _net: FabricNetServer = FabricNetServer(this, netConfig)

  private[this]
  val _topology: FabricSupervisorTopology = FabricSupervisorTopology(this)

  private[this]
  val _data: FabricSupervisorData = FabricSupervisorData(this)

  private[this]
  val _metadata: FabricSupervisorMetadata = FabricSupervisorMetadata(this)

  private[this]
  val _execution: FabricSupervisorExecution = FabricSupervisorExecution(this)

  private[this]
  var _listener: FabricSupervisorListener = _

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // hookups
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def netServer: FabricNetServer = _net

  override
  def topology: FabricSupervisorTopology = _topology

  override
  def data: FabricSupervisorData = _data

  override
  def metadata: FabricSupervisorMetadata = _metadata

  override
  def execution: FabricSupervisorExecution = _execution

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    synchronized {
      ensureNotRunning

      if (containerId.isEmpty) {
        containerId = System.currentTimeMillis()
      }

      // make sure that subtype has already defined metadata lookup and stores before calling this super start

      // start generic container
      super.start

      // we now have metadata access from top level container

      // start up the remaining four pillars net, topology, data, and execution
      _net.start
      _topology.start
      _data.start
      _metadata.start
      _execution.start

      // start supervisor stores found in classpath
      startSupervisorStores(this)

      markRunning
    }
    this
  }

  override
  def stop: this.type = {
    synchronized {
      ensureRunning

      _net.stop
      _topology.stop
      _data.stop
      _metadata.stop
      _execution.stop

      // stop generic container
      super.stop

      // stop supervisor stores found in classpath
      stopSupervisorStores(this)

      markNotRunning
    }
    this
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // hookups
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def talksTo(listener: FabricSupervisorListener): this.type = {
    _listener = listener
    this
  }
}

