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
    */
  def currentUsedMemory(offset: TeslaMemoryOffset): Unit

  /**
    * increment the location of the end of currently active data in the buffer as an
    * offset from the beginning of the data section. 
    * It is not the total memory used by the buffer because it does not 
    * include any header info.
    * .
    */
  def incrementUsedMemory(increment: TeslaMemorySize): Unit

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    */
  def writeBoolean(data: Boolean): Unit

  /**
    * write data using absolute positioning (does not increment current used memory pointer)
    */
  def writeBoolean(data: Boolean, absolutePosition: TeslaMemoryOffset): Unit

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    */
  def writeBooleans(data: Array[Boolean]): Unit

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    */
  def writeByte(data: Byte): Unit

  /**
    * write data using absolute positioning (does not increment current used memory pointer)
    */
  def writeByte(data: Byte, absolutePosition: TeslaMemoryOffset): Unit

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    */
  def writeBytes(data: Array[Byte]): Unit

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    */
  def writeShort(data: Short): Unit

  /**
    * write data using absolute positioning (does not increment current used memory pointer)
    */
  def writeShort(data: Short, absolutePosition: TeslaMemoryOffset): Unit

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    */
  def writeShorts(data: Array[Short]): Unit

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    */
  def writeInt(data: Int): Unit

  /**
    * write data using absolute positioning (does not increment current used memory pointer)
    */
  def writeInt(data: Int, absolutePosition: TeslaMemoryOffset): Unit

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    */
  def writeInts(data: Array[Int]): Unit

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    */
  def writeLong(data: Long): Unit

  /**
    * write data using absolute positioning (does not increment current used memory pointer)
    */
  def writeLong(data: Long, absolutePosition: TeslaMemoryOffset): Unit

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    */
  def writeLongs(data: Array[Long]): Unit

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    */
  def writeDouble(data: Double): Unit

  /**
    * write data using absolute positioning (does not increment current used memory pointer)
    */
  def writeDouble(data: Double, absolutePosition: TeslaMemoryOffset): Unit

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    */
  def writeDoubles(data: Array[Double]): Unit

  /**
    * write data using relative positioning (write to end and increment the current used memory pointer)
    */
  def writeOffset(data: TeslaMemoryOffset): Unit

  /**
    * write data using absolute positioning (does not increment current used memory pointer)
    */
  def writeOffset(data: TeslaMemoryOffset, absolutePosition: TeslaMemoryOffset): Unit

}
