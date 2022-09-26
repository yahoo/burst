/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.supervisor

import org.burstsys.api._
import org.burstsys.fabric.container.supervisor.FabricSupervisorContainer
import org.burstsys.fabric.data.supervisor.store._
import org.burstsys.fabric.data.model.slice.FabricSlice
import org.burstsys.fabric.data.model.store._
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.samplestore.SampleStoreName
import org.burstsys.samplestore.api.BurstSampleStoreApiRequestState.BurstSampleStoreApiNotReady
import org.burstsys.samplestore.api.BurstSampleStoreApiRequestState.BurstSampleStoreApiRequestException
import org.burstsys.samplestore.api.BurstSampleStoreApiRequestState.BurstSampleStoreApiRequestInvalid
import org.burstsys.samplestore.api.BurstSampleStoreApiRequestState.BurstSampleStoreApiRequestSuccess
import org.burstsys.samplestore.api.BurstSampleStoreApiRequestState.BurstSampleStoreApiRequestTimeout
import org.burstsys.samplestore.api._
import org.burstsys.samplestore.api.client.SampleStoreApiClient
import org.burstsys.samplestore.model.SampleStoreLocus
import org.burstsys.samplestore.model.SampleStoreSlice
import org.burstsys.samplestore.model._
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.healthcheck._
import org.burstsys.vitals.instrument.prettyTimeFromNanos
import org.burstsys.vitals.logging.burstStdMsg
import org.burstsys.vitals.uid.VitalsUid

import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable
import scala.concurrent.Future
import scala.language.implicitConversions

final case
class SampleStoreSupervisor(container: FabricSupervisorContainer) extends FabricStoreSupervisor with VitalsHealthMonitoredService {

  override val storeName: FabricStoreName = SampleStoreName

  ///////////////////////////////////////////////////////////////////
  // STATE
  ///////////////////////////////////////////////////////////////////

  private[this]
  val _apiClient: SampleStoreApiClient = SampleStoreApiClient()

  ///////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////

  def apiClient: SampleStoreApiClient = _apiClient

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

  ///////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////

  override def slices(guid: VitalsUid, workers: Array[FabricWorkerNode], datasource: FabricDatasource): Future[Array[FabricSlice]] = {
    val tag = s"SampleStoreMaster.slices(guid=$guid, datasource=$datasource)"
    val start = System.nanoTime

    // call the samplesource coordinator
    twitterFutureToScalaFuture(apiClient.getViewGenerator(guid, datasource)) map { response =>
      response.context.state match {
        case BurstSampleStoreApiRequestSuccess =>
          val loci = response.loci.get.map(SampleStoreDataLocus(_)).toArray
          SampleStoreGeneration(guid, response.generationHash, loci, datasource.view.schemaName, response.motifFilter)

        case BurstSampleStoreApiRequestTimeout |
             BurstSampleStoreApiRequestException |
             BurstSampleStoreApiRequestInvalid |
             BurstSampleStoreApiNotReady =>
          throw VitalsException(s"Got ${response.context.state} from samplestore master")
      }
    } recover {
      case t =>
        throw VitalsException(s"Failed to get view generation guid=$guid datasource=$datasource", t)
    } map { generator =>
      val workerMap = new mutable.HashMap[FabricWorkerNode, mutable.ArrayBuffer[SampleStoreLocus]]
      val i = new AtomicInteger
      // spread the loci across the workers
      generator.loci foreach { locus =>
        val worker = workers(i.getAndIncrement() % workers.length)
        workerMap.getOrElseUpdate(worker, new mutable.ArrayBuffer[SampleStoreLocus]) += locus
      }
      val motifFilter = generator.motifFilter.getOrElse(throw VitalsException(s"$tag no motif filter!"))
      val sliceCount = workerMap.size
      log info s"SAMPLE_STORE_SLICE_GOT_WORKERS workers=${workerMap.size} $tag"

      // mapping the slices to workers using nodeId as sliceKey.
      // Hey, nobody said keys had to fall in (0..slices) and as far as I can tell it's only used to build the region file path
      val slices = workerMap.keys.map { worker =>
        SampleStoreSlice(
          guid, sliceKey = worker.nodeId.toInt, generator.generationHash, sliceCount,
          datasource, motifFilter, worker, workerMap(worker).toArray)
      }.toArray[FabricSlice]
      val elapsedNanos = System.nanoTime - start
      log info s"SAMPLE_STORE_SLICE_SUCCESS elapsedTime=$elapsedNanos (${prettyTimeFromNanos(elapsedNanos)}), slices=${slices.length} $tag"
      slices
    } recover {
      case t =>
        log error burstStdMsg(s"SAMPLE_STORE_SLICE_FAIL $tag", t)
        throw t
    }
  }

}
