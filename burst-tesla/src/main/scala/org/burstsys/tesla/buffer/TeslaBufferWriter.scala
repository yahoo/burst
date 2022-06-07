/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.buffer

import org.burstsys.tesla.TeslaTypes.{TeslaMemoryOffset, TeslaMemorySize}


/**
  * Absolute and relative position writes to the underlying chunk of memory
  */
trait TeslaBufferWriter extends Any with TeslaBuffer {

  /**
    * The location of the end of currently active data in the buffer as an
    * offset from the beginning of the data section. 
    * It is not the total memory used by the buffer because it does not 
    * include any header info.
    *
    * @return
    */
  def currentUsedMemory: TeslaMemorySize

  /**
    * set the value of the end of the buffer as an offset from the beginning of the
    * data field. It is not the total memory used by the buffer because it does not 
    * include any header info.
    *
    * @param offset
    */
  def currentUsedMemory(offset: TeslaMemoryOffset): Unit

  /**
    * increment the location of the end of currently active data in the buffer as an
    * offset from the beginning of the data section. 
    * It is not the total memory used by the buffer because it does not 
    * include any header info.
    * .
    *
    * @param increment
    */
  def incrementUsedMemory(increment: TeslaMemorySize)

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    *
    * @param data
    */
  def writeBoolean(data: Boolean)

  /**
    * write data using absolute positioning (does not increment current used memory pointer)
    *
    * @param data
    * @param absolutePosition
    */
  def writeBoolean(data: Boolean, absolutePosition: TeslaMemoryOffset)

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    *
    * @param data
    */
  def writeBooleans(data: Array[Boolean])

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    *
    * @param data
    */
  def writeByte(data: Byte)

  /**
    * write data using absolute positioning (does not increment current used memory pointer)
    *
    * @param data
    * @param absolutePosition
    */
  def writeByte(data: Byte, absolutePosition: TeslaMemoryOffset)

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    *
    * @param data
    */
  def writeBytes(data: Array[Byte])

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    *
    * @param data
    */
  def writeShort(data: Short)

  /**
    * write data using absolute positioning (does not increment current used memory pointer)
    *
    * @param data
    * @param absolutePosition
    */
  def writeShort(data: Short, absolutePosition: TeslaMemoryOffset)

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    *
    * @param data
    */
  def writeShorts(data: Array[Short])

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    *
    * @param data
    */
  def writeInt(data: Int)

  /**
    * write data using absolute positioning (does not increment current used memory pointer)
    *
    * @param data
    * @param absolutePosition
    */
  def writeInt(data: Int, absolutePosition: TeslaMemoryOffset)

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    *
    * @param data
    */
  def writeInts(data: Array[Int])

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    *
    * @param data
    */
  def writeLong(data: Long)

  /**
    * write data using absolute positioning (does not increment current used memory pointer)
    *
    * @param data
    * @param absolutePosition
    */
  def writeLong(data: Long, absolutePosition: TeslaMemoryOffset)

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    *
    * @param data
    */
  def writeLongs(data: Array[Long])

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    *
    * @param data
    */
  def writeDouble(data: Double)

  /**
    * write data using absolute positioning (does not increment current used memory pointer)
    *
    * @param data
    * @param absolutePosition
    */
  def writeDouble(data: Double, absolutePosition: TeslaMemoryOffset)

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    *
    * @param data
    */
  def writeDoubles(data: Array[Double])

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    *
    * @param data
    */
  def writeOffset(data: TeslaMemoryOffset)

  /**
    * write data using absolute positioning (does not increment current used memory pointer)
    *
    * @param data
    * @param absolutePosition
    */
  def writeOffset(data: TeslaMemoryOffset, absolutePosition: TeslaMemoryOffset)

}
