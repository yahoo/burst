/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.master

import org.burstsys.fabric.configuration
import org.burstsys.fabric.container.model.{FabricContainer, FabricContainerContext}
import org.burstsys.fabric.data.master.FabricMasterData
import org.burstsys.fabric.data.master.store._
import org.burstsys.fabric.execution.master.FabricMasterExecution
import org.burstsys.fabric.metadata.master.FabricMasterMetadata
import org.burstsys.fabric.net.FabricNetworkConfig
import org.burstsys.fabric.net.server.FabricNetServer
import org.burstsys.fabric.topology.master.{FabricMasterTopology, FabricTopologyReporter}
import org.burstsys.fabric.topology.model.node.master.FabricMaster
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsStandaloneServer, VitalsStandardServer}
import org.burstsys.vitals.net.{getPublicHostAddress, getPublicHostName}

import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
 * the one per JVM top level container for a Fabric Master
 */
trait FabricMasterContainer extends FabricContainer {

  /**
   * the  [[FabricMaster]] metadata type for this local master instance
   *
   * @return
   */
  def master: FabricMaster

  /**
   * the master network
   *
   * @return
   */
  def netServer: FabricNetServer

  /**
   * the master topology service
   *
   * @return
   */
  def topology: FabricMasterTopology

  /**
   * the master data service
   *
   * @return
   */
  def data: FabricMasterData

  /**
   * the master metadata service
   *
   * @return
   */
  def metadata: FabricMasterMetadata

  /**
   * the master execution service
   *
   * @return
   */
  def execution: FabricMasterExecution

  /**
   * wire up this container to talk to a listener
   *
   * @return
   */
  def talksTo(listener: FabricMasterListener): this.type

}

abstract
class FabricMasterContainerContext(netConfig: FabricNetworkConfig) extends FabricContainerContext
  with FabricMasterContainer {

  override def serviceName: String = s"fabric-master-container"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final lazy
  val bootModality: VitalsServiceModality = if (configuration.burstFabricMasterStandaloneProperty.getOrThrow)
    VitalsStandaloneServer else VitalsStandardServer

  private[this]
  val _net: FabricNetServer = FabricNetServer(this, netConfig)

  private[this]
  val _topology: FabricMasterTopology = FabricMasterTopology(this)

  private[this]
  val _data: FabricMasterData = FabricMasterData(this)

  private[this]
  val _metadata: FabricMasterMetadata = FabricMasterMetadata(this)

  private[this]
  val _execution: FabricMasterExecution = FabricMasterExecution(this)

  private[this]
  var _listener: FabricMasterListener = _

  private[this]
  var _master: FabricMaster = _

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // hookups
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def master: FabricMaster = _master

  override
  def netServer: FabricNetServer = _net

  override
  def topology: FabricMasterTopology = _topology

  override
  def data: FabricMasterData = _data

  override
  def metadata: FabricMasterMetadata = _metadata

  override
  def execution: FabricMasterExecution = _execution

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    synchronized {
      ensureNotRunning

      // register the master
      _master = registerMaster()

      if (containerId.isEmpty) {
        containerId = master.nodeId
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

      // start master stores found in classpath
      startMasterStores(this)

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

      // stop master stores found in classpath
      stopMasterStores(this)

      markNotRunning
    }
    this
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // hookups
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def talksTo(listener: FabricMasterListener): this.type = {
    _listener = listener
    this
  }

  private
  def registerMaster(): FabricMaster = {
    val nodeMoniker = configuration.burstFabricMonikerProperty.getOrThrow
    val cellMoniker = org.burstsys.vitals.configuration.burstCellNameProperty.getOrThrow
    metadata.lookup.masterRegistration(cellMoniker, nodeMoniker, getPublicHostName, getPublicHostAddress) match {
      case Success(master) => master
      case Failure(t) => throw t
    }
  }

}

