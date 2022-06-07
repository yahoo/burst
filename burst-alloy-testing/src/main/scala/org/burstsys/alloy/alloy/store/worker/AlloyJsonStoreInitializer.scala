/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.alloy.store.worker

import org.burstsys.alloy.alloy.json._
import org.burstsys.alloy.alloy.store.sliceSet
import org.burstsys.alloy.alloy.{AlloyJsonFileProperty, AlloyJsonRootVersionProperty}
import org.burstsys.fabric.data.model.slice.state.{FabricDataState, FabricDataWarm}
import org.burstsys.fabric.data.model.snap.FabricSnap
import org.burstsys.fabric.data.worker.store.FabricWorkerLoader
import org.burstsys.fabric.execution.model.pipeline.publishPipelineEvent
import org.burstsys.tesla
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.logging._

/**
 * worker side cache initializer for the mini store
 */
trait AlloyJsonStoreInitializer extends FabricWorkerLoader {

  protected override
  def initializeSlice(snap: FabricSnap): FabricDataState = {
    val data = snap.data
    val metadata = snap.metadata
    sliceSet += data
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

  private
  def writeParcels(snap: FabricSnap): (Int, Int) = {
    val slice = snap.slice
    publishPipelineEvent(ParticleGotFile(slice.guid))
    var itemCount = 0
    var bufferTally = 0
    var byteCount = 0
    var inflatedParcel = tesla.parcel.factory.grabParcel(bufferSize)

    try {
      inflatedParcel.startWrites()

      def pushOut(): Unit = {
        bufferTally = 0
        val deflatedParcel = tesla.parcel.factory.grabParcel(bufferSize)
        deflatedParcel.deflateFrom(inflatedParcel)
        snap.data queueParcelForWrite deflatedParcel
        tesla.parcel.factory.releaseParcel(inflatedParcel)
        inflatedParcel = tesla.parcel.factory.grabParcel(bufferSize)
        inflatedParcel.startWrites()
      }

      publishPipelineEvent(ParticleReadFile(slice.guid))

      val view =snap.slice.datasource.view
      if (view.storeProperties.contains(AlloyJsonFileProperty)) {
      } else {
        throw VitalsException(s"no json property '$AlloyJsonFileProperty' " +
          s"found in store properties'")
      }

      loadFromJson(view.storeProperties(AlloyJsonFileProperty), view.schemaName, view.storeProperties(AlloyJsonRootVersionProperty).toInt)
        .filter(_ != null).foreach{ buffer =>
        itemCount += 1
        bufferTally += 1
        if (bufferTally > buffersPerParcel)
          pushOut()
        try {
          inflatedParcel.writeNextBuffer(buffer)
          byteCount += inflatedParcel.currentUsedMemory
        } finally
          tesla.buffer.factory.releaseBuffer(buffer)
      }
      if (bufferTally > 0)
        pushOut()
      (itemCount, byteCount)
    } finally
      tesla.parcel.factory.releaseParcel(inflatedParcel)
  }

}
