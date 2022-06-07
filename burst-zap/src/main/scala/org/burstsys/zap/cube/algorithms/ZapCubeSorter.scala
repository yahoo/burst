/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube.algorithms

import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive
import org.burstsys.zap.cube._

trait ZapCubeSorter extends Any {

  /**
   * forward quicksort a cubes rows
   *
   * @param aggregation
   * @param lowerIndex
   * @param higherIndex
   */
  @inline final
  def sortRowsAscending(builder: ZapCubeBuilder, thisCube: ZapCubeContext, aggregation: Int, lowerIndex: Int, higherIndex: Int): Unit = {
    var i = lowerIndex
    var j = higherIndex

    // get pivot row index
    val pivotKeyPoint = lowerIndex + ((higherIndex - lowerIndex) / 2)

    // and the associated row
    val pivotRow = thisCube.row(builder, thisCube, pivotKeyPoint)

    // this is the value for the chosen aggregation in that row
    val pivotValue: BrioPrimitive = pivotRow.readRowAggregationPrimitive(builder, thisCube, aggregation)

    // swap two rows field by field
    def swap(leftCursor: Int, rightCursor: Int): Unit = {
      if (leftCursor == rightCursor) return

      // swap one by one each field in the left row with the same field in the right row
      val leftRow = thisCube.row(builder, thisCube, leftCursor)
      val rightRow = thisCube.row(builder, thisCube, rightCursor)

      // swap dimensions
      {
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
      }

      // swap aggregations
      {
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

    def iValue: BrioPrimitive = thisCube.row(builder, thisCube, i).readRowAggregationPrimitive(builder, thisCube, aggregation)

    def jValue: BrioPrimitive = thisCube.row(builder, thisCube, j).readRowAggregationPrimitive(builder, thisCube, aggregation)

    while (i <= j) {
      while (iValue < pivotValue) i += 1
      while (jValue > pivotValue) j -= 1
      if (i <= j) {
        swap(i, j)
        i += 1
        j -= 1
      }
    }

    if (lowerIndex < j) {
      sortRowsAscending(builder, thisCube, aggregation, lowerIndex, j)
    }
    if (i < higherIndex) {
      sortRowsAscending(builder, thisCube, aggregation, i, higherIndex)
    }
  }

  /**
   * reverse quicksort a cube's rows.
   *
   * @param aggregation
   * @param lowerIndex
   * @param higherIndex
   */
  @inline final
  def sortRowsDescending(builder: ZapCubeBuilder, thisCube: ZapCubeContext, aggregation: Int, lowerIndex: Int, higherIndex: Int): Unit = {
    var i = lowerIndex
    var j = higherIndex

    // get pivot row index
    val pivotKeyPoint = lowerIndex + ((higherIndex - lowerIndex) / 2)

    // and the associated row
    val pivotRow = thisCube.row(builder, thisCube, pivotKeyPoint)

    // this is the value for the chosen aggregation in that row
    val pivotValue: BrioPrimitive = pivotRow.readRowAggregationPrimitive(builder, thisCube, aggregation)

    // swap two rows field by field
    def swap(leftCursor: Int, rightCursor: Int): Unit = {
      if (leftCursor == rightCursor) return

      // swap one by one each field in the left row with the same field in the right row
      val leftRow = thisCube.row(builder, thisCube, leftCursor)
      val rightRow = thisCube.row(builder, thisCube, rightCursor)

      // swap dimensions
      {
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
      }

      // swap aggregations
      {
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

    def iValue: BrioPrimitive = thisCube.row(builder, thisCube, i).readRowAggregationPrimitive(builder, thisCube, aggregation)

    def jValue: BrioPrimitive = thisCube.row(builder, thisCube, j).readRowAggregationPrimitive(builder, thisCube, aggregation)

    while (i <= j) {
      while (iValue > pivotValue) i += 1
      while (jValue < pivotValue) j -= 1
      if (i <= j) {
        swap(i, j)
        i += 1
        j -= 1
      }
    }

    if (lowerIndex < j) {
      sortRowsDescending(builder, thisCube, aggregation, lowerIndex, j)
    }
    if (i < higherIndex) {
      sortRowsDescending(builder, thisCube, aggregation, i, higherIndex)
    }
  }


}
