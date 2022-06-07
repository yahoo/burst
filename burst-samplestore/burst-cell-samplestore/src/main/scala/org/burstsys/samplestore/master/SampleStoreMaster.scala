/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.master

import org.burstsys.fabric.container.master.FabricMasterContainer
import org.burstsys.fabric.data.master.store._
import org.burstsys.fabric.data.model.store._
import org.burstsys.samplestore.SampleStoreName
import org.burstsys.samplestore.api._
import org.burstsys.vitals.VitalsService.VitalsStandardClient
import org.burstsys.vitals.healthcheck._

import scala.language.implicitConversions

/**
 * TODO
 *
 * '''NOTE:'''  the cell master sets up APIs as a ''client'' and remote end sets up APIs as a ''server''
 */
final case
class SampleStoreMaster(container: FabricMasterContainer) extends FabricStoreMaster
  with VitalsHealthMonitoredService with SampleStoreSlicer {

  override val storeName: FabricStoreName = SampleStoreName

  ///////////////////////////////////////////////////////////////////
  // STATE
  ///////////////////////////////////////////////////////////////////

  private[this]
  val _apiClient: SampleStoreApiService = SampleStoreApiService(VitalsStandardClient)

  ///////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////

  def apiClient: SampleStoreApiService = _apiClient

  def talksTo(l: SampleStoreApiListener): this.type = {
    _apiClient talksTo l
    this
  }

  ///////////////////////////////////////////////////////////////////
  // LIFECYCLE
  ///////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    sampleStoreServerLock synchronized {
      ensureNotRunning
      log info startingMessage
      _apiClient.start
    }
    markRunning
    this
  }

  override
  def stop: this.type = {
    sampleStoreServerLock synchronized {
      ensureRunning
      log info stoppingMessage
      _apiClient.stop
    }
    markNotRunning
    this
  }
}
