/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.buffer.access

import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.buffer.TeslaBufferReader

/**
  * read accessors for tesla buffers
  */
trait TeslaBufferReadAccessors extends Any with TeslaBufferReader {

  //////////////////////////////////////////////////////////////////////////////////////////
  // reader ops
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def readBoolean(absolutePosition: TeslaMemoryOffset): Boolean = {
    val ptr = checkPtr(dataPtr + absolutePosition)
    if (tesla.offheap.getByte(ptr) == 1) true else false
  }

  @inline final override
  def readBooleans(absolutePosition: TeslaMemoryOffset, length: Int): Array[Boolean] = {
    val a = new Array[Boolean](length.toInt)
    var cursor = absolutePosition
    var i = 0
    while (i < length) {
      val ptr = checkPtr(dataPtr + cursor)
      a(i) = if (tesla.offheap.getByte(ptr) == 1) true else false
      cursor += SizeOfByte
      i += 1
    }
    a
  }

  @inline final override
  def readBooleanInArray(absolutePosition: TeslaMemoryOffset, length: Int, value:Boolean): Boolean = {
    var cursor = absolutePosition
    var i = 0
    while (i < length) {
      val ptr = checkPtr(dataPtr + cursor)
      if (tesla.offheap.getByte(ptr) == 1) return true
      cursor += SizeOfByte
      i += 1
    }
    false
  }

  @inline final override
  def readByte(absolutePosition: TeslaMemoryOffset): Byte = {
    val ptr = checkPtr(dataPtr + absolutePosition)
    tesla.offheap.getByte(ptr)
  }

  @inline final override
  def readBytes(absolutePosition: TeslaMemoryOffset, length: Int): Array[Byte] = {
    val a = new Array[Byte](length.toInt)
    var cursor = absolutePosition
    var i = 0
    while (i < length) {
      val ptr = checkPtr(dataPtr + cursor)
      a(i) = tesla.offheap.getByte(ptr)
      cursor += SizeOfByte
      i += 1
    }
    a
  }

  @inline final override
  def readByteInArray(absolutePosition: TeslaMemoryOffset, length: Int, value:Byte): Boolean = {
    var cursor = absolutePosition
    var i = 0
    while (i < length) {
      val ptr = checkPtr(dataPtr + cursor)
      if (tesla.offheap.getByte(ptr) == value) return true
      cursor += SizeOfByte
      i += 1
    }
    false
  }

  @inline final override
  def readShort(absolutePosition: TeslaMemoryOffset): Short = {
    val ptr = checkPtr(dataPtr + absolutePosition)
    tesla.offheap.getShort(ptr)
  }

  @inline final override
  def readShorts(absolutePosition: TeslaMemoryOffset, length: Int): Array[Short] = {
    val a = new Array[Short](length.toInt)
    var cursor = absolutePosition
    var i = 0
    while (i < length) {
      val ptr = checkPtr(dataPtr + cursor)
      a(i) = tesla.offheap.getShort(ptr)
      cursor += SizeOfShort
      i += 1
    }
    a
  }

  @inline final override
  def readShortInArray(absolutePosition: TeslaMemoryOffset, length: Int, value:Short): Boolean = {
    var cursor = absolutePosition
    var i = 0
    while (i < length) {
      val ptr = checkPtr(dataPtr + cursor)
      if (tesla.offheap.getShort(ptr) == value) return true
      cursor += SizeOfShort
      i += 1
    }
    false
  }

  @inline final override
  def readInteger(absolutePosition: TeslaMemoryOffset): Int = {
    val ptr = checkPtr(dataPtr + absolutePosition)
    tesla.offheap.getInt(ptr)
  }

  @inline final override
  def readIntegers(absolutePosition: TeslaMemoryOffset, length: Int): Array[Int] = {
    val a = new Array[Int](length.toInt)
    var cursor = absolutePosition
    var i = 0
    while (i < length) {
      val ptr = checkPtr(dataPtr + cursor)
      a(i) = tesla.offheap.getInt(ptr)
      cursor += SizeOfInteger
      i += 1
    }
    a
  }

  @inline final override
  def readIntegerInArray(absolutePosition: TeslaMemoryOffset, length: Int, value:Int): Boolean = {
    var cursor = absolutePosition
    var i = 0
    while (i < length) {
      val ptr = checkPtr(dataPtr + cursor)
      if (tesla.offheap.getInt(ptr) == value) return true
      cursor += SizeOfInteger
      i += 1
    }
    false
  }

  @inline final override
  def readLong(absolutePosition: TeslaMemoryOffset): Long = {
    val ptr = checkPtr(dataPtr + absolutePosition)
    tesla.offheap.getLong(ptr)
  }

  @inline final override
  def readLongs(absolutePosition: TeslaMemoryOffset, length: Int): Array[Long] = {
    val a = new Array[Long](length.toInt)
    var cursor = absolutePosition
    var i = 0
    while (i < length) {
      val ptr = checkPtr(dataPtr + cursor)
      a(i) = tesla.offheap.getLong(ptr)
      cursor += SizeOfLong
      i += 1
    }
    a
  }

  @inline final override
  def readLongInArray(absolutePosition: TeslaMemoryOffset, length: Int, value:Long): Boolean = {
    var cursor = absolutePosition
    var i = 0
    while (i < length) {
      val ptr = checkPtr(dataPtr + cursor)
      if ( tesla.offheap.getLong(ptr) == value) return true
      cursor += SizeOfLong
      i += 1
    }
    false
  }

  @inline final override
  def readDouble(absolutePosition: TeslaMemoryOffset): Double = {
    val ptr = checkPtr(dataPtr + absolutePosition)
    tesla.offheap.getDouble(ptr)
  }

  @inline final override
  def readDoubles(absolutePosition: TeslaMemoryOffset, length: Int): Array[Double] = {
    val a = new Array[Double](length.toInt)
    var cursor = absolutePosition
    var i = 0
    while (i < length) {
      val ptr = checkPtr(dataPtr + cursor)
      a(i) = tesla.offheap.getDouble(ptr)
      cursor += SizeOfDouble
      i += 1
    }
    a
  }

  @inline final override
  def readDoubleInArray(absolutePosition: TeslaMemoryOffset, length: Int, value:Double): Boolean = {
    var cursor = absolutePosition
    var i = 0
    while (i < length) {
      val ptr = checkPtr(dataPtr + cursor)
      if (tesla.offheap.getDouble(ptr) == value) return true
      cursor += SizeOfDouble
      i += 1
    }
    false
  }

  @inline final override
  def readOffset(absolutePosition: TeslaMemoryOffset): TeslaMemoryOffset = {
    readInteger(absolutePosition)
  }

}
