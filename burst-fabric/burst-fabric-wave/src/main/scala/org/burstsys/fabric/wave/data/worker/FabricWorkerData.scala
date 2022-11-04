/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.worker

import org.burstsys.fabric.container.FabricWorkerService
import org.burstsys.fabric.wave.data
import org.burstsys.fabric.wave.data.worker.cache.FabricSnapCache
import org.burstsys.fabric.wave.container.worker.FabricWaveWorkerContainer
import org.burstsys.vitals.VitalsService

/**
 * data management on the worker side
 */
trait FabricWorkerData extends FabricWorkerService {

  /**
   * the local worker cache
   *
   * @return
   */
  def cache: FabricSnapCache

}

object FabricWorkerData {

  def apply(container: FabricWaveWorkerContainer): FabricWorkerData =
    FabricWorkerDataContext(container)

}

private[fabric] final case
class FabricWorkerDataContext(container: FabricWaveWorkerContainer) extends FabricWorkerData {

  override def modality: VitalsService.VitalsServiceModality = container.bootModality

  override def cache: FabricSnapCache = data.worker.cache.instance

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    ensureNotRunning
    log info startingMessage

    // connect worker to cache
    container.withCache(cache)

    // start up cache singleton
    cache.start

    markRunning
    this
  }

  override
  def stop: this.type = {
    ensureRunning
    log info stoppingMessage

    // stop  cache singleton
    cache.stop

    markNotRunning
    this
  }

}
