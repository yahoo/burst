/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2

import org.burstsys.tesla.TeslaTypes._

package object state {
  final val EmptyBucket: TeslaMemoryOffset = 0

  final val EmptyLink: TeslaMemoryOffset = 0

  /**
   * return the byte size required for a key based on the number of dimensions
   *
   * @param dimensions
   * @return
   */
  def byteSize(dimensions: Int): TeslaMemorySize = SizeOfLong + SizeOfByte + (dimensions * SizeOfLong)

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // POOL
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * store the pool id at the very beginning
   */
  private[state] final val poolIdFieldOffset: TeslaMemoryOffset = 0 // Int

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Dim and Agg counts
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * store the number of dimensions
   */
  private[state] final val dimCountFieldOffset: TeslaMemoryOffset = poolIdFieldOffset + SizeOfInteger // Int

  /**
   * store the number of aggregations
   */
  private[state] final val aggCountFieldOffset: TeslaMemoryOffset = dimCountFieldOffset + SizeOfInteger // Int

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BUCKETS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * store the offset of the bucket block
   */
  private[state] final val bucketsStartFieldOffset: TeslaMemoryOffset = aggCountFieldOffset + SizeOfInteger // Int

  /**
   * store the numbers of buckets
   */
  private[state] final val bucketsCountFieldOffset: TeslaMemoryOffset = bucketsStartFieldOffset + SizeOfInteger // Int

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CURSOR
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * store the offset of the cursor block
   */
  private[state] final val cursorStartFieldOffset: TeslaMemoryOffset = bucketsCountFieldOffset + SizeOfInteger // Int

  /**
   * the cursor updated field
   */
  private[state] final val cursorUpdatedFieldOffset: TeslaMemoryOffset = cursorStartFieldOffset + SizeOfInteger // Int

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PIVOT
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * store the offset of the cursor block
   */
  private[state] final val pivotStartFieldOffset: TeslaMemoryOffset = cursorUpdatedFieldOffset + SizeOfInteger // Int

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // ROWS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * size of the row
   */
  private[state] final val rowSizeFieldOffset: TeslaMemoryOffset = pivotStartFieldOffset + SizeOfInteger // Int

  /**
   * store the offset of the row block
   */
  private[state] final val rowsStartFieldOffset: TeslaMemoryOffset = rowSizeFieldOffset + SizeOfInteger // Int

  /**
   * store the offset of the end of current row block next
   */
  private[state] final val rowsEndFieldOffset: TeslaMemoryOffset = rowsStartFieldOffset + SizeOfInteger // Int

  /**
   * current count of rows
   */
  private[state] final val rowsCountFieldOffset: TeslaMemoryOffset = rowsEndFieldOffset + SizeOfInteger // Int

  /**
   * number of resize cycles
   */
  private[state] final val resizeCountFieldOffset: TeslaMemoryOffset = rowsCountFieldOffset + SizeOfInteger // Int

  /**
   * store the start of the row block next
   */
  private[state] final val rowsLimitedFieldOffset: TeslaMemoryOffset = resizeCountFieldOffset + SizeOfInteger // Int

  /**
   * store the start of the row pointed at by the cursor
   */
  private[state] final val cursorRowFieldOffset: TeslaMemoryOffset = rowsLimitedFieldOffset + SizeOfInteger // Int

  /**
   * the [[org.burstsys.brio.dictionary.flex.BrioFlexDictionary]] pointer
   */
  private[state] final val dictionaryFieldOffset: TeslaMemoryOffset = cursorRowFieldOffset + SizeOfInteger // Int

  /**
   * size of the fixed size header / start of variable size data
   */
  private[state] final val endOfFixedSizeHeader: TeslaMemoryOffset = dictionaryFieldOffset + SizeOfInteger

}
