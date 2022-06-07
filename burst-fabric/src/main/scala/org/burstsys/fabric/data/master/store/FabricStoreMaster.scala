/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.master.store

import org.burstsys.fabric.data.model.slice.FabricSlice
import org.burstsys.fabric.data.model.store.FabricStore
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.vitals.uid._

import scala.concurrent.Future

/**
 * master side processing and management for data stores
 */
trait FabricStoreMaster extends FabricStore {

  /**
   * the primary routine that a worker store is required to implement
   *
   * @param workers a set of candidate workers
   * @return
   */
  def slices(guid: VitalsUid, workers: Array[FabricWorkerNode], datasource: FabricDatasource): Future[Array[FabricSlice]]

}
