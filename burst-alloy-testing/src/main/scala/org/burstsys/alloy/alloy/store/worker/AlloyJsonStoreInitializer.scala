/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.alloy.store.worker

import org.burstsys.alloy.alloy.AlloyJsonFileProperty
import org.burstsys.alloy.alloy.AlloyJsonRootVersionProperty
import org.burstsys.alloy.alloy.json._
import org.burstsys.fabric.data.model.slice.state.FabricDataState
import org.burstsys.fabric.data.model.slice.state.FabricDataWarm
import org.burstsys.fabric.data.model.snap.FabricSnap
import org.burstsys.fabric.data.worker.store.FabricWorkerLoader
import org.burstsys.fabric.execution.model.pipeline.publishPipelineEvent
import org.burstsys.tesla
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._
import org.burstsys.vitals.instrument.prettyByteSizeString
import org.burstsys.vitals.logging._

/**
 * worker side cache initializer for the mini store
 */
trait AlloyJsonStoreInitializer extends FabricWorkerLoader {

  protected override
  def initializeSlice(snap: FabricSnap): FabricDataState = {
    val data = snap.data
    val metadata = snap.metadata
    try {
      val start = System.nanoTime
      data.openForWrites()
      try {
        val (itemCount, byteCount) = TeslaWorkerCoupler(writeParcels(snap))
        val loadTookMs = ((System.nanoTime - start) / 1e6).toLong
        data.waitForWritesToComplete()
        metadata.state = FabricDataWarm
        metadata.generationMetrics.recordSliceNormalColdLoad(
          loadTookMs, data.regionCount, itemCount, expectedItemCount = itemCount,
          potentialItemCount = itemCount, rejectedItemCount = 0, byteCount
        )
      } finally data.closeForWrites()
      publishPipelineEvent(ParticleWroteSlice(snap.guid))
    } catch safely {
      case t: Throwable =>
        val msg = burstStdMsg(t)
        log.error(msg, t)
        throw VitalsException(msg, t)
    }
    metadata.state
  }

  private def writeParcels(snap: FabricSnap): (Int, Int) = {
    val slice = snap.slice
    publishPipelineEvent(ParticleGotFile(slice.guid))

    var itemCount = 0
    var bufferTally = 0
    var byteCount = 0
    var inflatedParcel = tesla.parcel.factory.grabParcel(bufferSize)

    try {
      inflatedParcel.startWrites()

      def pushOut(): Unit = {
        log info s"AlloyJsonInit queuing parcel parcelBuffers=${inflatedParcel.bufferCount} parcelBytes=${prettyByteSizeString(inflatedParcel.currentUsedMemory)}"
        bufferTally = 0
        val deflatedParcel = tesla.parcel.factory.grabParcel(bufferSize)
        deflatedParcel.deflateFrom(inflatedParcel)
        snap.data queueParcelForWrite deflatedParcel
        tesla.parcel.factory.releaseParcel(inflatedParcel)
        inflatedParcel = tesla.parcel.factory.grabParcel(bufferSize)
        inflatedParcel.startWrites()
      }

      publishPipelineEvent(ParticleReadFile(slice.guid))

      val view = snap.slice.datasource.view
      if (!view.storeProperties.contains(AlloyJsonFileProperty)) {
        throw VitalsException(s"no json property '$AlloyJsonFileProperty' found in store properties'")
      }

      val buffers = loadFromJson(view.storeProperties(AlloyJsonFileProperty), view.schemaName, view.storeProperties(AlloyJsonRootVersionProperty).toInt)
        .filter(_ != null)
      buffers.foreach({ buffer =>
        itemCount += 1
        if (inflatedParcel.bufferCount >= buffersPerParcel) {
          log info "AlloyJsonInit parcel reached buffer limit, pushing parcel"
          pushOut()
        }

        try {
          val parcelRemaining = prettyByteSizeString(inflatedParcel.maxAvailableMemory - inflatedParcel.currentUsedMemory)
          val bufferSize = prettyByteSizeString(buffer.currentUsedMemory)
          log info s"AlloyJsonInit try write buffer item=$itemCount parcel_buffers=${inflatedParcel.bufferCount} parcel_available=$parcelRemaining buffer_size=$bufferSize"
          val remaining = inflatedParcel.writeNextBuffer(buffer)
          if (remaining == -1) {
            log info "AlloyJsonInit could not write buffer to parcel, not enough space remaining"
            pushOut()
            inflatedParcel.writeNextBuffer(buffer)
          }
          byteCount += buffer.currentUsedMemory
        } finally tesla.buffer.factory.releaseBuffer(buffer)
      })
      log info s"AlloyJsonInit pressed json buffers=${buffers.length}"

      if (inflatedParcel.bufferCount > 0) {
        log info "AlloyJsonInit all bufferes queued, pushing last parcel"
        pushOut()
      }

      (itemCount, byteCount)
    } finally
      tesla.parcel.factory.releaseParcel(inflatedParcel)
  }

}
