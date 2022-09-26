/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.metadata.supervisor

import org.burstsys.fabric.container.FabricSupervisorService
import org.burstsys.fabric.container.supervisor.FabricSupervisorContainer
import org.burstsys.fabric.metadata.model.FabricMetadataLookup
import org.burstsys.fabric.net.server.FabricNetServerListener
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsStandardServer}

/**
  * supervisor side control of distributed cell metadata
  * so far this service does not have a lot to do - we shall see where to take it
  */
trait FabricSupervisorMetadata extends FabricSupervisorService {

  /**
    * the supervisor metadata lookup interface
    *
    * @return
    */
  def lookup: FabricMetadataLookup

  /**
    * wire up this container to get metadata from a suitable metadata lookup
    *
    * @param lookup
    * @return
    */
  def withLookup(lookup: FabricMetadataLookup): this.type
}

object FabricSupervisorMetadata {

  def apply(container: FabricSupervisorContainer, modality: VitalsServiceModality = VitalsStandardServer): FabricSupervisorMetadata =
    FabricSupervisorMetadataContext(container: FabricSupervisorContainer, modality: VitalsServiceModality)

}

private final case
class FabricSupervisorMetadataContext(container: FabricSupervisorContainer, modality: VitalsServiceModality)
  extends FabricSupervisorMetadata with FabricNetServerListener {

  override def serviceName: String = s"fabric-supervisor-metadata"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _metadata: FabricMetadataLookup = _

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def lookup: FabricMetadataLookup = _metadata

  override
  def withLookup(lookup: FabricMetadataLookup): this.type = {
    ensureNotRunning
    _metadata = lookup
    this
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    ensureNotRunning
    log info startingMessage
    container.netServer talksTo this
    markRunning
    this
  }

  override
  def stop: this.type = {
    ensureRunning
    log info stoppingMessage
    markNotRunning
    this
  }

}
