/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.worker.cache.internal

import org.burstsys.fabric.data.model.generation.FabricGeneration
import org.burstsys.fabric.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.data.model.ops._
import org.burstsys.fabric.data.model.slice.metadata.FabricSliceMetadata
import org.burstsys.fabric.data.worker.cache.FabricSnapCacheContext
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging.burstStdMsg
import org.burstsys.vitals.uid.VitalsUid

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Future, Promise}

/**
 * worker side front end for external operations on the [[org.burstsys.fabric.data.worker.cache.FabricSnapCache]]
 * These are called over the fabric supervisor->worker network protocol
 */
trait FabricSnapCacheOps extends Any {

  self: FabricSnapCacheContext =>

  final override
  def cacheGenerationOp(guid: VitalsUid, operation: FabricCacheManageOp, generationKey: FabricGenerationKey,
                        parameters: Option[Seq[FabricCacheOpParameter]]): Future[Seq[FabricGeneration]] = {
    lazy val tag = s"FabricSnapCacheOpApi.cacheGenerationOp(guid=$guid, operation=$operation, generationKey=$generationKey)"
    val promise = Promise[Seq[FabricGeneration]]()
    try {
      val generations = new ArrayBuffer[FabricGeneration]
      val iterator = allSnaps()
      while (iterator.hasNext) {
        val snap = iterator.next()
        if (generationKey.identifies(snap.metadata)) {
          try {
            generations += FabricGeneration(snap.metadata.datasource, Array(snap.metadata))
            operation match {
              case FabricCacheEvict => evictSnap(snap, "API")
              case FabricCacheFlush => flushSnap(snap, "API")
              case FabricCacheSearch => // nothing else for search calls (we just want to get the generations)
              case _ => ???
            }
          } catch safely {
            case t: Throwable =>
              log warn burstStdMsg(s"CACHE_FAIL $t $tag", t)
          }
        }
      }
      promise.success(generations.toSeq)
    } catch safely {
      case t: Throwable =>
        log warn burstStdMsg(s"CACHE_FAIL $t $tag", t)
        promise.failure(t)
    }
    promise.future
  }

  final override
  def cacheSliceOp(guid: VitalsUid, generationKey: FabricGenerationKey): Future[Seq[FabricSliceMetadata]] = {
    lazy val tag = s"FabricSnapCacheOpApi.cacheSliceOp(guid=$guid, generationKey=$generationKey)"
    val promise = Promise[Seq[FabricSliceMetadata]]()
    try {
      val sliceMetadata = new ArrayBuffer[FabricSliceMetadata]
      val iterator = allSnaps()
      while (iterator.hasNext) {
        val snap = iterator.next()
        if (generationKey.identifies(snap.metadata)) {
          sliceMetadata += snap.metadata
        }
      }
      log info s"CACHE_SLICE_OP generationCount=${sliceMetadata.size} $tag"
      promise.success(sliceMetadata.toSeq)
    } catch safely {
      case t: Throwable =>
        log warn burstStdMsg(s"CACHE_FAIL $t $tag", t)
        promise.failure(t)
    }
    promise.future
  }

}
