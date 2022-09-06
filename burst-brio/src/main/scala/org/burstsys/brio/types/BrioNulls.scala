/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.types

import org.burstsys.brio.types.BrioTypes.{BrioRelationCount, BrioRelationOrdinal}
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.tesla.buffer.{TeslaBufferReader, TeslaBufferWriter}
import org.burstsys.tesla.TeslaTypes._

/**
  * Null maps are an array of Long values where each bit of each Long records the null state of a given field.
  * A '''0''' records a ''null'' value, a '''1''' records a ''non-null'' value
  * <p/>
  * [[http://www.catonmat.net/blog/low-level-bit-hacks-you-absolutely-must-know]]
  */
object BrioNulls {

  @inline final
  def setAllNull(count: BrioRelationCount, writer: TeslaBufferWriter, nullMapLocation: TeslaMemoryOffset): Unit = {
    val words = mapLongCount(count)
    //    log info s"setAllNull($words words -> ${nullMapLocation.toInt})"
    var i = 0
    while (i < words) {
      writer.writeLong(0, nullMapLocation.toInt + (i * SizeOfLong))
      i += 1
    }
  }

  @inline final
  def printNullMap(count: BrioRelationCount, reader: TeslaBufferReader, nullMapLocation: TeslaMemoryOffset): String = {
    (for (i <- 0 until mapLongCount(count)) yield {
      val value = reader.readLong(nullMapLocation.toInt + (i * SizeOfLong))
      toBinary(value).reverse
    }).mkString("\n")
  }

  def toBinary(i: Long): String =
    String.format("%64s", i.toBinaryString).replace(' ', '0')


  @inline final
  def relationTestNull(reader: TeslaBufferReader, relationOrdinal: BrioRelationOrdinal, nullMapLocation: TeslaMemoryOffset): Boolean = {
    val key: BrioRelationOrdinal = relationOrdinal
    val offset = getWordOffset(key)
    val index = getIndexOffset(key)
    val bitmap = reader.readLong(nullMapLocation + (offset * SizeOfLong))
    ((bitmap >> index) & 0x1) != 0
  }

  @inline final
  def relationSetNull(reader: TeslaBufferReader, writer: TeslaBufferWriter, relationOrdinal: BrioRelationOrdinal, nullMapLocation: TeslaMemoryOffset): Unit = {
    val key: BrioRelationOrdinal = relationOrdinal
    val offset = getWordOffset(key)
    val index = getIndexOffset(key)
    val bit: Long = 1 << index
    val oldValue: Long = reader.readLong(nullMapLocation + (offset * SizeOfLong))
    val newValue: Long = oldValue | bit
    val position = nullMapLocation.toInt + (offset * SizeOfLong)
    writer.writeLong(newValue, position)
  }

  @inline final
  def relationClearNull(reader: TeslaBufferReader, writer: TeslaBufferWriter, relationOrdinal: BrioRelationOrdinal, nullMapLocation: TeslaMemoryOffset): Unit = {
    val key: BrioRelationCount = relationOrdinal
    val offset = getWordOffset(key)
    val index = getIndexOffset(key)
    val bit: Long = 1 << index
    val oldValue: Long = reader.readLong(nullMapLocation + (offset * SizeOfLong))
    val newValue: Long = oldValue & ~bit
    val position = nullMapLocation.toInt + (offset * SizeOfLong)
    writer.writeLong(newValue, position)
  }

  private final val word1 = 64
  private final val word2 = word1 + 64
  private final val word3 = word2 + 64

  @inline final
  def getWordOffset(i: BrioRelationOrdinal): Int = {
    if (i < 0) {
      throw VitalsException(s"getWordOffset($i)")
    }
    if (i < word1) return 0
    if (i < word2) return 1
    if (i < word3) return 2
    throw VitalsException(s"getWordOffset($i)")
  }

  // TODO - compiler tells us this cannot be inlined...
  /*@inline */final
  def getIndexOffset(i: BrioRelationOrdinal): Int = {
    if (i < 0) {
      throw VitalsException(s"getIndexOffset($i)")
    }
    if (i < word1) return i
    if (i < word2) return i - word1
    if (i < word3) return i - word2
    throw VitalsException(s"getIndexOffset($i)")
  }

  @inline final
  def readBit(i: BrioRelationOrdinal, map: Array[Long]): Boolean = {
    checkRelationKey(i)
    val offset = getWordOffset(i)
    val bitmap = map(offset)
    val index = getIndexOffset(i)
    val bit = 1 << index
    (bitmap & bit) != 0
  }

  @inline final
  def setBit(i: BrioRelationOrdinal, map: Array[Long]): Unit = {
    checkRelationKey(i)
    val offset = getWordOffset(i)
    val index = getIndexOffset(i)
    val bit = 1 << index
    map(offset) = map(offset) | bit
  }

  @inline final
  def clearBit(i: BrioRelationOrdinal, map: Array[Long]): Unit = {
    checkRelationKey(i)
    val offset = getWordOffset(i)
    val index = getIndexOffset(i)
    val bit = 1 << index
    map(offset) = map(offset) & ~bit
  }

  /**
    * These are one bit per field and stored in longs.
    * So this is either one or two longs (8 or 16 bytes)
    *
    * @param count
    * @return
    */
  @inline final
  def mapLongCount(count: BrioRelationCount): Int = {
    checkRelationCount(count)
    (count / (SizeOfLong * 8)) + 1
  }

  @inline final
  def mapByteCount(count: BrioRelationCount): Int = {
    checkRelationCount(count)
    mapLongCount(count) * SizeOfLong
  }

  @inline final
  def checkRelationCount(i: Int): Unit = {
    if (i < 1 || i > 127) {
      throw VitalsException(s"checkRelationCount($i) out of range")
    }
  }

  @inline final
  def checkRelationKey(i: Int): Unit = {
    if (i < 0 || i > 127) {
      throw VitalsException(s"checkRelationKey($i) out of range")
    }
  }


}
