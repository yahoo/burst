/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.parcel

import org.burstsys.tesla
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.vitals.logging._

import scala.collection.mutable.ArrayBuffer

package object mock extends VitalsLogger {

  /**
   * press directly to buffers
   * NOTE: the mutable buffers created by this class need to be freed
   * outside of this method.
   */
  final
  def pressToBuffers(buffCount: Int, buffSize: Int): Array[TeslaMutableBuffer] = {
    ???
/*
    items.map {
      item =>
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
    }.toArray
*/
  }


  /**
   * press directly to deflated parcels
   * NOTE: the parcels created by this class need to be freed
   * outside of this method.
   */
  final
  def pressToDeflatedParcels(input: Array[TeslaMutableBuffer]): Array[TeslaParcel] = {
    val buffers = new ArrayBuffer[TeslaMutableBuffer]
    buffers ++= input
    val deflatedParcels = new ArrayBuffer[TeslaParcel]
    while (buffers.nonEmpty) {
      val inflatedParcel = tesla.parcel.factory.grabParcel(10e6.toInt)
      try {
        inflatedParcel.startWrites()
        var buffersInParcel = 0
        while (buffers.nonEmpty && buffersInParcel < 2) {
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
    deflatedParcels.toArray
  }

  /**
   * press directly to inflated parcels
   * NOTE: the parcels created by this class need to be freed
   * outside of this method.
   */
  final
  def pressToInflatedParcels(input: Array[TeslaMutableBuffer]): Array[TeslaParcel] = {
    val buffers = new ArrayBuffer[TeslaMutableBuffer]
    buffers ++= input
    val inflatedParcels = new ArrayBuffer[TeslaParcel]
    while (buffers.nonEmpty) {
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
    inflatedParcels.toArray
  }

}
