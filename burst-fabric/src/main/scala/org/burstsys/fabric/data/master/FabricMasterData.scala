/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.master

import java.util.concurrent.ConcurrentHashMap

import org.burstsys.fabric.container.FabricMasterService
import org.burstsys.fabric.container.master.FabricMasterContainer
import org.burstsys.fabric.data
import org.burstsys.fabric.data.model.ops.FabricCacheOps
import org.burstsys.fabric.data.master.op.{FabricDataAffinityOp, FabricDataCacheOps}
import org.burstsys.fabric.data.master.store.{getMasterStore, startMasterStores, stopMasterStores}
import org.burstsys.fabric.data.model.slice.FabricSlice
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.net.server.FabricNetServerListener
import org.burstsys.fabric.topology.master.FabricTopologyListener
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.uid._
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._

import scala.collection.JavaConverters._
import org.burstsys.vitals.logging._

import scala.concurrent.Future

/**
  * master side control of distributed cell data
  */
trait FabricMasterData extends FabricMasterService with FabricCacheOps {

  /**
    * get slices for a particular store. We let the data service manage which workers to use.
    *
    * @param guid       the global operation UID
    * @param datasource the data view to slice
    * @return
    */
  def slices(guid: VitalsUid, datasource: FabricDatasource): Future[Array[FabricSlice]]

  /**
    * the set of workers that are thought likely to have a slice's data in cache. Note that this is not
    * a guarantee in the future (once we are sparkfree)
    *
    * @param slice
    * @return
    */
  def affineWorkers(slice: FabricSlice): Array[FabricWorkerNode]


}

object FabricMasterData {

  def apply(container: FabricMasterContainer): FabricMasterData =
    FabricMasterDataContext(container: FabricMasterContainer)

}

private[data] final case
class FabricMasterDataContext(container: FabricMasterContainer) extends FabricMasterData
  with FabricNetServerListener with FabricDataAffinityOp with FabricDataCacheOps {

  override def modality: VitalsServiceModality = container.bootModality

  override val serviceName: String = s"fabric-master-data"

  override def toString: String = serviceName

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // state
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def slices(guid: VitalsUid, datasource: FabricDatasource): Future[Array[FabricSlice]] = {
    val tag = s"FabricMasterData.slices(guid=$guid, datasource=$datasource)"
    ensureRunning
    try {
      if (container.topology.healthyWorkers.isEmpty)
        throw VitalsException(s"FAB_SLICES_NO_WORKERS! $tag")
      getMasterStore(datasource).slices(guid, container.topology.healthyWorkers, datasource)
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        throw t
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    ensureNotRunning
    log info startingMessage
    container.netServer talksTo this
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
