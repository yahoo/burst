/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.result.row

import java.util

import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive
import org.burstsys.vitals.errors.VitalsException

/**
  * ==ZapCubeKey==
  * A value class that wraps an array of long primitives which represent the dimensional key for a cube. Nulls are
  * supported.
  * ===Null Map===
  * The first value is the null-map which has a bit set
  * for each value that is been set (non-null). The zero'th bit is for the first dimension, the first bit for the
  * second dimension etc.
  * ===Dimension Values===
  * The rest of the array is the actual dimension values. The second value is the first dimension, the third value
  * the second dimension etc
  *
  * @param data this must be pre-allocated to be an array one large than the number of dimensions
  */
final case
class FabricDataKeyAnyVal(data: Array[Long] = null) extends AnyVal {

  def dump: String = {
    s"key->${data.mkString(",")}"
  }

  @inline
  def nullMap: Long = data(0)

  @inline
  def clear: FabricDataKeyAnyVal = {
    util.Arrays.fill(data, 0.toByte) // TODO check to see which is faster.
    /*
    var i = 0
    while (i < cursor.data.length) {
      cursor.data(i) = 0L
      i += 1
    }
*/
    this
  }

  @inline
  def checkDimensions: FabricDataKeyAnyVal = {
    if (nullMap != 0L) return this
    var d = 0
    while (d < data.length - 1) {
      if (readKeyDimension(d) != 0L)
        throw VitalsException(s"bad readKeyDimension")
      d += 1
    }
    this
  }

  @inline
  def readKeyDimension(dimension: Int): Long = {
    data(dimension + 1)
  }

  @inline def writeKeyDimensionPrimitive(dimension: Int, value: BrioPrimitive): Unit = {
    writeKeyDimensionNotNull(dimension)
    data(dimension + 1) = value.toLong
  }

  @inline
  def writeKeyDimensionNull(dimension: Int): Unit = {
    val bit: Long = 1L << dimension.toLong
    val oldNulls = data(0)
    val newNulls = oldNulls & ~bit
    data(0) = newNulls
    data(dimension + 1) = 0L
  }

  @inline
  def writeKeyDimensionNotNull(dimension: Int): Unit = {
    val bit: Long = 1L << dimension.toLong
    val oldNulls = data(0)
    val newNulls = oldNulls | bit
    data(0) = newNulls
  }

  @inline
  def readKeyDimensionIsNull(dimension: Int): Boolean = {
    val bit: Long = 1L << dimension.toLong
    val oldNulls = data(0)
    val newNulls = oldNulls & bit
    newNulls != bit
  }

  @inline
  def hashcode(length: Int): Int = {
    data match {
      case null => 0
      case d =>
        var result: Int = 1
        var i = 0
        while (i < length) {
          val element = data(i)
          val elementHash: Int = (element ^ (element >>> 32)).toInt
          result = 31 * result + elementHash
          i += 1
        }
        result
    }
  }

}
