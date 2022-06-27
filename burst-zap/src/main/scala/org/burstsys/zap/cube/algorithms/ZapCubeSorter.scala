/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube.algorithms

import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive
import org.burstsys.zap.cube._

trait ZapCubeSorter extends Any {

  /**
   * forward quicksort a cubes rows
   *
   * @param aggregationIndex
   * @param low
   * @param high
   */
  @inline final
  def sortRowsAscending(builder: ZapCubeBuilder, thisCube: ZapCubeContext, aggregationIndex: Int, low: Int, high: Int): Unit = {
    if (low >= 0 && high > low) {
      val pivotIndex = partition(builder, thisCube, aggregationIndex, low, high)
      sortRowsAscending(builder, thisCube, aggregationIndex, low, pivotIndex)
      sortRowsAscending(builder, thisCube, aggregationIndex, pivotIndex + 1, high)
    }
  }

  /**
   * reverse quicksort a cube's rows.
   *
   * @param aggregationIndex
   * @param low
   * @param high
   */
  @inline final
  def sortRowsDescending(builder: ZapCubeBuilder, thisCube: ZapCubeContext, aggregationIndex: Int, low: Int, high: Int): Unit = {
    if (low >= 0 && high > low) {
      val pivotIndex = partition(builder, thisCube, aggregationIndex, low, high, ascending = false)
      sortRowsDescending(builder, thisCube, aggregationIndex, low, pivotIndex)
      sortRowsDescending(builder, thisCube, aggregationIndex, pivotIndex + 1, high)
    }

  }

  private def partition(builder: ZapCubeBuilder, thisCube: ZapCubeContext, aggregationIndex: Int, low: Int, high: Int, ascending: Boolean = true): Int = {
    val pivotValue = valueForRow(builder, thisCube, aggregationIndex, (high + low) / 2)

    var i = low - 1
    var j = high + 1

    while (true) {
      do i += 1 while (
        if (ascending) valueForRow(builder, thisCube, aggregationIndex, i) < pivotValue
        else valueForRow(builder, thisCube, aggregationIndex, i) > pivotValue
      )
      do j -= 1 while (
        if (ascending) valueForRow(builder, thisCube, aggregationIndex, j) > pivotValue
        else valueForRow(builder, thisCube, aggregationIndex, j) < pivotValue
      )
      if (i >= j) return j
      swapRows(builder, thisCube, i, j)
    }
    throw new IllegalStateException("Cube partition failed!")
  }

  private def valueForRow(builder: ZapCubeBuilder, thisCube: ZapCubeContext, aggregationIndex: ZapCubeDimensionKey, i: ZapCubeDimensionKey): BrioPrimitive = {
    thisCube.row(builder, thisCube, i).readRowAggregationPrimitive(builder, thisCube, aggregationIndex)
  }

  private def swapRows(builder: ZapCubeBuilder, thisCube: ZapCubeContext, left: Int, right: Int): Unit = {
    if (left == right) return

    // swap one by one each field in the left row with the same field in the right row
    val leftRow = thisCube.row(builder, thisCube, left)
    val rightRow = thisCube.row(builder, thisCube, right)

    // swap dimensions
    var d = 0
    while (d < builder.dimensionCount) {

      val leftIsNull = leftRow.readRowDimensionIsNull(builder, thisCube, d)
      val rightIsNull = rightRow.readRowDimensionIsNull(builder, thisCube, d)

      val leftValue = leftRow.readRowDimensionPrimitive(builder, thisCube, d)
      val rightValue = rightRow.readRowDimensionPrimitive(builder, thisCube, d)

      if (rightIsNull) leftRow.writeRowDimensionIsNull(builder, thisCube, d) else leftRow.writeRowDimensionPrimitive(builder, thisCube, d, rightValue)
      if (leftIsNull) rightRow.writeRowDimensionIsNull(builder, thisCube, d) else rightRow.writeRowDimensionPrimitive(builder, thisCube, d, leftValue)

      d += 1
    }

    // swap aggregations
    var a = 0
    while (a < builder.aggregationCount) {

      val leftIsNull = leftRow.readRowAggregationIsNull(builder, thisCube, a)
      val rightIsNull = rightRow.readRowAggregationIsNull(builder, thisCube, a)

      val leftValue = leftRow.readRowAggregationPrimitive(builder, thisCube, a)
      val rightValue = rightRow.readRowAggregationPrimitive(builder, thisCube, a)

      if (rightIsNull) leftRow.writeRowAggregationIsNull(builder, thisCube, a) else leftRow.writeRowAggregationPrimitive(builder, thisCube, a, rightValue)
      if (leftIsNull) rightRow.writeRowAggregationIsNull(builder, thisCube, a) else rightRow.writeRowAggregationPrimitive(builder, thisCube, a, leftValue)

      a += 1
    }
  }
}
