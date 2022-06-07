/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube.algorithms

import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubeCollector}
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.FeltCubeAggSemRt
import org.burstsys.vitals.bitmap.VitalsBitMapAnyVal
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.zap.cube
import org.burstsys.zap.cube.{ZapCubeRow, _}

/**
 * ==Zap Cube Merge==
 * A Zap Cube merge between CUBE1 and CUBE2 is much like a FULL OUTER JOIN in SQL  ON CUBE1.dimensions == CUBE2.dimensions
 * except for:
 * {{{
 *   1. The merge is of CUBE2 into CUBE1 - the result changes CUBE2 it does not create a third cube.
 *   2. Null dimensions are allowed
 *      a. null != non-null
 *      b. null == null.
 *   3. matching rows have their aggregations 'merged'.
 * }}}
 * ==Bucket Partitioned==
 * Because this is already a bucket partitioned store based on the dimensions, we can perform this merge using the same
 * buckets. This reduces time complexity O(n2) => O(c)
 */
trait ZapCubeMerger extends Any with ZapCube {

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * an inter merge is done across items out side of a traversal
   *
   * @param thatCube
   */
  @inline final override
  def interMerge(builder: FeltCubeBuilder,
                 thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary,
                 thatCube: FeltCubeCollector, thatDictionary: BrioMutableDictionary): Unit = {
    merge(
      builder = builder.asInstanceOf[ZapCubeBuilder],
      thisCube = thisCube.asInstanceOf[ZapCubeContext], thisDictionary = thisDictionary,
      thatCube = thatCube.asInstanceOf[ZapCubeContext], thatDictionary = thatDictionary,
      dimensionMask = VitalsBitMapAnyVal(~0L), aggregationMask = VitalsBitMapAnyVal(~0L),
      intra = false
    )
  }

  /**
   * an intra merge is done within an item traversal
   *
   * @param thatCube
   */
  @inline final override
  def intraMerge(builder: FeltCubeBuilder,
                 thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary,
                 thatCube: FeltCubeCollector, thatDictionary: BrioMutableDictionary,
                 dimensionMask: VitalsBitMapAnyVal, aggregationMask: VitalsBitMapAnyVal): Unit = {
    merge(
      builder = builder.asInstanceOf[ZapCubeBuilder],
      thisCube = thisCube.asInstanceOf[ZapCubeContext], thisDictionary = thisDictionary,
      thatCube = thatCube.asInstanceOf[ZapCubeContext], thatDictionary = thatDictionary,
      dimensionMask = dimensionMask, aggregationMask = aggregationMask,
      intra = true
    )
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // IMPLEMENTATION
  //////////////////////////////////////////////////////////////////////////////////////////////////

  @inline private
  def merge(builder: ZapCubeBuilder,
            thisCube: ZapCubeContext, thisDictionary: BrioMutableDictionary,
            thatCube: ZapCubeContext, thatDictionary: BrioMutableDictionary,
            dimensionMask: VitalsBitMapAnyVal, aggregationMask: VitalsBitMapAnyVal,
            intra: Boolean): Unit = {
    if (thisDictionary != thatDictionary)
      throw VitalsException(s" Merge must have identical dictionaries!")

    // if we have overflow, then bail TODO: do we need to do more here???
    if (thisDictionary.overflowed || thatDictionary.overflowed)
      return

    val thatRows = thatCube.rowCount
    // if the incoming cube is empty - nothing to do
    if (thatRows == 0) return
    // now we have to lookup
    var b = 0
    while (b < thisCube.bucketCount) {
      // for each bucket in each cube, there are one of four states...
      val thisBucket = thisCube.bucket(builder, thisCube, b)
      val thatBucket = thatCube.bucket(builder, thatCube, b)
      if (thisBucket == ZapCubeEmptyBucket && thatBucket == ZapCubeEmptyBucket) {
        // state 1: nothing in either bucket so nothing to do
      } else if (thisBucket != ZapCubeEmptyBucket && thatBucket == ZapCubeEmptyBucket) {
        // state 2: something in this bucket but nothing in that bucket so nothing to do
      } else if (thisBucket == ZapCubeEmptyBucket && thatBucket != ZapCubeEmptyBucket) {
        // state 3: nothing in current bucket and rows in other bucket so we simply need to import all the 'other' rows
        importEntireOtherBucket(builder, thisCube, b, thatCube, cube.ZapCubeRow(thatBucket))
        if (thisCube.rowLimited) return
      } else {
        // state 4: something in both buckets so we need to merge the rows in both
        mergeOtherRows(
          builder, thisCube, b, dimensionMask, aggregationMask,
          cube.ZapCubeRow(thisBucket), thatCube, cube.ZapCubeRow(thatBucket), intra
        )
        if (thisCube.rowLimited) return
      }
      b += 1
    }
  }

  /**
   * import all rows from a bucket in another cube into our empty bucket. This is a simple matter
   * of copying the rows into the current cube and inserting them into the empty bucket.
   *
   * @param bucket
   * @param thatCube
   * @param thatRow
   */
  @inline private
  def importEntireOtherBucket(builder: ZapCubeBuilder, thisCube: ZapCubeContext,
                              bucket: Int, thatCube: ZapCubeContext, thatRow: ZapCubeRow): Unit = {
    var currentThatRow = thatRow
    var clone = thisCube.cloneRowFromAnotherCube(builder, thisCube, thatCube, currentThatRow)
    if (thisCube.rowLimited) return
    var newRow = clone
    thisCube.setBucket(builder, thisCube, bucket, newRow.rowStartOffset)
    var count = 0
    while (currentThatRow.hasLinkRow(builder, thatCube)) {
      currentThatRow = currentThatRow.linkRow(builder, thatCube)
      clone = thisCube.cloneRowFromAnotherCube(builder, thisCube, thatCube, currentThatRow)
      if (thisCube.rowLimited) return
      newRow.setLinkRow(builder, thisCube, clone)
      newRow = clone
      count += 1
    }
  }

  /**
   * merge all rows from a bucket in another cube with the rows in our full bucket. We scan each row set and
   * put the combinations into the three types of buckets.
   * {{{
   * 1) existing rows that match incoming rows - merge each incoming row into its matching existing row
   * 2) existing rows that don't match incoming row - do nothing
   * 3) incoming rows that don't match existing row - insert incoming rows into current bucket
   * }}}
   * NOTE: though this part is a N^2^ time complexity i.e. two nested loops - we are bucket partitioned (log N)
   * and super fast in our bad ass off heap unsafe memory and more importantly we are generating zero object
   * allocations
   *
   * @param bucketIndex
   * @param thisRow
   * @param thatRow
   */
  @inline private
  def mergeOtherRows(builder: ZapCubeBuilder, thisCube: ZapCubeContext,
                     bucketIndex: Int, dimensionMask: VitalsBitMapAnyVal, aggregationMask: VitalsBitMapAnyVal,
                     thisRow: ZapCubeRow, thatCube: ZapCubeContext, thatRow: ZapCubeRow, intra: Boolean): Unit = {

    /**
     * scan all existing rows for matches with incoming rows - matches we merge, non matches we do nothing - they
     * are in the right place already
     */
    var existingRow = thisRow
    do {
      var incomingRow = thatRow
      do {
        mergeRow(
          builder = builder, thisCube = thisCube, bucketIndex = bucketIndex,
          dimensionMask = dimensionMask, aggregationMask = aggregationMask,
          thatCube = thatCube, thisRow = existingRow, thatRow = incomingRow, intra = intra
        )
        incomingRow = incomingRow.linkRow(builder, thatCube)
      } while (incomingRow.validRow)
      existingRow = existingRow.linkRow(builder, thisCube)
    } while (existingRow.validRow)

    /**
     * now scan all incoming rows for matches with existing rows - matches we ignore (they have been merged already)
     * non matches we import since these have not matched with existing rows and so are bonafide new keys
     * It is important for multiple reasons to link in rows at the beginning of the list. This allows us
     * always scan forward without facing a change list, and to be able to get to pointers efficiently (the head
     * of the bucket list is always stored in a known place)
     */
    var row = thatRow
    do {
      importRow(builder, thisCube, bucketIndex, thatCube, row)
      if (thisCube.rowLimited) return
      row = row.linkRow(builder, thatCube)
    } while (row.validRow)
  }

  // TODO REMOVE bucketIndex and dimensionMask
  @inline private
  def mergeRow(builder: ZapCubeBuilder, thisCube: ZapCubeContext, bucketIndex: Int,
               dimensionMask: VitalsBitMapAnyVal, aggregationMask: VitalsBitMapAnyVal,
               thatCube: ZapCubeContext, thisRow: ZapCubeRow, thatRow: ZapCubeRow, intra: Boolean): Unit = {
    // if these two match then merge the rows.
    if (thisRow.matchesRowDimensionKeyInAnotherMap(builder, thisCube, thatCube, thatRow)) {
      // merge the rows for this we require aggregation semantics
      var a = 0
      while (a < builder.aggregationCount) {
        // make sure that we do not aggregate for fields in other gathers
        if (aggregationMask.testBit(a)) {
          val semantic: FeltCubeAggSemRt = builder.aggregationSemantics(a)
          if (!thatRow.readRowAggregationIsNull(builder, thatCube, a)) {
            val oldValue = thisRow.readRowAggregationPrimitive(builder, thisCube, a)
            val newValue = thatRow.readRowAggregationPrimitive(builder, thatCube, a)
            val result = builder.aggregationFieldTypes(a) match {
              case BrioBooleanKey => semantic.doBoolean(oldValue, newValue, intra)
              case BrioByteKey => semantic.doByte(oldValue, newValue, intra)
              case BrioShortKey => semantic.doShort(oldValue, newValue, intra)
              case BrioIntegerKey => semantic.doInteger(oldValue, newValue, intra)
              case BrioLongKey => semantic.doLong(oldValue, newValue, intra)
              case BrioDoubleKey => semantic.doDouble(oldValue, newValue, intra)
              case BrioStringKey => semantic.doString(oldValue, newValue, intra)
            }
            thisRow.writeRowAggregationPrimitive(builder, thisCube, a, result)
          }
        }
        a += 1
      }
    }
  }

  @inline private
  def importRow(builder: ZapCubeBuilder, thisCube: ZapCubeContext, bucketIndex: Int,
                thatCube: ZapCubeContext, thatRow: ZapCubeRow): Unit = {
    /**
     * yes for now we have to scan this over and over again - generally this is ok - bucket lists are short
     * and we are fast  and its more important to get it working and not allocate any objects. However we
     * could figure out how to store these without creating ephemeral objects soon.
     */
    var foundMatch = false
    var thisRow = thisCube.bucketHead(builder, thisCube, bucketIndex)
    while (thisRow != cube.ZapCubeRow() && !foundMatch) {
      foundMatch = thisRow.matchesRowDimensionKeyInAnotherMap(builder, thisCube, thatCube, thatRow)
      thisRow = thisRow.linkRow(builder, thisCube)
    }
    if (!foundMatch) {
      val bucketListHead = thisCube.bucketHead(builder, thisCube, bucketIndex)
      val clone = thisCube.cloneRowFromAnotherCube(builder, thisCube, thatCube, thatRow)
      if (thisCube.rowLimited) return
      clone.setLinkRow(builder, thisCube, bucketListHead)
      thisCube.setBucket(builder, thisCube, bucketIndex, clone.rowStartOffset)
    }
  }

}
