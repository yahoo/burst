/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2.key

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.offheap
import org.burstsys.zap.cube2.ZapCube2DimensionAxis

/**
 * A key (composite set of dimensions) that identifies a unique row within the cube. One of these
 * is stored within cube as the ''cursor'' that points to the ''current'' row used for operations.
 */
trait ZapCube2Key extends Any with ZapCube2DimensionAxis {

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Dimensions
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * a bit map of nulls for this key
   *
   * @return
   */
  def nullMap: Long

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Navigation
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   *
   * @param otherKey
   */
  def importFrom(otherKey: ZapCube2Key): Unit

  /**
   * return the bucket index for this key
   *
   * @return
   */
  def bucketIndex(buckets: Int): Int

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   *
   * @param dimensions
   */
  def initialize(dimensions: Int): Unit

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODEC
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def write(k: Kryo, out: Output): Unit

  def read(k: Kryo, in: Input): Unit

}

object ZapCube2Key {

  /**
   * instantiate a key val given an appropriately sized block of memory at ``startOffset``
   *
   * @param basePtr
   * @param dimensions
   * @return
   * TODO = be better!
   */
  def apply(basePtr: TeslaMemoryPtr, dimensions: Int): ZapCube2Key = {
    assert(dimensions < 64)
    //    assert(dimensions > 0 && dimensions < 64)
    val key = ZapCube2KeyAnyVal(basePtr)
    key.initialize(dimensions)
    key
  }

}

/**
 * ==Off Heap G2 Cube Key Value ==
 * {{{
 *   FIXED LENGTH HEADER (always the same size structure for all cubes)
 *   [ NULL_MAP    | LONG ] the nullity of each of the dimensions  (up to 64 currently)
 *   [ DIM_COUNT   | BYTE ] the number of dimensions (up to 64 currently)
 *
 *   VARIABLE SIZE DATA (changes based on cube schema)
 *   [ KEYS        | ARRAY[LONG] ] the primitive values of each of the dimensions  (up to 64 currently)
 * }}}
 */
final case
class ZapCube2KeyAnyVal(basePtr: TeslaMemoryPtr) extends AnyVal with ZapCube2Key {

  @inline override
  def nullMap: Long = offheap.getLong(basePtr + nullMapFieldOffset)

  @inline
  def nullMap_=(v: Long): Unit = offheap.putLong(basePtr + nullMapFieldOffset, v)

  @inline override
  def dimCount: Int = offheap.getByte(basePtr + dimCountFieldOffset)

  @inline
  def dimCount_=(v: Int): Unit = offheap.putByte(basePtr + dimCountFieldOffset, v.toByte)

  @inline override
  def dimRead(dimension: Int): BrioPrimitive = {
    val offset = dimensionValuesOffset + (dimension * SizeOfLong)
    offheap.getLong(basePtr + offset)
  }

  @inline override
  def dimWrite(dimension: Int, value: BrioPrimitive): Unit = {
    dimSetNotNull(dimension)
    val offset = dimensionValuesOffset + (dimension * SizeOfLong)
    offheap.putLong(basePtr + offset, value)
  }

  @inline override
  def dimSetNull(dimension: Int): Unit = {
    val bit: Long = 1L << dimension.toLong
    val oldNulls = nullMap
    val newNulls = oldNulls & ~bit
    dimWrite(dimension, 0L)
    // do this last since the above dimWrite sets the nullity
    nullMap = newNulls
  }

  @inline override
  def dimSetNotNull(dimension: Int): Unit = {
    val bit: Long = 1L << dimension.toLong
    val oldNulls = nullMap
    val newNulls = oldNulls | bit
    nullMap = newNulls
  }

  @inline override
  def dimIsNull(dimension: Int): Boolean = {
    val bit: Long = 1L << dimension.toLong
    val oldNulls = nullMap
    val newNulls = oldNulls & bit
    newNulls != bit
  }

  @inline override
  def initialize(dimensions: Int): Unit = {
    nullMap = 0L
    dimCount = dimensions
    var i = 0
    var cursor = basePtr + dimensionValuesOffset
    while (i < dimCount) {
      offheap.putLong(cursor, 0L)
      i += 1
      cursor += SizeOfLong
    }
  }

  @inline override
  def bucketIndex(buckets: Int): Int = {
    val prime: Long = 92821L
    var result: Long = 31L
    var i = 0
    var cursor = basePtr + dimensionValuesOffset
    while (i < dimCount) {
      val code = offheap.getLong(cursor)
      result = prime * (result >>> 1) + code
      i += 1
      cursor += SizeOfLong
    }
    Math.abs(result % buckets).toInt
  }

  @inline override
  def importFrom(otherKey: ZapCube2Key): Unit = {
    assert(dimCount == otherKey.dimCount)
    val ok = otherKey.asInstanceOf[ZapCube2KeyAnyVal]
    dimCount = ok.dimCount
    var d = 0
    while (d < dimCount) {
      if (ok.dimIsNull(d))
        dimSetNull(d)
      else
        dimWrite(d, ok.dimRead(d))
      d += 1
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // MISC
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  override def toString: String =
    s"KEY(basePtr=$basePtr nullMap=$nullMap, dimCount=$dimCount, [${(for (d <- 0 until dimCount) yield s"d$d=${dimRead(d)}").mkString(", ")}] )"

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODEC
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def write(k: Kryo, out: Output): Unit = {
    out writeLong nullMap
    out writeInt dimCount
    var d = 0
    while (d < dimCount) {
      out writeLong dimRead(d)
      d += 1
    }
  }

  override
  def read(k: Kryo, in: Input): Unit = {
    nullMap = in.readLong
    dimCount = in.readInt
    var d = 0
    while (d < dimCount) {
      dimWrite(d, in.readLong)
      d += 1
    }
  }
}
