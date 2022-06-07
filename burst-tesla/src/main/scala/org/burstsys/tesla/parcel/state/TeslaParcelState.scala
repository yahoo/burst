/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.parcel.state

import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryOffset, TeslaMemoryPtr, TeslaMemorySize, _}
import org.burstsys.tesla.block.{TeslaBlock, TeslaBlockAnyVal}
import org.burstsys.tesla.parcel.TeslaParcel
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.errors.VitalsException

/**
 * The off heap state associated with the parcel
 * {{{
 *   |-------------------|-------------|---------|
 *   |            PARCEL HEADER (28 bytes)       |
 *   |-------------------|-------------|---------|
 *   |     field         |     type    | offset  |
 *   |-------------------|-------------|---------|
 *   | currentUsedMemory | 4 byte int  |   0     |
 *   | poolId            | 4 byte int  |   4     |
 *   | bufferCount       | 4 byte int  |   8     |
 *   | inflatedSize      | 4 byte int  |   12    |
 *   | deflatedSize      | 4 byte int  |   16    |
 *   | isInflated        | 4 byte int  |   20    |
 *   | nextSlotOffset    | 4 byte int  |   24    |
 *   |-------------------|-------------|---------|
 *
 *
 *   |-------------------|-------------|---------|
 *   |            EACH BUFFER SLOT               |
 *   |-------------------|-------------|---------|
 *   |     field         |     type    | offset  |
 *   |-------------------|-------------|---------|
 *   | bufferSize        | 4 byte int  |   28    |
 *   | bufferContent     | array[byte] |   32    |
 *   |-------------------|-------------|---------|
 *
 *   ....... buffer slots 1 through n ............
 *
 * }}}
 * '''NOTE: '''The state trait must be a universal trait separate from the AnyVal object - don't ask me why...
 */
trait TeslaParcelState extends Any with TeslaParcel {

  //////////////////////////////////////////////////////////////////////////////////////////
  // the information about the associated memory block
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline final
  def parcelStartPtr: TeslaMemoryPtr = TeslaBlockAnyVal(blockPtr).dataStart

  @inline final override
  def maxAvailableMemory: TeslaMemorySize = TeslaBlockAnyVal(blockPtr).dataSize - headerSize

  //////////////////////////////////////////////////////////////////////////////////////////
  // field 1 in the memory block is the position for writers (not needed for readers)
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline private
  def currentUsedMemoryFieldStart: TeslaMemoryOffset = 0

  @inline final override
  def currentUsedMemory: TeslaMemorySize = {
    val ptr = checkPtr(parcelStartPtr + currentUsedMemoryFieldStart)
    tesla.offheap.getInt(ptr)
  }

  @inline final
  def incrementUsedMemory(increment: TeslaMemorySize): Unit = {
    if (!isInflated)
      throw VitalsException(s"is deflated $this ")
    currentUsedMemory(currentUsedMemory + increment)
    inflatedSize(currentUsedMemory)
  }

  @inline final
  def currentUsedMemory(usedMemory: TeslaMemoryOffset): Unit = {
    val ptr = checkPtr(parcelStartPtr + currentUsedMemoryFieldStart)
    tesla.offheap.putInt(ptr, usedMemory)
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // field 2 in the memory block is the pool id
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline private final
  def poolIdFieldStart: TeslaMemoryOffset = currentUsedMemoryFieldStart + SizeOfInteger

  @inline final override
  def poolId: TeslaPoolId = {
    val ptr = checkPtr(parcelStartPtr + poolIdFieldStart)
    tesla.offheap.getInt(ptr)
  }

  @inline final
  def poolId(id: TeslaPoolId): Unit = {
    val ptr = checkPtr(parcelStartPtr + poolIdFieldStart)
    tesla.offheap.putInt(ptr, id)
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // field 4 in the memory block is the buffer count
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline private final
  def bufferCountFieldStart: TeslaMemoryOffset = poolIdFieldStart + SizeOfInteger

  @inline final override
  def bufferSlotsStartPtr: TeslaMemoryPtr = checkPtr(parcelStartPtr + bufferSlotsStart)

  @inline final override
  def bufferCount: Int = {
    val ptr = checkPtr(parcelStartPtr + bufferCountFieldStart)
    tesla.offheap.getInt(ptr)
  }

  @inline final
  def incrementBufferCount(): Unit = {
    bufferCount(bufferCount + 1)
  }

  @inline final
  def decrementBufferCount(): Unit = {
    bufferCount(bufferCount - 1)
  }

  @inline final
  def bufferCount(id: Int): Unit = {
    val ptr = checkPtr(parcelStartPtr + bufferCountFieldStart)
    tesla.offheap.putInt(ptr, id)
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // field 5 in the memory block is the inflated size
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline private final
  def inflatedSizeFieldStart: TeslaMemoryOffset = bufferCountFieldStart + SizeOfInteger

  @inline final override
  def inflatedSize: Int = {
    val ptr = checkPtr(parcelStartPtr + inflatedSizeFieldStart)
    tesla.offheap.getInt(ptr)
  }

  @inline final
  def inflatedSize(id: Int): Unit = {
    val ptr = checkPtr(parcelStartPtr + inflatedSizeFieldStart)
    tesla.offheap.putInt(ptr, id)
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // field  6 in the memory block is the deflated size
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline private
  def deflatedSizeFieldStart: TeslaMemoryOffset = inflatedSizeFieldStart + SizeOfInteger

  @inline final override
  def deflatedSize: Int = {
    if (isInflated)
      throw VitalsException(s"not deflated!!! $this ")

    val ptr = checkPtr(parcelStartPtr + deflatedSizeFieldStart)
    tesla.offheap.getInt(ptr)
  }

  @inline final
  def deflatedSize(size: Int): Unit = {
    val ptr = checkPtr(parcelStartPtr + deflatedSizeFieldStart)
    tesla.offheap.putInt(ptr, size)
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // field 7 in the memory block is the inflated marker
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline private
  def isInflatedFieldStart: TeslaMemoryOffset = deflatedSizeFieldStart + SizeOfInteger

  @inline final override
  def isInflated: Boolean = {
    val ptr = checkPtr(parcelStartPtr + isInflatedFieldStart)
    tesla.offheap.getInt(ptr) > 0
  }

  @inline final
  def isInflated(id: Boolean): Unit = {
    val ptr = checkPtr(parcelStartPtr + isInflatedFieldStart)
    tesla.offheap.putInt(ptr, if (id) 1 else 0)
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // field 8 in the next buffer slot offset
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline private
  def nextSlotFieldStart: TeslaMemoryOffset = isInflatedFieldStart + SizeOfInteger

  @inline final override
  def nextSlotOffset: TeslaMemoryOffset = {
    val ptr = checkPtr(parcelStartPtr + nextSlotFieldStart)
    tesla.offheap.getInt(ptr)
  }

  @inline final
  def nextSlotOffset(offset: TeslaMemoryOffset): Unit = {
    val ptr = checkPtr(parcelStartPtr + nextSlotFieldStart)
    tesla.offheap.putInt(ptr, offset)
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // header/slot locations
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline final override def headerSize: TeslaMemoryOffset = nextSlotFieldStart + SizeOfInteger

  @inline final override def bufferSlotsStart: TeslaMemoryOffset = headerSize

  //////////////////////////////////////////////////////////////////////////////////////////
  // life cycle
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline
  final override
  def initialize(id: TeslaPoolId): Unit = {
    reset
    poolId(id)
  }

  @inline
  final override
  def reset: TeslaParcel = {
    currentUsedMemory(0)
    bufferCount(0)
    inflatedSize(0)
    deflatedSize(0)
    isInflated(true)
    nextSlotOffset(bufferSlotsStart)
    this
  }

}
