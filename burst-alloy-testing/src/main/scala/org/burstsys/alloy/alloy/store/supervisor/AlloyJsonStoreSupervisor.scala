/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.alloy.store.supervisor

import org.burstsys.alloy.alloy
import org.burstsys.alloy.store.mini.MiniSlice
import org.burstsys.fabric.wave.data.model.slice.{FabricSlice, FabricSliceKey}
import org.burstsys.fabric.wave.data.model.store.FabricStoreName
import org.burstsys.fabric.wave.data.supervisor.store.FabricStoreSupervisor
import org.burstsys.fabric.wave.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.fabric.wave.container.supervisor.FabricWaveSupervisorContainer
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.vitals.uid.VitalsUid

import scala.concurrent.Future
import scala.language.postfixOps

/**
 *
 */
final case
class AlloyJsonStoreSupervisor(container: FabricWaveSupervisorContainer) extends FabricStoreSupervisor {

  override lazy val storeName: FabricStoreName = alloy.store.AlloyJsonStoreName

  ///////////////////////////////////////////////////////////////////
  // Supervisor Store
  ///////////////////////////////////////////////////////////////////

  override
  def slices(guid: VitalsUid, workers: Array[FabricWorkerNode], datasource: FabricDatasource): Future[Array[FabricSlice]] = {
    TeslaRequestFuture {
      var sliceKey: FabricSliceKey = -1
      workers.map {
        worker =>
          sliceKey += 1
          MiniSlice(
            guid = guid, datasource = datasource, sliceKey = sliceKey, sliceHash = sliceKey.toString, slices = workers.length,
            motifFilter = "no-filter", worker = worker
          )
      }
    }
  }

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
