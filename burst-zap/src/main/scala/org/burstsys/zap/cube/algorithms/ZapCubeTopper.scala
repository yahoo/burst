/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube.algorithms

import org.burstsys.fabric.execution.model.result.row.FabricDataKeyAnyVal
import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubeCollector}
import org.burstsys.zap.cube
import org.burstsys.zap.cube._

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
 * <li>This design relies on a very high row limit (large cube sizes) to accommodate finding the ``complete`` set
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
trait ZapCubeTopper extends Any with ZapCubeSorter with ZapCube {

  ///////////////////////////////////////////
  //  API
  ///////////////////////////////////////////

  @inline final override
  def truncateToTopKBasedOnAggregation(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, k: Int, aggregation: Int): Unit = {

    val zb = builder.asInstanceOf[ZapCubeBuilder]
    val tc = thisCube.asInstanceOf[ZapCubeContext]

    // zero out the bucket list
    tc.initBuckets(tc.cubeDataStart)

    initializeLinks(zb, tc)

    // reverse sort the entire row set
    sortRowsDescending(zb, tc, aggregation, 0, rowCount - 1)

    // set the row count to K. This essentially throws away all rows but 0 through k-1.
    rowCount_=(math.min(rowCount, k))

    // now set up the buckets correctly for these rows.
    wireSubsetRowsIntoBuckets(zb, tc)

  }

  @inline final override
  def truncateToBottomKBasedOnAggregation(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, k: Int, aggregation: Int): Unit = {

    val zb = builder.asInstanceOf[ZapCubeBuilder]
    val tc = thisCube.asInstanceOf[ZapCubeContext]

    // zero out the bucket list
    tc.initBuckets(tc.cubeDataStart)

    initializeLinks(zb, tc)

    // reverse sort the entire row set
    sortRowsAscending(zb, tc, aggregation, 0, rowCount - 1)

    // set the row count to K. This essentially throws away all rows but 0 through k-1.
    rowCount_=(math.min(rowCount, k))

    // now set up the buckets correctly for these rows.
    wireSubsetRowsIntoBuckets(zb, tc)

  }

  ///////////////////////////////////////////
  //  IMPLEMENTATION
  ///////////////////////////////////////////

  /**
   * Take the existing rows and wire them up into the correct buckets
   * and bucket lists.
   *
   */
  @inline private
  def wireSubsetRowsIntoBuckets(builder: ZapCubeBuilder, thisCube: ZapCubeContext): Unit = {
    // link the first K rows into the bucket lists.
    var i = 0
    while (i < rowCount) {
      val r = thisCube.row(builder, thisCube, i)
      // start out with this as end of any list
      r.setLinkField(builder, thisCube, ZapCubeEmptyLink)

      // get a key to use
      val key = FabricDataKeyAnyVal(freshKeyData)

      // and set its dimensions
      var d = 0
      while (d < builder.dimensionCount) {
        if (!r.readRowDimensionIsNull(builder, thisCube, d)) {
          val value = r.readRowDimensionPrimitive(builder, thisCube, d)
          key.writeKeyDimensionPrimitive(d, value)
        }
        d += 1
      }

      // now using that key, find the correct bucket and add our row in
      val hashCode = thisCube.hash(builder, thisCube, key)
      thisCube.bucket(builder, thisCube, hashCode) match {

        // no rows in bucket yet - we put ours into the bucket and we are done
        case ZapCubeEmptyBucket => thisCube.setBucket(builder, thisCube, hashCode, r.rowStartOffset)

        // we have at least one row in bucket list - scan through to the end and links ours there...
        case startPtr =>
          var currentRow = cube.ZapCubeRow(startPtr)
          while (currentRow.hasLinkRow(builder, thisCube)) {
            currentRow = currentRow.linkRow(builder, thisCube)
          }
          // no match in bucket list, create a new matching one and link at end of currentRow
          currentRow.setLinkRow(builder, thisCube, r)
      }
      i += 1
    }
  }

  /**
   * we want to clear out any previous row linkages since we are about to
   * invalidate the whole bucket list reference system.
   */
  @inline private
  def initializeLinks(builder: ZapCubeBuilder, thisCube: ZapCubeContext): Unit = {
    var i = 0
    while (i < rowCount) {
      thisCube.row(builder, thisCube, i).setLinkField(builder, thisCube, ZapCubeEmptyLink)
      i += 1
    }
  }

}
