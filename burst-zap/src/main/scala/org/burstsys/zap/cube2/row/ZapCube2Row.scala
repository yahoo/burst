/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2.row

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.offheap
import org.burstsys.zap.cube2.key.ZapCube2Key
import org.burstsys.zap.cube2.state.EmptyLink
import org.burstsys.zap.cube2.{ZapCube2, ZapCube2AggregationAxis, ZapCube2DimensionAxis}

/**
 * one row within a [[org.burstsys.zap.cube2.ZapCube2]]. This is combination of a
 * set of dimensions (as a unique ''key'') and a set of aggregations. Also included
 * are ''nullity'' maps for both as well as some local knowledge about aggregation & dimensions
 * counts used for internal operations. Rows can ''link'' to other rows so they can be
 * placed into linked ''bucket lists''.
 */
trait ZapCube2Row extends Any with ZapCube2DimensionAxis with ZapCube2AggregationAxis {

  /**
   * row as offset into cube
   *
   * @return
   */
  def basePtr: TeslaMemoryPtr

  def fauxRow: Boolean

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Dirty Field
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * has this row been update in current algorithm (used for tracking row upsizing restarts). This relies on the fact
   * that during all algorithms that create rows, any given row is only '''touched''' once and that the algorithms
   * are essentially reentrant after being resized.
   *
   * @return
   */
  def dirty: Boolean

  def dirty_=(v: Boolean): Unit

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Nullity
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   *
   * @return
   */
  def dimNullMap: Long

  def dimNullMap_=(v: Long): Unit

  /**
   * aggregation nullity
   *
   * @return
   */
  def aggNullMap: Long

  def aggNullMap_=(v: Long): Unit

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Linked List
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * the offset to the next row in the bucket list
   *
   * @return
   */
  def link: Int

  def link_=(l: Int): Unit

  /**
   * is this row the end of the bucket list
   *
   * @return
   */
  def isListEnd: Boolean

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Initialize
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def initialize(dimensions: Int, aggregations: Int): Unit

  /**
   *
   * @param key
   */
  def initializeFromKey(key: ZapCube2Key): Unit

  /**
   * Copy in all data from external row other than the start offset and the link column
   *
   * @param thatRow
   * @return
   */
  def initializeFromRow(thatRow: ZapCube2Row): Unit

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // matching
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * key matching
   *
   * @param key
   * @return
   */
  def matchesKey(key: ZapCube2Key): Boolean

  def matchesRow(thatRow: ZapCube2Row): Boolean

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODEC
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def write(k: Kryo, out: Output): Unit

  def read(k: Kryo, in: Input): Unit

}

object ZapCube2Row {

  final
  def apply(startPtr: TeslaMemoryPtr, dimensions: Int, aggregations: Int): ZapCube2Row = {
    val row = ZapCube2RowAnyVal(startPtr)
    if (startPtr != TeslaNullMemoryPtr)
      row.initialize(dimensions = dimensions, aggregations = aggregations)
    row
  }

  final
  def apply(cube: ZapCube2, startOffset: TeslaMemoryOffset): ZapCube2Row =
    ZapCube2RowAnyVal(cube.basePtr + startOffset)

  final
  def apply(startPtr: TeslaMemoryPtr, startOffset: TeslaMemoryOffset): ZapCube2Row =
    ZapCube2RowAnyVal(startPtr + startOffset)

  /*
    final
    def apply(startPtr: TeslaMemoryPtr): ZapCube2Row =
      ZapCube2RowAnyVal(startPtr)
  */

  final
  def byteSize(dimCount: Int, aggCount: Int): TeslaMemorySize =
    SizeOfLong + SizeOfLong + // null maps
      SizeOfByte + SizeOfByte + // dim/agg counts
      SizeOfByte + // dirty field
      SizeOfInteger + // link to next row in bucket list
      (dimCount * SizeOfLong) + // dimension values
      (aggCount * SizeOfLong) // aggregations values

}

/**
 * ==Off Heap G2 Cube Row Value ==
 * {{{
 *   FIXED LENGTH HEADER (always the same size structure for all cubes)
 *   [ DIM_NULL_MAP    | LONG  ] the nullity of each of the dimensions  (up to 64 currently)
 *   [ AGG_NULL_MAP    | LONG  ] the nullity of each of the aggregations  (up to 64 currently)
 *   [ DIM_COUNT       | BYTE  ] the number of dimensions  (up to 64 currently)
 *   [ AGG_COUNT       | BYTE  ] the number of aggregations  (up to 64 currently)
 *   [ DIRTY           | BYTE  ] true if this row has been updated already in a algorithm
 *   [ LINK            | INT   ] offset to next row in bucket list
 *
 *   VARIABLE SIZE DATA (changes based on cube schema)
 *   [ DIMS        | ARRAY[LONG] ] the primitive values of each of the dimensions  (up to 64 currently)
 *   [ AGGS        | ARRAY[LONG] ] the primitive values of each of the aggregations  (up to 64 currently)
 * }}}
 * '''NOTE:''' we captures things like dimension and aggregation counts here even thought they are available in cube.
 * This is cause its a pain to have to gain access to parent objects in value classes.
 *
 */
final case
class ZapCube2RowAnyVal(basePtr: TeslaMemoryPtr) extends AnyVal with ZapCube2Row {

  @inline override
  def fauxRow: Boolean = basePtr == TeslaNullOffset

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // offheap state
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline override
  def dimNullMap: Long = offheap.getLong(basePtr + dimNullMapFieldOffset)

  @inline
  def dimNullMap_=(v: Long): Unit = offheap.putLong(basePtr + dimNullMapFieldOffset, v)

  @inline override
  def aggNullMap: Long = offheap.getLong(basePtr + aggNullMapFieldOffset)

  @inline
  def aggNullMap_=(v: Long): Unit = offheap.putLong(basePtr + aggNullMapFieldOffset, v)

  @inline override
  def aggCount: Int = offheap.getByte(basePtr + aggCountFieldOffset).toInt

  @inline
  def aggCount_=(v: Int): Unit = offheap.putByte(basePtr + aggCountFieldOffset, v.toByte)

  @inline override
  def dimCount: Int = offheap.getByte(basePtr + dimCountFieldOffset)

  @inline
  def dimCount_=(v: Int): Unit = offheap.putByte(basePtr + dimCountFieldOffset, v.toByte)

  @inline override
  def dirty: Boolean = offheap.getByte(basePtr + dirtyFieldOffset) > 0

  @inline override
  def dirty_=(v: Boolean): Unit = offheap.putByte(basePtr + dirtyFieldOffset, if (v) 1 else 0)

  @inline override
  def link: TeslaMemoryOffset = offheap.getInt(basePtr + linkFieldOffset)

  @inline override
  def link_=(v: TeslaMemoryOffset): Unit = offheap.putInt(basePtr + linkFieldOffset, v)

  @inline override
  def isListEnd: Boolean = link == EmptyLink

  @inline override
  def dimRead(dimension: Int): BrioPrimitive = {
    offheap.getLong(
      basePtr + (valuesFieldOffset + (dimension * SizeOfLong))
    )
  }

  @inline override
  def dimWrite(dimension: Int, v: BrioPrimitive): Unit = {
    dimSetNotNull(dimension)
    offheap.putLong(
      basePtr + (valuesFieldOffset + (dimension * SizeOfLong)),
      v
    )
  }

  @inline override
  def aggRead(aggregation: Int): BrioPrimitive = {
    offheap.getLong(
      basePtr + (valuesFieldOffset + (dimCount * SizeOfLong) + (aggregation * SizeOfLong))
    )
  }

  @inline override
  def aggWrite(aggregation: Int, v: BrioPrimitive): Unit = {
    aggSetNotNull(aggregation)
    offheap.putLong(
      basePtr + valuesFieldOffset + (dimCount * SizeOfLong) + (aggregation * SizeOfLong),
      v
    )
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // INITIALIZATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline override
  def initialize(dimensions: Int, aggregations: Int): Unit = {
    assert(dimensions < 64)
    //    assert(dimensions > 0 && dimensions < 64)
    dimCount = dimensions
    assert(aggregations < 64)
    //    assert(aggregations > 0 && aggregations < 64)
    aggCount = aggregations
    dirty = false

    var d = 0
    while (d < dimCount) {
      dimWrite(d, 0L)
      d += 1
    }

    var a = 0
    while (a < aggCount) {
      aggWrite(a, 0L)
      a += 1
    }

    aggNullMap = 0L
    dimNullMap = 0L
    link = EmptyLink

  }

  @inline override
  def initializeFromKey(key: ZapCube2Key): Unit = {

    ////////////////////////////////////////////////////////////////////////////////////////////
    // set the dimensions from the key
    dimNullMap = 0L
    var d = 0
    while (d < dimCount) {
      if (key.dimIsNull(d)) dimWrite(d, 0L) else dimWrite(d, key.dimRead(d))
      d += 1
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // clear the aggregations
    aggNullMap = 0L
    var a = 0
    while (a < aggCount) {
      aggWrite(a, 0L)
      a += 1
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // now clear the next row pointer to 0
    link = EmptyLink

    ////////////////////////////////////////////////////////////////////////////////////////////
    // init the dimension nulls map to original value
    dimNullMap = key.nullMap

    ////////////////////////////////////////////////////////////////////////////////////////////
    // init the aggregation nulls map to null
    aggNullMap = 0L

  }

  @inline override
  def initializeFromRow(thatRow: ZapCube2Row): Unit = {
    ////////////////////////////////////////////////////////////////////////////////////////////
    // copy over dimensions
    dimNullMap = 0L
    var d = 0
    while (d < dimCount) {
      if (thatRow.dimIsNull(d))
        dimWrite(d, 0L)
      else
        dimWrite(d, thatRow.dimRead(d))
      d += 1
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // copy over aggregations
    aggNullMap = 0L
    var a = 0
    while (a < aggCount) {
      if (thatRow.aggIsNull(a))
        aggWrite(a, 0L)
      else
        aggWrite(a, thatRow.aggRead(a))
      a += 1
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // clear link column
    link = EmptyLink

    ////////////////////////////////////////////////////////////////////////////////////////////
    // write dimension null map to clear all inappropriate writes to dimensions
    dimNullMap = thatRow.dimNullMap

    ////////////////////////////////////////////////////////////////////////////////////////////
    // write aggregation null map to clear all inappropriate writes to aggregations
    aggNullMap = thatRow.aggNullMap

    thatRow.dirty = this.dirty

  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // KEY MATCHING
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * determine if a dimensional key matches
   *
   * @param key
   * @return
   */
  @inline override
  def matchesKey(key: ZapCube2Key): Boolean = {

    ////////////////////////////////////////////////////////////////////////////////////////////
    // first check the dimension null map
    val dimensionNullMap = dimNullMap
    val keyNullMap = key.nullMap

    // if both are all nulls then we have a match
    if (keyNullMap == 0L && dimensionNullMap == 0L)
      return true

    // if the nulls are not the same we do not have a match
    if (keyNullMap != dimensionNullMap)
      return false

    ////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * we got the nulls sorted out, lets now check the non null dimension values
     * The nulls are the same, so we can use either key or this row to test
     * for non null-ness.
     */
    var d = 0 // start at first dimension
    while (d < dimCount) {
      // only check non null fields
      if (!key.dimIsNull(d)) {
        if (key.dimRead(d) != dimRead(d)) return false
      }
      d += 1
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // its a match
    true
  }

  @inline
  def matchesRow(thatRow: ZapCube2Row): Boolean = {

    ////////////////////////////////////////////////////////////////////////////////////////////
    // check dimension nulls map for these rows...
    val thisDimensionNullsMap = dimNullMap
    val thatDimensionNullsMap = thatRow.dimNullMap

    // if both are all null then we have a match
    if (thisDimensionNullsMap == 0L && thatDimensionNullsMap == 0L)
      return true

    // if null are not equal then we do not have a match
    if (thisDimensionNullsMap != thatDimensionNullsMap)
      return false

    ////////////////////////////////////////////////////////////////////////////////////////////
    // now check each non null dimension
    var d = 0
    while (d < dimCount) {
      if (!dimIsNull(d)) {
        val thisD = dimRead(d)
        val thatD = thatRow.dimRead(d)
        if (thisD != thatD)
          return false
      }
      d += 1
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // we have a match
    true
  }


  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // NULLITY
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline override
  def aggIsNull(aggregation: Int): Boolean = {
    // 0 == NULL
    val bit: Long = 1L << aggregation.toLong
    val oldNulls: Long = aggNullMap
    val newNulls: Long = oldNulls & bit
    newNulls != bit
  }

  @inline override
  def dimIsNull(dimension: Int): Boolean = {
    // 0 == NULL
    val bit: Long = 1L << dimension.toLong
    val oldNulls: Long = dimNullMap
    val newNulls: Long = oldNulls & bit
    newNulls != bit
  }

  @inline override
  def aggSetNotNull(aggregation: Int): Unit = {
    // 0 == NULL, 1 == NOT NULL
    val bit: Long = 1L << aggregation.toLong
    val oldNulls: Long = aggNullMap
    val newNulls: Long = oldNulls | bit // set the bit
    aggNullMap = newNulls
  }

  @inline override
  def aggSetNull(aggregation: Int): Unit = {
    // 0 == NULL, 1 == NOT NULL
    val bit: Long = 1L << aggregation.toLong
    val oldNulls: Long = aggNullMap
    val newNulls: Long = oldNulls & ~bit // reset the bit
    aggNullMap = newNulls
  }

  @inline override
  def dimSetNotNull(dimension: Int): Unit = {
    // 0 == NULL, 1 == NOT NULL
    val bit: Long = 1L << dimension.toLong
    val oldNulls: Long = dimNullMap
    val newNulls: Long = oldNulls | bit // set the bit
    dimNullMap = newNulls
  }

  @inline override
  def dimSetNull(dimension: Int): Unit = {
    // 0 == NULL, 1 == NOT NULL
    val bit: Long = 1L << dimension.toLong
    val oldNulls: Long = dimNullMap
    val newNulls: Long = oldNulls & ~bit // reset the bit
    dimNullMap = newNulls
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // MISC
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def toString: String = {
    if (fauxRow) return "FAUX_ROW"
    s"ROW(basePtr=$basePtr, dirty=$dirty, link=$link, dimNullMap=$dimNullMap, dimCount=$dimCount, aggNullMap=$aggNullMap, aggCount=$aggCount, DIM(${
      (for (d <- 0 until dimCount) yield s"${dimRead(d)}").mkString(",")
    }) AGG(${
      (for (a <- 0 until aggCount) yield s"${aggRead(a)}").mkString(",")
    })"
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODEC
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def write(k: Kryo, out: Output): Unit = {
    out writeLong dimNullMap
    out writeLong aggNullMap
    out writeByte dimCount
    out writeByte aggCount
    out writeBoolean dirty
    out writeInt link
    var d = 0
    while (d < dimCount) {
      if (!dimIsNull(d))
        out writeLong dimRead(d)
      else
        out writeLong 0
      d += 1
    }
    var a = 0
    while (a < aggCount) {
      if (!aggIsNull(a))
        out writeLong aggRead(a)
      else
        out writeLong 0
      a += 1
    }
  }

  override
  def read(k: Kryo, in: Input): Unit = {
    val tDimNullMap = in.readLong
    val tAggNullMap = in.readLong
    dimCount = in.readByte
    aggCount = in.readByte
    dirty = in.readBoolean
    link = in.readInt
    var d = 0
    while (d < dimCount) {
      dimWrite(d, in.readLong)
      d += 1
    }
    dimNullMap = tDimNullMap
    var a = 0
    while (a < aggCount) {
      aggWrite(a, in.readLong)
      a += 1
    }
    aggNullMap = tAggNullMap
  }

}
