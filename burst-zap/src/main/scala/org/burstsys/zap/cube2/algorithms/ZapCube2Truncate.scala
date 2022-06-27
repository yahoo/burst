/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2.algorithms

import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive
import org.burstsys.felt.model.collectors.cube.FeltCubeBuilder
import org.burstsys.felt.model.collectors.cube.FeltCubeCollector
import org.burstsys.zap.cube2.row.ZapCube2Row
import org.burstsys.zap.cube2.state.ZapCube2State
import org.burstsys.zap.cube2.state._

/**
 * == Algorithm ==
 * This is a pseudo TopK algorithm. For the set of rows in the given Map, find the subset of '''k''' rows,
 * that have the highest value in a given aggregation column.
 * <ol>
 * <li>Take an existing cube with a set of buckets, and a set of rows.</li>
 * <li>Initialize the set of buckets to be empty.</li>
 * <li>Sort the set of rows independent of their position in the buckets and bucket lists
 * using an in-situ non-stable, quicksort algorithm reverse ordering (largest to smallest)
 * based on a single given aggregation column.</li>
 * <li>Set the row count in the cube to an integer K, where K defines the number of rows
 * to keep in the beginning of the list.</li>
 * <li>For each row, 0 through K-1, find the hash of the row's dimension keys, and link
 * the row into the appropriate bucket list. This is much like the original insertion
 * algorithm for the cube, but using existing rows already in the row set.</li>
 * </ol>
 *
 * == Notes: ==
 *
 * <ol>
 * <li>This design relies a very high row limit (large cube sizes) to accommodate finding the ``complete`` set
 * within a given Item that is then reverse sorted, and truncated before being merged with the TopK
 * from other Items, and then serialized back to the master for further merging. We do truncate the size of the cube
 * before serialization to just the final rows, so this is only a local problem.
 * </li>
 * <li>This design is a pseudo TopK in that the topK subsets are performed locally in each Item and then merged
 * with all other items. A true topK would have to calculate the complete set, return all candidate rows to the master,
 * and then sort/truncate that final set.
 * </li>
 * </ol>
 */
trait ZapCube2Truncate extends Any with ZapCube2State {

  @inline final override
  def truncateToBottomKBasedOnAggregation(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, k: Int, aggregation: Int): Unit = {

    // zero out the bucket list
    resetBuckets()

    initializeLinks()

    // reverse sort the entire row set
    sortRowsAscending(aggregation, 0, rowsCount - 1)

    // set the row count to K. This essentially throws away all rows but 0 through k-1.
    truncateRows(k)

    // now set up the buckets correctly for these rows.
    wireSubsetRowsIntoBuckets()

    // we do not need to worry about row resizing here since we only make things smaller...

  }

  @inline final override
  def truncateToTopKBasedOnAggregation(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, k: Int, aggregation: Int): Unit = {

    // zero out the bucket list
    resetBuckets()

    initializeLinks()

    // reverse sort the entire row set
    sortRowsDescending(aggregation, 0, rowsCount - 1)

    // set the row count to K. This essentially throws away all rows but 0 through k-1.
    truncateRows(k)

    // now set up the buckets correctly for these rows.
    wireSubsetRowsIntoBuckets()

    // we do not need to worry about row resizing here since we only make things smaller...

  }

  /**
   * Take the existing rows and wire them up into the correct buckets
   * and bucket lists.
   *
   */
  @inline private
  def wireSubsetRowsIntoBuckets(): Unit = {
    // link the first K rows into the bucket lists.
    var i = 0
    while (i < rowsCount) {
      val r = row(i)
      // start out with this the end of any list
      r.link = EmptyLink

      // get a key to use
      val key = pivot
      resetPivot()

      // and set its dimensions
      var d = 0
      while (d < dimCount) {
        if (!r.dimIsNull(d)) key.dimWrite(d, r.dimRead(d))
        d += 1
      }

      // now using that key, find the correct bucket and add our row in
      val index = key.bucketIndex(bucketsCount)
      bucketRead(index) match {

        // no rows in bucket yet - we put ours into the bucket and we are done
        case EmptyBucket =>
          bucketWrite(index = index, offset = rowOffset(r))

        // we have at least one row in bucket list - scan through to the end and links ours there...
        case headOffset =>
          var currentRow = ZapCube2Row(this, headOffset)
          while (!currentRow.isListEnd) currentRow = ZapCube2Row(this, currentRow.link)
          // no match in bucket list, create a new matching one and link at end of currentRow
          currentRow.link = rowOffset(r)
      }
      i += 1
    }
  }

  /**
   * we want to clear out any previous row linkages since we are about to
   * invalidate the whole bucket list reference system.
   */
  @inline private
  def initializeLinks(): Unit = {
    var i = 0
    while (i < rowsCount) {
      row(i).link = EmptyLink
      i += 1
    }
  }


  /**
   * forward quicksort a cubes rows
   *
   * @param aggIndex
   * @param lowerIndex
   * @param higherIndex
   */
  @inline final private
  def sortRowsAscending(aggIndex: Int, low: Int, high: Int): Unit = {
    if (low >= 0 && high > low) {
      val pivotIndex = partition(aggIndex, low, high)
      sortRowsAscending(aggIndex, low, pivotIndex)
      sortRowsAscending(aggIndex, pivotIndex + 1, high)
    }
  }

  /**
   * reverse quicksort a cube's rows.
   *
   * @param aggIndex
   * @param low
   * @param high
   */
  @inline final private
  def sortRowsDescending(aggIndex: Int, low: Int, high: Int): Unit = {
    if (low >= 0 && high > low) {
      val pivotIndex = partition(aggIndex, low, high, ascending = false)
      sortRowsDescending(aggIndex, low, pivotIndex)
      sortRowsDescending(aggIndex, pivotIndex + 1, high)
    }
  }

  private def partition(aggIndex: Int, low: Int, high: Int, ascending: Boolean = true): Int = {
    val pivotValue = row((high + low) / 2).aggRead(aggIndex)

    var i = low - 1
    var j = high + 1

    while (true) {
      do i += 1 while (
        if (ascending) row(i).aggRead(aggIndex) < pivotValue
        else row(i).aggRead(aggIndex) > pivotValue
      )
      do j -= 1 while (
        if (ascending) row(j).aggRead(aggIndex) > pivotValue
        else row(j).aggRead(aggIndex) < pivotValue
      )

      if (i >= j) return j
      else swap(i, j)
    }
    throw new IllegalStateException("Cube partition failed!")
  }

  // swap two rows field by field
  private def swap(left: Int, right: Int): Unit = {
    if (left == right) return

    // swap one by one each field in the left row with the same field in the right row
    val leftRow = row(left)
    val rightRow = row(right)

    // swap dimensions
    var d = 0
    while (d < dimCount) {

      val leftIsNull = leftRow.dimIsNull(d)
      val rightIsNull = rightRow.dimIsNull(d)

      val leftValue = leftRow.dimRead(d)
      val rightValue = rightRow.dimRead(d)

      if (rightIsNull) leftRow.dimSetNull(d) else leftRow.dimWrite(d, rightValue)
      if (leftIsNull) rightRow.dimSetNull(d) else rightRow.dimWrite(d, leftValue)

      d += 1
    }

    // swap aggregations
    var a = 0
    while (a < aggCount) {

      val leftIsNull = leftRow.aggIsNull(a)
      val rightIsNull = rightRow.aggIsNull(a)

      val leftValue = leftRow.aggRead(a)
      val rightValue = rightRow.aggRead(a)

      if (rightIsNull) leftRow.aggSetNull(a) else leftRow.aggWrite(a, rightValue)
      if (leftIsNull) rightRow.aggSetNull(a) else rightRow.aggWrite(a, leftValue)

      a += 1
    }
  }

}
