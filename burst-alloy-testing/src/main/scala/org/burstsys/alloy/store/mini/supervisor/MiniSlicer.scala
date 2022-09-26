/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.store.mini.supervisor

import org.burstsys.alloy.store.mini.MiniSlice
import org.burstsys.fabric.data.model.slice.{FabricSlice, FabricSliceKey}
import org.burstsys.fabric.data.supervisor.store.FabricStoreSupervisor
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.vitals.uid._

import scala.concurrent.Future

/**
 * supervisor side slicer for the mini store
 */
trait MiniSlicer extends FabricStoreSupervisor {

  final override
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

}
