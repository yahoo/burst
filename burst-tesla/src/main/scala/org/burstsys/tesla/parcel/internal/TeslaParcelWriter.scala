/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.parcel.internal

import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.buffer.TeslaBuffer
import org.burstsys.tesla.parcel.TeslaParcel
import org.burstsys.tesla.parcel.state.TeslaParcelState
import org.burstsys.vitals.errors.VitalsException

/**
 * write (input) operations
 */
trait TeslaParcelWriter extends Any with TeslaParcel with TeslaParcelState {

  @inline final override
  def startWrites(): Unit = {
    if (!isInflated)
      throw VitalsException(s"is deflated and cannot be written $this")
    nextSlotOffset(bufferSlotsStart)
  }

  @inline final override
  def writeNextBuffer(buffer: TeslaBuffer): TeslaMemorySize = {
    if (!isInflated)
      throw VitalsException(s"is deflated and cannot be written $this")
    var writeOffset = nextSlotOffset

    val size = buffer.currentUsedMemory
    // check if this write will actually fit
    if (size + SizeOfInteger > maxAvailableMemory - currentUsedMemory)
      return -1
    // write the size of the buffer as its header (include size field)
    tesla.offheap.putInt(parcelStartPtr + writeOffset, size)
    writeOffset += SizeOfInteger

    // now write the buffer contents
    tesla.offheap.copyMemory(buffer.dataPtr, parcelStartPtr + writeOffset, size)

    // update how far into the parcel we are
    writeOffset += size
    nextSlotOffset(writeOffset)

    // and how many buffers have completed
    incrementBufferCount()
    // up the used memory by the buffer size and the length field
    incrementUsedMemory(size + SizeOfInteger)

    maxAvailableMemory - currentUsedMemory
  }

  override def copyFrom(parcel: TeslaParcel): Unit = {
    this.currentUsedMemory(parcel.currentUsedMemory)
    this.bufferCount(parcel.bufferCount)
    this.inflatedSize(parcel.inflatedSize)
    if (!parcel.isInflated) {
      this.deflatedSize(parcel.deflatedSize)
      this.isInflated(false)
    }
    this.nextSlotOffset(parcel.nextSlotOffset)
    tesla.offheap.copyMemory(parcel.bufferSlotsStartPtr, this.bufferSlotsStartPtr, parcel.currentUsedMemory)
  }

}
