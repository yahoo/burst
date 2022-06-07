/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube.state

import org.burstsys.fabric.execution.model.result.row.FabricDataKeyAnyVal
import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubeCollector}
import org.burstsys.zap.cube
import org.burstsys.zap.cube.{ZapCubeRow, _}

/**
 * Manage creating, finding, and cloning rows.
 */
trait ZapCubeRower extends Any with ZapCube {

  def lastRow_=(s: Int): Unit

  /**
   * this is the row for a given row index. This is a row that is an offset
   *
   * @param rowIndex
   * @return
   */
  @inline final override
  def row(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, rowIndex: Int): ZapCubeRow = {
    val offset = rowOffset(builder.asInstanceOf[ZapCubeBuilder], rowIndex)
    ZapCubeRow(offset)
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // IMPLEMENTATION
  //////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * The offset from the beginning of the memory block for a row. This is the address kept in a row itself
   *
   * @param row
   * @return
   */
  @inline private[zap]
  def rowOffset(builder: ZapCubeBuilder, row: Int): ZapMemoryOffset = rowBlockOffset + (builder.rowSize * row)

  /**
   *
   * @param key
   * @return
   */
  @inline private[zap]
  def createNewRow(builder: ZapCubeBuilder, thisCube: ZapCubeContext, key: FabricDataKeyAnyVal): ZapCubeRow = {
    val r = newRow(builder, thisCube)
    if (rowLimited) r else r.initializeToDimensionKey(builder, thisCube, key)
  }

  @inline private[zap]
  def cloneRowFromAnotherCube(builder: ZapCubeBuilder, thisCube: ZapCubeContext, thatCube: ZapCubeContext, thatRow: ZapCubeRow): ZapCubeRow = {
    val r = newRow(builder, thisCube)
    if (rowLimited) r else r.copyDimensionAndAggregationValuesFrom(builder, thisCube, thatCube, thatRow)
  }

  @inline private
  def newRow(builder: ZapCubeBuilder, thisCube: ZapCubeContext): ZapCubeRow = {
    if (rowCount + 1 > builder.rowLimit) {
      rowLimited = true
      ZapCubeRow()
    } else {
      lastRow = lastRow + 1
      row(builder, thisCube, lastRow)
    }
  }

}
