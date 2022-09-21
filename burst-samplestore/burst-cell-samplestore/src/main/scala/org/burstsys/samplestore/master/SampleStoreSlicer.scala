/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.master

import org.burstsys.api._
import org.burstsys.fabric.data.model.slice._
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.samplestore.api.BurstSampleStoreApiRequestState.BurstSampleStoreApiRequestSuccess
import org.burstsys.samplestore.api._
import org.burstsys.samplestore.model._
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.instrument.prettyTimeFromNanos
import org.burstsys.vitals.logging._
import org.burstsys.vitals.uid._

import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.util.Failure
import scala.util.Success

/**
 * sample store specific cell master side ''slicing'' (data set partitioning)
 */
trait SampleStoreSlicer extends Any {

  self: SampleStoreMaster =>

  ///////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////

  final override
  def slices(guid: VitalsUid, workers: Array[FabricWorkerNode], datasource: FabricDatasource): Future[Array[FabricSlice]] = {
    val tag = s"SampleStoreSlicer.slices(guid=$guid, datasource=$datasource)"
    val start = System.nanoTime

    // call the samplesource coordinator
    getViewGenerator(guid, datasource) map { generator =>
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
        log error burstStdMsg(s"SAMPLE_STORE_SLICE_FAIL $t $tag", t)
        throw t
    }
  }


  private
  def getViewGenerator(guid: String, datasource: BurstSampleStoreDataSource): Future[SampleStoreGeneration] = {
    val tag = s"SampleStoreSlicer.getViewGenerator(guid=$guid, datasource=$datasource)"
    val promise = Promise[SampleStoreGeneration]()

    def FAIL(t: Throwable): Unit = {
      log error burstStdMsg(s"SAMPLE_STORE_GET_GEN_FAIL $t $tag", t)
      promise.failure(t)
    }

    try {
      twitterFutureToScalaFuture(apiClient.getViewGenerator(guid, datasource)) onComplete {
        case Failure(t) => FAIL(t)
        case Success(response) =>
          response.context.state match {
            case BurstSampleStoreApiRequestSuccess =>
              promise.success(
                SampleStoreGeneration(
                  guid, response.generationHash,
                  response.loci.get.map {
                    locus =>
                      SampleStoreDataLocus(locus.suid, locus.hostAddress, locus.hostName, locus.port, locus.partitionProperties)
                  }.toArray,
                  datasource.view.schemaName, response.motifFilter
                )
              )
            case t: Throwable => FAIL(t)
            case _ => ???
          }
      }
    } catch safely {
      case t: Throwable => FAIL(t)
    }
    promise.future
  }

}
