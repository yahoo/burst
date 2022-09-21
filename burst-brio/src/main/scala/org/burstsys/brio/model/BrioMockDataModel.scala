/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.model

import org.burstsys.brio.blob.BrioBlobEncoder
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.press.{BrioPressInstance, BrioPressSink, BrioPressSource, BrioPresser}
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.tesla.parcel.TeslaParcel
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.{brio, tesla}

import scala.collection.mutable.ArrayBuffer

trait BrioMockDataModel {

  def pressSource(root: BrioPressInstance): BrioPressSource

  def items: Seq[BrioPressInstance]

  def schema: BrioSchema

  /**
   * press directly to buffers
   * NOTE: the mutable buffers created by this class need to be freed
   * outside of this method.
   */
  final
  lazy val pressToBuffers: Array[TeslaMutableBuffer] = {
    items.map {
      item =>
        TeslaWorkerCoupler {
          val pressBuffer = tesla.buffer.factory.grabBuffer(1e6.toInt)
          val blobBuffer = tesla.buffer.factory.grabBuffer(1e6.toInt)
          val dictionary = brio.dictionary.factory.grabMutableDictionary()
          val sink = BrioPressSink(pressBuffer, dictionary)
          val presser = BrioPresser(schema, sink, pressSource(item))
          try {
            presser.press
            BrioBlobEncoder.encodeV2Blob(sink.buffer, item.schemaVersion, sink.dictionary, blobBuffer)
            blobBuffer
          } finally {
            tesla.buffer.factory.releaseBuffer(pressBuffer)
            brio.dictionary.factory.releaseMutableDictionary(dictionary)
          }
        }
    }.toArray
  }

  /**
   * press directly to deflated parcels
   * NOTE: the parcels created by this class need to be freed
   * outside of this method.
   */
  final
  lazy val pressToDeflatedParcels: Array[TeslaParcel] = {
    val buffers = new ArrayBuffer[TeslaMutableBuffer]
    buffers ++= pressToBuffers
    val deflatedParcels = new ArrayBuffer[TeslaParcel]
    while (buffers.nonEmpty) {
      TeslaWorkerCoupler {
        val inflatedParcel = tesla.parcel.factory.grabParcel(10e6.toInt)
        try {
          inflatedParcel.startWrites()
          var buffersInParcel = 0
          while (buffers.nonEmpty && buffersInParcel < 20) {
            val buf = buffers.remove(0)
            inflatedParcel writeNextBuffer buf
            tesla.buffer.factory releaseBuffer buf
            buffersInParcel += 1
          }
          val deflatedParcel = tesla.parcel.factory.grabParcel(inflatedParcel.currentUsedMemory)
          deflatedParcel.deflateFrom(inflatedParcel)
          deflatedParcels += deflatedParcel
        } finally tesla.parcel.factory.releaseParcel(inflatedParcel)
      }
    }
    deflatedParcels.toArray
  }

  /**
   * press directly to inflated parcels
   * NOTE: the parcels created by this class need to be freed
   * outside of this method.
   */
  final
  lazy val pressToInflatedParcels: Array[TeslaParcel] = {
    val buffers = new ArrayBuffer[TeslaMutableBuffer]
    buffers ++= pressToBuffers
    val inflatedParcels = new ArrayBuffer[TeslaParcel]
    while (buffers.nonEmpty) {
      TeslaWorkerCoupler {
        val inflatedParcel = tesla.parcel.factory.grabParcel(10e6.toInt)
        inflatedParcel.startWrites()
        var buffersInParcel = 0
        while (buffers.nonEmpty && buffersInParcel < 2) {
          val buf = buffers.remove(0)
          inflatedParcel writeNextBuffer buf
          tesla.buffer.factory releaseBuffer buf
          buffersInParcel += 1
        }
        inflatedParcels += inflatedParcel
      }
    }
    inflatedParcels.toArray
  }

}
