/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.model.slice

import org.burstsys.brio.blob.BrioBlob.BrioRegionIterator
import org.burstsys.tesla.parcel.TeslaParcel
import org.burstsys.vitals.logging._

import scala.concurrent.duration._
import scala.language.postfixOps

package object data extends VitalsLogger {

  final val slowWrite = 10 seconds

  final val useHose = false

  final val magicNumber:Int = 123210999

  final val version:Int = 1

  /**
   * API for writing to a slice.
   * step 1. call openSliceForWrites
   * step 2. call queueParcelForWrite for each parcel to write
   * step 3. call waitForWritesToComplete
   * step 4. call closeForWrites
   */
  trait FabricSliceWriteApi extends Any {

    /**
     * step 1. open slice for write pipeline
     */
    def openForWrites(): Unit

    /**
     * @return if this slice is open for writes
     */
    def isOpenForWrites:Boolean

    /**
     * add another deflated parcel into the write pipeline
     *
     * @param parcel
     */
    def queueParcelForWrite(parcel: TeslaParcel): Unit

    /**
     * wait for all writes in pipeline to complete, this must be called after all parcels have been queued
     *
     */
    def waitForWritesToComplete(): Unit

    /**
     * close region files
     *
     * @return
     */
    def closeForWrites(): Unit

  }

  /**
   * Slice Data Access API - slice must be in memory
   */
  trait FabricSliceDataApi {

    /**
     * return a per region set of in memory brio blob iterators for this slice
     *
     * @return
     */
    def iterators: Array[BrioRegionIterator]

  }

  /**
   * API for slice operations
   */
  trait FabricSliceStateApi extends Any {

    /**
     * open this slice for reads (mmap region files into memory)
     *
     * @return
     */
    def loadSliceIntoMemory(): Unit

    /**
     * close this slice for reads (close mmap region files )
     */
    def evictSliceFromMemory(): Unit

    /**
     * is this slice currently open for reads?
     *
     * @return
     */
    def sliceInMemory: Boolean

    /**
     * delete all files associated with the regions in this slice
     *
     * @return
     */
    def flushSliceFromDisk(): Unit

    /**
     * is this slice currently on disk??
     *
     * @return
     */
    def sliceOnDisk: Boolean

  }

}
