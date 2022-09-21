/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.provider

import org.burstsys.brio
import org.burstsys.brio.blob.BrioBlobEncoder
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.press.BrioPressSink
import org.burstsys.brio.press.BrioPresser
import org.burstsys.tesla
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.tesla.parcel.TeslaParcel
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler

case class BrioSyntheticDatasetPresser(
                                        datasetProvider: BrioSyntheticDataProvider,
                                        bufferSize: Int = 1e6.toInt,
                                        parcelSize: Int = 10e6.toInt
                                      ) {

  def buffers(itemCount: Int): Iterator[TeslaMutableBuffer] = {
    val schema = BrioSchema(datasetProvider.schemaName)
    val pressBuffer = tesla.buffer.factory.grabBuffer(bufferSize)
    val dictionary = brio.dictionary.factory.grabMutableDictionary()

    datasetProvider.data(itemCount).map { item =>
      TeslaWorkerCoupler {
        val blobBuffer = tesla.buffer.factory.grabBuffer(bufferSize)
        try {
          val presser = BrioPresser(schema, BrioPressSink(pressBuffer, dictionary), datasetProvider.pressSource(item))
          val sink = presser.press
          BrioBlobEncoder.encodeV2Blob(sink.buffer, item.schemaVersion, sink.dictionary, blobBuffer)
          blobBuffer
        } finally {
          pressBuffer.reset
          dictionary.reset()
        }
      }
    }
  }

  def deflatedParcels(itemCount: Int): Iterator[TeslaParcel] = ParcelIterator(buffers(itemCount), parcelSize, deflate = true)

  def inflatedParcels(itemCount: Int): Iterator[TeslaParcel] = ParcelIterator(buffers(itemCount), parcelSize, deflate = false)
}

/**
 * An iterator that packs buffers into parcels. This iterator preserves the lazyness of the buffers iterator
 * @param buffers a source of pressed buffers
 * @param parcelSize how much memory to allocate for the uninflated parcel
 * @param deflate whether the produced parcel should be deflated or not
 */
private final
case class ParcelIterator(buffers: Iterator[TeslaMutableBuffer], parcelSize: Int, deflate: Boolean) extends Iterator[TeslaParcel] {

  private val inflatedParcel = tesla.parcel.factory.grabParcel(parcelSize)

  override def hasNext: Boolean = {
    val next = buffers.hasNext
    if (!next) tesla.parcel.factory.releaseParcel(inflatedParcel)
    next
  }

  override def next(): TeslaParcel = {
    TeslaWorkerCoupler {
      inflatedParcel.reset
      inflatedParcel.startWrites()
      var buffersInParcel = 0
      while (buffers.hasNext && buffersInParcel < 10) {
        val buf = buffers.next()
        inflatedParcel.writeNextBuffer(buf)
        tesla.buffer.factory.releaseBuffer(buf)
        buffersInParcel += 1
      }
      val parcel = tesla.parcel.factory.grabParcel(inflatedParcel.currentUsedMemory)
      if (deflate) parcel.deflateFrom(inflatedParcel)
      else parcel.copyFrom(inflatedParcel)
      parcel
    }
  }
}
