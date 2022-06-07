/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.buffer

import org.burstsys.tesla.TeslaTypes._


/**
  * Absolute positioned reads from the underlying chunk of memory
  */
trait TeslaBufferReader extends Any with TeslaBuffer {

  /**
    *
    * @param index
    * @return
    */
  def readBoolean(index: TeslaMemoryOffset): Boolean

  /**
    *
    * @param index
    * @param length
    * @return
    */
  def readBooleans(index: TeslaMemoryOffset, length: Int): Array[Boolean]

  /**
    *
    * @param absolutePosition
    * @param length
    * @param value
    * @return
    */
  def readBooleanInArray(absolutePosition: TeslaMemoryOffset, length: Int, value:Boolean): Boolean

  /**
    *
    * @param absolutePosition
    * @return
    */
  def readByte(absolutePosition: TeslaMemoryOffset): Byte

  /**
    *
    * @param absolutePosition
    * @param length
    * @return
    */
  def readBytes(absolutePosition: TeslaMemoryOffset, length: Int): Array[Byte]

  /**
    *
    * @param absolutePosition
    * @param length
    * @param value
    * @return
    */
  def readByteInArray(absolutePosition: TeslaMemoryOffset, length: Int, value:Byte): Boolean

  /**
    *
    * @param absolutePosition
    * @return
    */
  def readShort(absolutePosition: TeslaMemoryOffset): Short

  /**
    *
    * @param absolutePosition
    * @param length
    * @return
    */
  def readShorts(absolutePosition: TeslaMemoryOffset, length: Int): Array[Short]

  /**
    *
    * @param absolutePosition
    * @param length
    * @param value
    * @return
    */
  def readShortInArray(absolutePosition: TeslaMemoryOffset, length: Int, value:Short): Boolean

  /**
    *
    * @param absolutePosition
    * @return
    */
  def readInteger(absolutePosition: TeslaMemoryOffset): Int

  /**
    *
    * @param absolutePosition
    * @param length
    * @return
    */
  def readIntegers(absolutePosition: TeslaMemoryOffset, length: Int): Array[Int]

  /**
    *
    * @param absolutePosition
    * @param length
    * @param value
    * @return
    */
  def readIntegerInArray(absolutePosition: TeslaMemoryOffset, length: Int, value:Int): Boolean

  /**
    *
    * @param absolutePosition
    * @return
    */
  def readLong(absolutePosition: TeslaMemoryOffset): Long

  /**
    *
    * @param absolutePosition
    * @param length
    * @return
    */
  def readLongs(absolutePosition: TeslaMemoryOffset, length: Int): Array[Long]

  /**
    *
    * @param absolutePosition
    * @param length
    * @param value
    * @return
    */
  def readLongInArray(absolutePosition: TeslaMemoryOffset, length: Int, value:Long): Boolean

  /**
    *
    * @param absolutePosition
    * @return
    */
  def readDouble(absolutePosition: TeslaMemoryOffset): Double

  /**
    *
    * @param absolutePosition
    * @param length
    * @return
    */
  def readDoubles(absolutePosition: TeslaMemoryOffset, length: Int): Array[Double]

  /**
    *
    * @param absolutePosition
    * @param length
    * @param value
    * @return
    */
  def readDoubleInArray(absolutePosition: TeslaMemoryOffset, length: Int, value:Double): Boolean

  /**
    *
    * @param absolutePosition
    * @return
    */
  def readOffset(absolutePosition: TeslaMemoryOffset): TeslaMemoryOffset

}
