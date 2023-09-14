/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.supervisor

import org.burstsys.fabric.container.FabricSupervisorService
import org.burstsys.fabric.wave.data.model.ops.FabricCacheOps
import org.burstsys.fabric.wave.data.model.slice.FabricSlice
import org.burstsys.fabric.wave.data.supervisor.op.{FabricDataAffinityOp, FabricDataCacheOps}
import org.burstsys.fabric.wave.data.supervisor.store.getSupervisorStore
import org.burstsys.fabric.wave.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.fabric.wave.container.supervisor.{FabricWaveSupervisorContainer, FabricWaveSupervisorListener}
import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.logging._
import org.burstsys.vitals.uid._

import scala.concurrent.Future

/**
  * supervisor side control of distributed cell data
  */
trait FabricSupervisorData extends FabricSupervisorService with FabricCacheOps {

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
    * @return
    */
  def affineWorkers(slice: FabricSlice): Array[FabricWorkerNode]


}

object FabricSupervisorData {

  def apply(container: FabricWaveSupervisorContainer): FabricSupervisorData =
    FabricWaveSupervisorDataContext(container: FabricWaveSupervisorContainer)

}

private[data] final case
class FabricWaveSupervisorDataContext(container: FabricWaveSupervisorContainer) extends FabricSupervisorData
  with FabricWaveSupervisorListener with FabricDataAffinityOp with FabricDataCacheOps {

  override def modality: VitalsServiceModality = container.bootModality

  override val serviceName: String = s"fabric-supervisor-data"

  override def toString: String = serviceName

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // state
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  override def slices(guid: VitalsUid, datasource: FabricDatasource): Future[Array[FabricSlice]] = {
    val tag = s"FabricSupervisorData.slices(guid=$guid, datasource=$datasource)"
    ensureRunning
    try {
      if (container.topology.healthyWorkers.isEmpty)
        throw VitalsException(s"FAB_SLICES_NO_WORKERS! $tag")
      getSupervisorStore(datasource).slices(guid, container.topology.healthyWorkers, datasource)
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
    container talksTo this
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
