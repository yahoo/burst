/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.worker.store

import org.burstsys.fabric.wave.data.model.slice.state._
import org.burstsys.fabric.wave.data.model.snap.FabricSnap
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

/**
 * This is a helper class for stores that provides complete cache state and metrics management in one place.
 * All Fabric Store 'workers' implement this to fill the cache and handle status/errors etc
 * This has to be implemented by each [[org.burstsys.fabric.data.model.store.FabricStoreProvider]]
 */
trait FabricWorkerLoader extends Any {

  /**
   * implemented by subtypes to load data into the cache if needed...
   *
   * @param snap
   * @return
   */
  protected
  def initializeSlice(snap: FabricSnap): FabricDataState

  /**
   * main entry point to either fetch from cache or initialize to cache if necessary
   *
   * @param snap
   * @return
   */
  final
  def loadSliceFromCacheOrInitialize(snap: FabricSnap): Unit = {
    lazy val tag = s"FabricWorkerLoader.loadSliceFromCacheOrInitialize(guid=${snap.guid}, ${snap.slice.identity})"
    log info s"CACHE_LOAD_SLICE $tag"

    // get cache descriptor out of cache
    try {
      val fabricSliceData = snap.data
      snap.metadata.state match {

        /**
         * not in cache, lets put er there.
         */
        case FabricDataCold =>
          try {
            initializeSlice(snap) match {

              // its now on disk - get into memory
              case FabricDataWarm =>
                log info s"FabricDataWarm (loading into memory...)  $tag"
                fabricSliceData.loadSliceIntoMemory()

              // some sort of no data situation that did not throw exception...
              case FabricDataNoData =>
                log info s"FabricSliceNoData (returning empty iterator...) $tag "

              // It should not land into this case, if slice initialization fails - it should throw
              case FabricDataFailed =>
                val msg = s"FabricSliceFailed (\n${snap.metadata.failure}\n) $tag"
                log error burstStdMsg(msg)
                throw VitalsException(msg)

              case state =>
                val msg = s"BAD_STATE $state (throwing an exception...) $tag"
                log error burstStdMsg(msg)
                throw VitalsException(msg)
            }
          } catch safely {
            case t: Throwable =>
              log error burstStdMsg(s"FAIL $t (flush slice and set to FabricSliceUninitialized...) $tag", t)
              fabricSliceData.flushSliceFromDisk()
              snap.metadata.state = FabricDataCold
              throw t
          }

        /**
         * already on disc, lets mmap it into memory for reading ...
         */
        case FabricDataWarm =>
          log info s"FabricDataWarm (opening for read...) $tag"
          fabricSliceData.loadSliceIntoMemory()

        /**
         * Its in memory already
         */
        case FabricDataHot =>
          log warn burstStdMsg(s"FabricSliceInMemory (ready to go...) $tag ")

        /**
         * if we have an empty dataset return an empty iterator
         */
        case FabricDataNoData =>
          log info burstStdMsg(s"FabricSliceNoData (returning empty iterator...) $tag ")

        // definitely not ok
        case FabricDataFailed =>
          throw VitalsException(s"FabricSliceFailed (\n${snap.metadata.failure}\n) $tag ")

        // definitely not ok
        case state =>
          throw VitalsException(s"BAD_STATE $state $tag ")
      }

    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"FAIL $t $tag", t)
        throw t
    }
  }

}
