/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.test.parcel

import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.test.support.TeslaAbstractSpec
import org.burstsys.tesla.thread.request.TeslaRequestCoupler
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler

class TeslaBasicParcelSpec extends TeslaAbstractSpec {

  "tesla parcel" should "access header" in {
    TeslaWorkerCoupler {
      val parcel = tesla.parcel.factory.grabParcel(1024)
      try {
        parcel.headerSize should equal(28)
        parcel.bufferCount should equal(0)
        parcel.inflatedSize should equal(0)
        parcel.isInflated should equal(true)
        parcel.nextSlotOffset should equal(28)
        parcel.maxAvailableMemory should equal(4056)
      } finally tesla.parcel.factory.releaseParcel(parcel)
    }
  }

  "tesla parcel" should "write and read buffers" in {
    val magics = Array(11, 22, 33, 44, 55, 66, 77, 88, 99)
    val howManyBuffers = 9

    TeslaWorkerCoupler {

      val parcel = tesla.parcel.factory.grabParcel(10 * SizeOfInteger)
      try {
        // do the writing
        parcel.startWrites()
        parcel.bufferCount should equal(0)
        for (i <- 0 until howManyBuffers) {
          val buffer = tesla.buffer.factory.grabBuffer(SizeOfInteger)
          try {

            // put some unique data in each buffer
            buffer.writeInt(magics(i))
            buffer.readInteger(0) should equal(magics(i))

            val availMem = parcel.writeNextBuffer(buffer)
            availMem should not equal (-1)
            availMem should be > 0
            parcel.bufferCount should equal(i + 1)
          } finally tesla.buffer.factory.releaseBuffer(buffer)
        }

        // do the reading
        parcel.bufferCount should equal(howManyBuffers)
        parcel.currentUsedMemory should equal(18 * SizeOfInteger) // the data and the length
        (parcel.maxAvailableMemory - parcel.currentUsedMemory) should be > 0

        parcel.startReads()
        val readBuffers = for (i <- 0 until parcel.bufferCount) yield parcel.readNextBuffer

        for (i <- readBuffers.indices) {
          val magic = magics(i)
          val readBuffer = readBuffers(i)
          var startOffset = 0
          val magicTest = readBuffer.readInteger(startOffset)
          magicTest should equal(magic)
        }

        readBuffers.length should equal(howManyBuffers)

        val checkNext = parcel.readNextBuffer
        checkNext.blockPtr should equal(TeslaNullMemoryPtr)

      } finally tesla.parcel.factory.releaseParcel(parcel)

    }
  }


}
