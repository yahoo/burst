/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.buffer.access

import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.buffer.TeslaBufferWriter

/**
  * write accessors for tesla buffers
  */
trait TeslaBufferWriteAccessors extends Any with TeslaBufferWriter {

  //////////////////////////////////////////////////////////////////////////////////////////
  // writer ops
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def writeBoolean(data: Boolean): Unit = {
    writeBoolean(data, currentUsedMemory)
    incrementUsedMemory(SizeOfByte)
  }

  @inline final override
  def writeBoolean(data: Boolean, absolutePosition: TeslaMemoryOffset): Unit = {
    val ptr = checkPtr(dataPtr + absolutePosition)
    tesla.offheap.putByte(ptr, if (data) 1.toByte else 0.toByte)
  }

  @inline final override
  def writeBooleans(data: Array[Boolean]): Unit = {
    var i = 0
    while (i < data.length) {
      val ptr = checkPtr(dataPtr + currentUsedMemory)
      tesla.offheap.putByte(ptr, if (data(i)) 1.toByte else 0.toByte)
      incrementUsedMemory(SizeOfByte)
      i += 1
    }
  }

  @inline final override
  def writeByte(data: Byte): Unit = {
    writeByte(data, currentUsedMemory)
    incrementUsedMemory(SizeOfByte)
  }

  @inline final override
  def writeByte(data: Byte, absolutePosition: TeslaMemoryOffset): Unit = {
    val ptr = checkPtr(dataPtr + absolutePosition)
    tesla.offheap.putByte(ptr, data)
  }

  @inline final override
  def writeBytes(data: Array[Byte]): Unit = {
    var i = 0
    while (i < data.length) {
      val ptr = checkPtr(dataPtr + currentUsedMemory)
      tesla.offheap.putByte(ptr, data(i))
      incrementUsedMemory(SizeOfByte)
      i += 1
    }
  }

  @inline final override
  def writeShort(data: Short): Unit = {
    writeShort(data, currentUsedMemory)
    incrementUsedMemory(SizeOfShort)
  }

  @inline final override
  def writeShort(data: Short, absolutePosition: TeslaMemoryOffset): Unit = {
    val ptr = checkPtr(dataPtr + absolutePosition)
    tesla.offheap.putShort(ptr, data)
  }

  @inline final override
  def writeShorts(data: Array[Short]): Unit = {
    var i = 0
    while (i < data.length) {
      val ptr = checkPtr(dataPtr + currentUsedMemory)
      tesla.offheap.putShort(ptr, data(i))
      incrementUsedMemory(SizeOfShort)
      i += 1
    }
  }

  @inline final override
  def writeInt(data: Int): Unit = {
    writeInt(data, currentUsedMemory)
    incrementUsedMemory(SizeOfInteger)
  }

  @inline final override
  def writeInt(data: TeslaMemorySize, absolutePosition: TeslaMemoryOffset): Unit = {
    val ptr = checkPtr(dataPtr + absolutePosition)
    tesla.offheap.putInt(ptr, data)
  }

  @inline final override
  def writeInts(data: Array[Int]): Unit = {
    var i = 0
    while (i < data.length) {
      val ptr = checkPtr(dataPtr + currentUsedMemory)
      tesla.offheap.putInt(ptr, data(i))
      incrementUsedMemory(SizeOfInteger)
      i += 1
    }
  }

  @inline final override
  def writeLong(data: Long): Unit = {
    writeLong(data, currentUsedMemory)
    incrementUsedMemory(SizeOfLong)
  }

  @inline final override
  def writeLong(data: Long, absolutePosition: TeslaMemoryOffset): Unit = {
    val ptr = checkPtr(dataPtr + absolutePosition)
    tesla.offheap.putLong(ptr, data)
  }

  @inline final override
  def writeLongs(data: Array[Long]): Unit = {
    var i = 0
    while (i < data.length) {
      val ptr = checkPtr(dataPtr + currentUsedMemory)
      tesla.offheap.putLong(ptr, data(i))
      incrementUsedMemory(SizeOfLong)
      i += 1
    }
  }

  @inline final override
  def writeDouble(data: Double): Unit = {
    writeDouble(data, currentUsedMemory)
    incrementUsedMemory(SizeOfDouble)
  }

  @inline final override
  def writeDouble(data: Double, p: TeslaMemoryOffset): Unit = {
    val ptr = checkPtr(dataPtr + p)
    tesla.offheap.putDouble(ptr, data)
  }

  @inline final override
  def writeDoubles(data: Array[Double]): Unit = {
    var i = 0
    while (i < data.length) {
      tesla.offheap.putDouble(dataPtr + currentUsedMemory, data(i))
      incrementUsedMemory(SizeOfDouble)
      i += 1
    }
  }

  @inline final override
  def writeOffset(data: TeslaMemoryOffset): Unit = {
    writeInt(data.toInt)
  }

  @inline final override
  def writeOffset(data: TeslaMemoryOffset, absolutePosition: TeslaMemoryOffset): Unit =
    writeInt(data.toInt, absolutePosition)


}
