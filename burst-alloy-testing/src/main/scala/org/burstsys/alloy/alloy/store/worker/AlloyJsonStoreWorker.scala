/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.alloy.store.worker

import org.burstsys.alloy.alloy
import org.burstsys.fabric.container.worker.FabricWorkerContainer
import org.burstsys.fabric.data.model.store.FabricStoreName
import org.burstsys.fabric.data.worker.store.FabricStoreWorker

final case
class AlloyJsonStoreWorker(container: FabricWorkerContainer) extends FabricStoreWorker
  with AlloyJsonStoreInitializer {

  override lazy val storeName: FabricStoreName = alloy.store.AlloyJsonStoreName

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
