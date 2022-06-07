/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.mock

import org.burstsys.fabric.container.worker.FabricWorkerContainer
import org.burstsys.fabric.data.model.store.FabricStoreName
import org.burstsys.fabric.data.worker.store.FabricStoreWorker

import scala.language.postfixOps


/**
 * Store to be used in unit tests
 */
final case
class MockStoreWorker(container: FabricWorkerContainer) extends FabricStoreWorker with MockInitializer {

  override lazy val storeName: FabricStoreName = MockStoreName

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
