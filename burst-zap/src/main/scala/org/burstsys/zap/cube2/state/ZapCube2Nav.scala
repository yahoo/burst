/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2.state

import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive
import org.burstsys.zap.cube2.key.ZapCube2Key
import org.burstsys.zap.cube2.row.{ZapCube2Row, ZapCube2RowAnyVal, limitExceededMarkerRow}
import org.burstsys.zap.cube2.{ZapCube2, ZapCube2AggregationAxis, ZapCube2DimensionAxis}

/**
 * The ''navigation'' operations associated with a [[ZapCube2]]. This includes all dimension and
 * aggregation operations, cursor management, and row creation.
 */
trait ZapCube2Nav extends Any with ZapCube2State with ZapCube2DimensionAxis with ZapCube2AggregationAxis {

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Dimensions
  //////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def dimIsNull(dimension: Int): Boolean = cursor.dimIsNull(dimension)

  @inline final override
  def dimSetNull(dimension: Int): Unit = {
    cursor.dimSetNull(dimension)
    cursorUpdated = true
  }

  @inline final override
  def dimSetNotNull(dimension: Int): Unit = {
    cursor.dimSetNotNull(dimension)
    cursorUpdated = true
  }

  @inline final override
  def dimRead(dimension: Int): BrioPrimitive = {
    cursor.dimRead(dimension)
  }

  @inline final override
  def dimWrite(dimension: Int, value: BrioPrimitive): Unit = {
    cursor.dimWrite(dimension, value)
    cursorUpdated = true
  }

  @inline final override
  def dimWrite(): Unit = {
    navigate()
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Aggregations
  //////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def aggIsNull(aggregation: Int): Boolean = {
    navigate()
    ZapCube2RowAnyVal(basePtr + cursorRow).aggIsNull(aggregation)
  }

  @inline final override
  def aggSetNull(aggregation: Int): Unit = {
    navigate()
    if (!rowsLimited)
      ZapCube2RowAnyVal(basePtr + cursorRow).aggSetNull(aggregation)
  }

  @inline final override
  def aggSetNotNull(aggregation: Int): Unit = {
    navigate()
    if (!rowsLimited)
      ZapCube2RowAnyVal(basePtr + cursorRow).aggSetNotNull(aggregation)
  }

  @inline final override
  def aggRead(aggregation: Int): BrioPrimitive = {
    navigate()
    ZapCube2RowAnyVal(basePtr + cursorRow).aggRead(aggregation)
  }

  @inline final override
  def aggWrite(aggregation: Int, value: BrioPrimitive): Unit = {
    navigate()
    if (!rowsLimited)
      ZapCube2RowAnyVal(basePtr + cursorRow).aggWrite(aggregation, value)
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Navigation internals
  //////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Navigate if the cursor has been updated since the last dimension update
   *
   * @return
   */
  @inline final override
  def navigate(): Unit = {
    if (cursorUpdated) {
      val row = navigate(cursor)
      if (row != limitExceededMarkerRow) {
        cursorRow = rowOffset(row)
        cursorUpdated = false
      }
    }
  }

  /**
   * navigate to a row that matches the dimensional key or create a row with a matching key and insert at end of
   * appropriate cube bucket list
   *
   * @param key
   * @return the row found or created
   */
  @inline final override
  def navigate(key: ZapCube2Key): ZapCube2Row = {
    // figure out which bucket this key maps to
    val index = key.bucketIndex(bucketsCount)

    // and see whats in the associated bucket list
    bucketRead(index) match {
      // no rows in bucket yet - we create new matching one, add to bucket and return
      case EmptyBucket =>
        val newRow = createNewRowFromKey(key)
        // as always, check for a out of room condition
        if (newRow == limitExceededMarkerRow)
          return newRow
        // update bucket with a valid first row in list
        bucketWrite(index, rowOffset(newRow))
        newRow

      // we have at least one row in bucket list - scan through and see if we have a match...
      case firstRowOffset =>
        navigateToRowInBucket(key, ZapCube2RowAnyVal(basePtr + firstRowOffset))

    }
  }

  /**
   *
   * @param key
   * @param firstRow
   * @return
   */
  @inline final private
  def navigateToRowInBucket(key: ZapCube2Key, firstRow: ZapCube2Row): ZapCube2Row = {
    // start at the beginning
    var currentRow = firstRow
    var more = true

    // iterate down the list
    while (more) {
      // any joy?
      if (currentRow.matchesKey(key))
        return currentRow

      // see if there are more in the list
      more = !currentRow.isListEnd
      // if so, transition to the next one in list
      if (more)
        currentRow = ZapCube2RowAnyVal(basePtr + currentRow.link)
    }
    // no match in bucket list, create a new matching one and link at end of currentRow
    val newRow = createNewRowFromKey(key)

    // link it in unless we ran out of room...
    if (newRow != limitExceededMarkerRow)
      currentRow.link = rowOffset(newRow)

    newRow
  }

}
