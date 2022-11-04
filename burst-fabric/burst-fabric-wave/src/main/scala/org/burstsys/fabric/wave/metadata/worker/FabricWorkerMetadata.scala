/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.metadata.worker

import org.burstsys.fabric.container.FabricWorkerService
import org.burstsys.fabric.wave.metadata.model.FabricMetadataLookup
import org.burstsys.fabric.wave.container.worker.FabricWaveWorkerContainer
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsStandardServer}

/**
  * worker side control of distributed cell metadata
  * so far this service does not have a lot to do - we shall see where to take it
  */
trait FabricWorkerMetadata extends FabricWorkerService {

  /**
    * @return the worker metadata lookup interface
    */
  def lookup: FabricMetadataLookup

  /**
    * wire up this container to get metadata from a suitable metadata lookup
    *
    * @param lookup the lookup to use
    */
  def withLookup(lookup: FabricMetadataLookup): this.type
}

object FabricWorkerMetadata {

  def apply(container: FabricWaveWorkerContainer, modality: VitalsServiceModality = VitalsStandardServer): FabricWorkerMetadata =
    FabricWorkerMetadataContext(container: FabricWaveWorkerContainer, modality: VitalsServiceModality)

}

private final case
class FabricWorkerMetadataContext(container: FabricWaveWorkerContainer, modality: VitalsServiceModality)
  extends FabricWorkerMetadata {

  override def serviceName: String = s"fabric-worker-metadata"

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
