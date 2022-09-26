/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.mock

import org.burstsys.fabric.data.supervisor.store.FabricStoreSupervisor
import org.burstsys.fabric.data.model.slice.FabricSlice
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.vitals.uid._

import scala.concurrent.Future

/**
 * create slices from a datasource for mock store
 */
trait MockSlicer extends FabricStoreSupervisor {

  final override
  def slices(guid: VitalsUid, workers: Array[FabricWorkerNode], datasource: FabricDatasource): Future[Array[FabricSlice]] = {
    TeslaRequestFuture {
      (for (sliceKey <- workers.indices) yield {
        MockSlice(guid, sliceKey, currentGenerationHash, workers.length, datasource, motifFilter = "no-filter", workers(sliceKey))
      }).toArray
    }
  }

}
