/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.lattice.codec

import org.burstsys.brio.types.BrioTypes._
import org.burstsys.tesla.TeslaTypes.{SizeOfShort, TeslaMemoryOffset, TeslaMemorySize}
import org.burstsys.tesla.buffer.{TeslaBufferReader, TeslaBufferWriter}

object BrioStringStringMapCodec {

  @inline final
  def calcStringStringMapSize(count: Int): TeslaMemorySize =
    SizeOfCount + (count * (SizeOfString + SizeOfString))

  /**
   * Write a map out to unsafe memory as array of key/value pairs sorted by key value
   *
   * @param data
   * @return number of bytes written
   */
  @inline final
  def writeStringStringMap(data: Map[BrioDictionaryKey, BrioDictionaryKey], writer: TeslaBufferWriter) {
    val sorted = data.toList.sortBy(_._1) // sort by key
    val size = data.size.toShort
    writer.writeShort(size) // number of map entries
    sorted.foreach(d => writer.writeShort(d._1)) // keys
    sorted.foreach(d => writer.writeShort(d._2)) // values
  }

  /**
   * Lookup the string dictionary identifier for a dictionary identifier key
   *
   * @param mKey
   * @param reader
   * @param offset
   * @return
   */
  @inline final
  def lookupStringStringMap(mKey: Int, reader: TeslaBufferReader, offset: TeslaMemoryOffset): BrioDictionaryKey = {
    var cursor: TeslaMemoryOffset = offset
    val length = reader.readShort(cursor).toInt
    if (length == 0) return BrioDictionaryNotFound
    cursor += SizeOfCount
    binarySearchShort(reader, cursor, length, mKey).toShort
  }

  /**
   *
   * @param mKey
   * @param reader
   * @param offset
   * @return
   */
  @inline final
  def lookupStringStringMapIsNull(mKey: Int, reader: TeslaBufferReader, offset: TeslaMemoryOffset): Boolean = {
    var cursor: TeslaMemoryOffset = offset
    val length: Int = reader.readShort(cursor).toInt
    if (length == 0) return true
    cursor += SizeOfCount.toInt
    binarySearchShort(reader, cursor, length, mKey) == BrioDictionaryNotFound.toInt
  }

  @inline final
  def lookupStringStringMapSize(reader: TeslaBufferReader, offset: TeslaMemoryOffset): BrioCount = {
    val cursor: TeslaMemoryOffset = offset
    reader.readShort(cursor)
  }

  @inline final
  def lookupStringStringMapKeys(reader: TeslaBufferReader, offset: TeslaMemoryOffset): Array[BrioDictionaryKey] = {
    var cursor: TeslaMemoryOffset = offset
    val length = reader.readShort(cursor)
    cursor += SizeOfCount
    val result = new Array[BrioDictionaryKey](length)
    val keyStart = cursor
    var i = 0
    while (i < length) {
      result(i) = reader.readShort(keyStart + (i * SizeOfShort))
      i += 1
    }
    result
  }

  /**
   * an off heap binary search for a short value. The off heap structure is a sorted array of keys followed by a sorted
   * arrays of values (both arrays are the same size)
   *
   * @param reader
   * @param offset
   * @param length
   * @param key
   * @return
   */
  @inline private
  def binarySearchShort(reader: TeslaBufferReader, offset: TeslaMemoryOffset, length: Int, key: Int): Int = {
    // start of the values list
    val valuesOffset: Int = offset.toInt + (length * SizeOfString.toInt)
    // main index into both key and value arrays
    var keyIndex: Int = 0

    // special case unit length vectors
    if (length == 1) {
      return if (keyValue(reader, offset, keyIndex) == key)
        valueValue(reader, keyIndex, valuesOffset)
      else BrioDictionaryNotFound.toInt
    }

    var lowKeyIndex: Int = 0
    var highKeyIndex: Int = length
    // all others
    while (lowKeyIndex <= highKeyIndex) {
      keyIndex = (lowKeyIndex + highKeyIndex) / 2 // pick a midpoint in between
      val midKeyVal: Long = keyValue(reader, offset, keyIndex) // get key value at current mid point
      if (midKeyVal < key)
        lowKeyIndex = keyIndex + 1
      else if (midKeyVal > key)
        highKeyIndex = keyIndex - 1
      else
        return valueValue(reader, keyIndex, valuesOffset)
    }
    BrioDictionaryNotFound.toInt
  }

  // key and value accessors
  @inline private
  def keyValue(reader: TeslaBufferReader, offset: TeslaMemoryOffset, keyIndex: TeslaMemoryOffset): Int =
    reader.readShort(offset + (keyIndex * SizeOfString))

  @inline private
  def valueValue(reader: TeslaBufferReader, keyIndex: TeslaMemoryOffset, valuesOffset: TeslaMemoryOffset): Int =
    reader.readShort(valuesOffset + keyIndex * SizeOfString)

}
