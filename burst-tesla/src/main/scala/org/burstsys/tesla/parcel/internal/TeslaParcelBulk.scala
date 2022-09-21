/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.parcel.internal

import org.burstsys.tesla.TeslaTypes.TeslaMemoryPtr
import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.parcel.TeslaParcel
import org.burstsys.tesla.parcel.state.TeslaParcelState
import org.burstsys.tesla.TeslaTypes
import org.burstsys.tesla.offheap

import java.nio.ByteBuffer

/**
 * bulk operations byte ingress/egress operations
 * TODO: These methods looks sketchy, they don't copy or expose the parcel header information
 */
trait TeslaParcelBulk extends Any with TeslaParcel with TeslaParcelState {

  @inline final override
  def toHeapArray: Array[Byte] = {
    val size = currentUsedMemory
    val bytes = new Array[Byte](size)
    offheap.copyMemory(checkPtr(parcelStartPtr + bufferSlotsStart), bytes, size)
    bytes
  }

  @inline final override
  def fromHeapArray(bytes: Array[Byte]): Unit = {
    checkPtr(parcelStartPtr + bufferSlotsStart + bytes.length)
    offheap.copyMemory(bytes, checkPtr(parcelStartPtr + bufferSlotsStart), bytes.length)
    currentUsedMemory(bytes.length)
  }

  @inline final override
  def fromDeflatedMemoryPtr(number: Int, inflatedSize: TeslaMemorySize, deflatedSize: TeslaMemorySize, source: TeslaMemoryPtr): Unit = {
    this.bufferCount(number)
    this.inflatedSize(inflatedSize)
    this.deflatedSize(deflatedSize)
    isInflated(false)
    currentUsedMemory(deflatedSize)
    offheap.copyMemory(source, checkPtr(parcelStartPtr + bufferSlotsStart), deflatedSize)
  }

  @inline final override
  def asByteBuffer: ByteBuffer =
    offheap.directBuffer(bufferSlotsStartPtr, inflatedSize) order TeslaTypes.TeslaByteOrder

}
