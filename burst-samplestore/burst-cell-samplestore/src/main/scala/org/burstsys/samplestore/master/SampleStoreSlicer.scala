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
import org.burstsys.vitals
import org.burstsys.vitals.errors.{VitalsException, safely}
import org.burstsys.vitals.instrument.prettyTimeFromNanos
import org.burstsys.vitals.logging._
import org.burstsys.vitals.uid._

import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

/**
 * sample store specific cell master side ''slicing'' (data set partitioning)
 */
trait SampleStoreSlicer extends Any {

  self: SampleStoreMaster =>

  ///////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////

  type LocusBuffer = mutable.ArrayBuffer[SampleStoreLocus]

  final override
  def slices(guid: VitalsUid, workers: Array[FabricWorkerNode], datasource: FabricDatasource): Future[Array[FabricSlice]] = {
    val tag = s"SampleStoreSlicer.slices(guid=$guid, datasource=$datasource)"
    val start = System.nanoTime
    val promise = Promise[Array[FabricSlice]]
    val workerMap = new mutable.HashMap[FabricWorkerNode, LocusBuffer]

    // generalized failure routine
    def FAIL(t: Throwable): Unit = {
      log error burstStdMsg(s"SAMPLE_STORE_SLICE_FAIL $t $tag", t)
      promise.failure(t)
    }

    // first get the generator
    getViewGenerator(guid, datasource) onComplete {
      case Failure(t) => FAIL(t)
      case Success(generator) =>
        try {
          var i = 0
          // next get the workers
          generator.loci foreach {
            locus =>
              val worker = workers(i)
              workerMap.getOrElseUpdate(worker, new LocusBuffer) += locus
              i = if (i == workers.length - 1) 0 else i + 1
          }
          val motifFilter = generator.motifFilter
          val sliceCount = workerMap.size
          log info s"SAMPLE_STORE_SLICE_GOT_WORKERS workers=${workerMap.size} $tag"

          // mapping the slices to workers using nodeId as sliceKey.
          // Hey, nobody said keys had to fall in (0..slices) and as far as I can tell it's only used to build the region file path

          val monkeyHash = doMonkeyHash(generator)

          val slices = workerMap.keys.map {
            worker =>
              SampleStoreSlice(
                guid,
                sliceKey = worker.nodeId.toInt,
                generationHash = monkeyHash,
                sliceCount,
                datasource,
                motifFilter.getOrElse(throw VitalsException(s"$tag no motif filter!")),
                worker,
                workerMap(worker).toArray
              )
          }.toArray[FabricSlice]
          val elapsedNanos = System.nanoTime - start
          log info
            s"SAMPLE_STORE_SLICE_SUCCESS elapsedTime=$elapsedNanos (${prettyTimeFromNanos(elapsedNanos)}), slices=${
              slices.map(s => s"(${s.sliceKey} -> ${s.worker})").mkString("{", ",", "}")
            } $tag"
          promise.success(slices)
        } catch safely {
          case t: Throwable => FAIL(t)
        }
    }
    promise.future
  }


  private
  def getViewGenerator(guid: String, datasource: BurstSampleStoreDataSource): Future[SampleStoreGenerator] = {
    val tag = s"SampleStoreSlicer.getViewGenerator(guid=$guid, datasource=$datasource)"
    val promise = Promise[SampleStoreGenerator]

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
                SampleStoreGenerator(
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
