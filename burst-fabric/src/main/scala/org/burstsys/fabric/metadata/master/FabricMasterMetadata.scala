/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.metadata.master

import org.burstsys.fabric.container.FabricMasterService
import org.burstsys.fabric.container.master.FabricMasterContainer
import org.burstsys.fabric.metadata.model.FabricMetadataLookup
import org.burstsys.fabric.net.server.FabricNetServerListener
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsStandardServer}

/**
  * master side control of distributed cell metadata
  * so far this service does not have a lot to do - we shall see where to take it
  */
trait FabricMasterMetadata extends FabricMasterService {

  /**
    * the master metadata lookup interface
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

object FabricMasterMetadata {

  def apply(container: FabricMasterContainer, modality: VitalsServiceModality = VitalsStandardServer): FabricMasterMetadata =
    FabricMasterMetadataContext(container: FabricMasterContainer, modality: VitalsServiceModality)

}

private final case
class FabricMasterMetadataContext(container: FabricMasterContainer, modality: VitalsServiceModality)
  extends FabricMasterMetadata with FabricNetServerListener {

  override def serviceName: String = s"fabric-master-metadata"

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
