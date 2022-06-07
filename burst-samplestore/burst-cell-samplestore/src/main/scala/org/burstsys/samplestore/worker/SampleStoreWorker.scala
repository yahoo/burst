/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.worker

import org.burstsys.fabric.container.worker.FabricWorkerContainer
import org.burstsys.fabric.data.worker.store.FabricStoreWorker
import org.burstsys.samplestore.SampleStoreName

import scala.language.postfixOps

/**
  * the worker side of the sample store
  */
final case
class SampleStoreWorker(container: FabricWorkerContainer) extends FabricStoreWorker with SampleStoreInitializer {

  override lazy val storeName: String = SampleStoreName

  ///////////////////////////////////////////////////////////////////
  // LIFECYCLE
  ///////////////////////////////////////////////////////////////////

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
