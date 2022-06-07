/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.parcel.internal

import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes.TeslaNullMemoryPtr
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.tesla.parcel.TeslaParcel
import org.burstsys.tesla.parcel.state.TeslaParcelState
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.tesla.TeslaTypes._


/**
  * read (output) operations
  */
trait TeslaParcelReader extends Any with TeslaParcel with TeslaParcelState {

  @inline final override
  def startReads(): Unit = {
    if (!isInflated)
      throw VitalsException(s"is deflated and cannot be read $this")
    nextSlotOffset(bufferSlotsStart)
  }


  @inline final override
  def readNextBuffer: TeslaMutableBuffer = {
    if (!isInflated)
      throw VitalsException(s"is deflated and cannot be read $this")
    var readOffset = nextSlotOffset

    // are we going to read past our used space?
    if (readOffset > bufferSlotsStart + currentUsedMemory)
      return TeslaNullMemoryPtr
    if (bufferCount == 0)
      return TeslaNullMemoryPtr

    // read the size of the buffer as its header
    val byteSize = tesla.offheap.getInt(parcelStartPtr + readOffset)
    readOffset += SizeOfInteger
    val buffer: TeslaMutableBuffer = tesla.buffer.factory.grabBuffer(byteSize)

    // now read the buffer contents
    tesla.offheap.copyMemory(parcelStartPtr + readOffset, buffer.dataPtr, byteSize)
    buffer.currentUsedMemory(byteSize)

    // update how far into the parcel we are
    readOffset += byteSize
    nextSlotOffset(readOffset)

    // and how many buffers have completed
    decrementBufferCount()
    buffer
  }

}
