/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.buffer.state

import java.nio.ByteBuffer

import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.block._
import org.burstsys.tesla.buffer.access.{TeslaBufferReadAccessors, TeslaBufferWriteAccessors}
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.tesla.offheap
import org.burstsys.tesla.pool.TeslaPoolId

import scala.language.implicitConversions

/**
 * The off heap state associated with the [[org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer]]
 * {{{
 *   | Data Size   | Int -- the size in bytes of the buffers data capacity
 *   | Used Memory | Int -- the relative location for writes
 *   | Pool ID     | Int -- the id of the pool this was allocated from
 *   | Data        | Array[Byte] - the actual data of the buffer
 * }}}
 * '''NOTE: '''The state trait must be a universal trait separate from the AnyVal object - don't ask me why...
 */
trait TeslaMutableBufferState extends Any with TeslaMutableBuffer with TeslaBufferReadAccessors with TeslaBufferWriteAccessors {

  /////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * test to see if this buffer is acting as a 'null'
   *
   * @return
   */
  def isNullBuffer: Boolean = blockPtr == TeslaNullMemoryPtr

  //////////////////////////////////////////////////////////////////////////////////////////
  // the information about the associated memory block
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline
  def memoryPtr: TeslaMemoryPtr = TeslaBlockAnyVal(blockPtr).dataStart

  @inline
  def maxAvailableMemory: TeslaMemorySize = TeslaBlockAnyVal(blockPtr).dataSize

  //////////////////////////////////////////////////////////////////////////////////////////
  // second field in the memory block is the position for writers (not needed for readers)
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline private
  def currentUsedMemoryFieldStart: TeslaMemoryOffset = 0 // dataSizeStart + SizeOfLong

  @inline
  def currentUsedMemory: TeslaMemorySize = {
    val ptr = checkPtr(memoryPtr + currentUsedMemoryFieldStart)
    tesla.offheap.getInt(ptr)
  }

  @inline
  def incrementUsedMemory(increment: TeslaMemorySize): Unit =
    currentUsedMemory(currentUsedMemory + increment)

  @inline
  def currentUsedMemory(usedMemory: TeslaMemoryOffset): Unit = {
    val ptr = checkPtr(memoryPtr + currentUsedMemoryFieldStart)
    tesla.offheap.putInt(ptr, usedMemory)
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // third field in the memory block is the pool id
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline private
  def poolIdFieldStart: TeslaMemoryOffset = currentUsedMemoryFieldStart + SizeOfInteger

  @inline
  def poolId: TeslaPoolId = {
    val ptr = checkPtr(memoryPtr + poolIdFieldStart)
    tesla.offheap.getInt(ptr)
  }

  @inline
  def poolId(id: TeslaPoolId): Unit = {
    val ptr = checkPtr(memoryPtr + poolIdFieldStart)
    tesla.offheap.putInt(ptr, id)
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // this is where client data goes
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline override def dataPtr: TeslaMemoryPtr = memoryPtr + poolIdFieldStart + SizeOfInteger

  //////////////////////////////////////////////////////////////////////////////////////////
  // life cycle
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline
  def initialize(id: TeslaPoolId): Unit = {
    currentUsedMemory(0)
    poolId(id)
  }

  @inline
  def reset: TeslaMutableBuffer = {
    currentUsedMemory(0)
    this
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // bulk operations
  //////////////////////////////////////////////////////////////////////////////////////////
  @inline
  def toBytes: Array[Byte] = {
    val size = currentUsedMemory
    //    val size = if (cursor == 0) dataSize.toInt else cursor
    val bytes = new Array[Byte](size)
    offheap.copyMemory(dataPtr, bytes, size)
    bytes
  }

  @inline
  def loadBytes(bytes: Array[Byte]): Unit = {
    //    log info burstStdMsg(s"loading array of size ${bytes.length} to ${blockDataPtr + dataStart}")
    checkPtr(dataPtr + bytes.length)
    offheap.copyMemory(bytes, dataPtr, bytes.length)
    currentUsedMemory(bytes.length)
  }

  @inline
  def loadBytes(buffer: ByteBuffer): Unit = {
    ???
  }

  /**
   * load bytes into this buffer from another memory location
   *
   * @param source
   * @param size
   */
  @inline
  def loadBytes(source: TeslaMemoryPtr, size: TeslaMemorySize): Unit = {
    //    log info burstStdMsg(s"loading off heap memory of size $size from $source to ${blockDataPtr + dataStart}")
    checkPtr(dataPtr + size)
    offheap.copyMemory(source, dataPtr, size)
    currentUsedMemory(size)
  }


}
