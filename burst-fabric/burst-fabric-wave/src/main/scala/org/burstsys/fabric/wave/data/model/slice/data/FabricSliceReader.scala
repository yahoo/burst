/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model.slice.data

import org.burstsys.brio.blob.BrioBlob
import org.burstsys.brio.blob.BrioBlob._
import org.burstsys.fabric.wave.data.model.slice.state.{FabricDataHot, FabricDataNoData, FabricDataWarm, FabricDataCold}
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

/**
 * read operations associated with a Fabric slice
 */
trait FabricSliceReader extends AnyRef {

  self: FabricSliceDataContext =>

  //////////////////////////////////////////
  // Lifecycle
  //////////////////////////////////////////

  final override
  def loadSliceIntoMemory(): Unit = {
    lazy val tag = s"FabricSliceReader.loadSliceIntoMemory($parameters)"
    if (sliceInMemory) {
      throw VitalsException(s"ALREADY IN MEMORY! $tag")
    }

    val start = System.currentTimeMillis
    synchronized {
      try {

        regions.foreach(_.loadRegionIntoMemory())

        // mark this in memory to ensure descriptor management doesn't mess with it until eviction...
        snap.metadata.state = FabricDataHot
        snap.metadata.generationMetrics.recordSliceNormalWarmLoad(System.currentTimeMillis - start)
        sliceInMemory = true
      } catch safely {
        case t: Throwable =>
          val msg = s"FAB_SLICE_READ_FAIL $t $tag"
          log error(burstStdMsg(msg, t), t)
          throw VitalsException(msg, t)
      }
    }
  }

  final override
  def evictSliceFromMemory(): Unit = {
    lazy val tag = s"FabricSliceReader.evictSliceFromMemory($parameters)"
    if (!sliceInMemory) {
      log.warn(s"NOT_IN_MEMORY! $tag")
    }
    synchronized {
      try {
        regions.foreach(_.evictRegionFromMemory())
        snap.metadata.state = FabricDataWarm
        snap.metadata.generationMetrics.recordSliceEvictOnWorker()
        sliceInMemory = false
      } catch safely {
        case t: Throwable =>
          val msg = s"FAB_SLICE_EVICT_FAIL $t $tag"
          log error(burstStdMsg(msg, t), t)
          throw VitalsException(msg, t)
      }
    }
  }


  final override
  def flushSliceFromDisk(): Unit = {
    lazy val tag = s"FabricSliceReader.flushSliceFromDisk($parameters)"
    synchronized {
      if (!sliceOnDisk) {
        log.warn(s"NOT_ON_DISK! $tag")
      }

      try {
        regions.foreach(_.flushRegionFromDisk())
        snap.metadata.state = FabricDataCold
        snap.metadata.generationMetrics.recordSliceFlushOnWorker()
        sliceOnDisk = false
      } catch safely {
        case t: Throwable =>
          val msg = s"FAB_SLICE_FLUSH_FAIL $t $tag"
          log error(burstStdMsg(msg, t), t)
          throw VitalsException(msg, t)
      }
    }
  }

  //////////////////////////////////////////
  // Data Access
  //////////////////////////////////////////

  final override
  def iterators: Array[BrioRegionIterator] = {
    lazy val tag = s"FabricSliceReader.iterators($parameters)"
    val start = System.currentTimeMillis
    try {

      // handle no data case
      val data = if (snap.metadata.state == FabricDataNoData) {
        Array(BrioBlob.emptyRegionIterator(sliceKey = snap.metadata.sliceKey))
      } else {
        if (!sliceInMemory) throw VitalsException(s"FAB_SLICE_NOT_IN_MEM $tag")
        regions.map(_.iterator)
      }
      data
    } catch safely {
      case t: Throwable =>
        val msg = s"FAB_SLICE_ITERATE_FAIL $t $tag"
        log error(burstStdMsg(msg, t), t)
        throw VitalsException(msg, t)
    }
  }

}
