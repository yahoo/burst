/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2.algorithms

import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubeCollector}
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.FeltCubeAggSemRt
import org.burstsys.vitals.bitmap.VitalsBitMapAnyVal
import org.burstsys.zap.cube2.row.ZapCube2Row
import org.burstsys.zap.cube2.state.{ZapCube2State, _}
import org.burstsys.zap.cube2.{ZapCube2, ZapCube2Builder}

/**
 *
 */
trait ZapCube2Merge extends Any with ZapCube2State {

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def interMerge(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary,
                 thatCube: FeltCubeCollector, thatDictionary: BrioMutableDictionary): Unit = {
    try {
      merge(builder, thatCube.asInstanceOf[ZapCube2], VitalsBitMapAnyVal(~0L), VitalsBitMapAnyVal(~0L), intra = false)
    } finally if (!rowsLimited) resizeCount = 0 // made it all the way through without a resize
  }

  @inline final override
  def intraMerge(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary,
                 thatCube: FeltCubeCollector, thatDictionary: BrioMutableDictionary,
                 dimensionMask: VitalsBitMapAnyVal, aggregationMask: VitalsBitMapAnyVal): Unit = {
    try {
      merge(builder, thatCube.asInstanceOf[ZapCube2], dimensionMask, aggregationMask, intra = true)
    } finally if (!rowsLimited) resizeCount = 0 // made it all the way through without a resize
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // IMPLEMENTATION
  //////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * An intra merge is a merge within the traversal of an item
   *
   * <br/>'''NOTE:''' check for dictionary overflow __before__ calling
   *
   * @param thatCube
   * @return
   */
  @inline private
  def merge(builder: FeltCubeBuilder, thatCube: ZapCube2, dimensionMask: VitalsBitMapAnyVal, aggregationMask: VitalsBitMapAnyVal, intra: Boolean): Unit = {

    if (thatCube.isEmpty) return // if the incoming cube is empty - nothing to do

    // now we have to lookup
    var b = 0
    while (b < bucketsCount) {
      // for each bucket in each cube, there are one of four states...
      val thisBucketOffset = bucketRead(b)
      val thatBucketOffset = thatCube.bucketRead(b)
      if (thisBucketOffset == EmptyBucket && thatBucketOffset == EmptyBucket) {
        // state 1: nothing in either bucket so nothing to do
      } else if (thisBucketOffset != EmptyBucket && thatBucketOffset == EmptyBucket) {
        // state 2: something in this bucket but nothing in that bucket so nothing to do
      } else if (thisBucketOffset == EmptyBucket && thatBucketOffset != EmptyBucket) {
        // state 3: nothing in current bucket and rows in other bucket so we simply need to import all the 'other' rows
        importEntireOtherBucket(builder, b, thatCube, ZapCube2Row(thatCube, thatBucketOffset))
        if (rowsLimited)
          return // always be checking
      } else {
        // state 4: something in both buckets so we need to merge the rows in both
        val thisRow = ZapCube2Row(this, thisBucketOffset)
        val thatRow = ZapCube2Row(thatCube, thatBucketOffset)
        mergeOtherRows(builder, b, dimensionMask, aggregationMask, thisRow, thatCube, thatRow, intra)
        if (rowsLimited)
          return // always be checking
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
  def importEntireOtherBucket(builder: FeltCubeBuilder, bucket: Int, thatCube: ZapCube2, thatRow: ZapCube2Row) : Unit = {

    // setup initial state
    var currentThatRow = thatRow
    var cloneRow = cloneRowFromRow(thatRow)
    if (rowsLimited)
      return // always be checking

    // if we have to upsize, we need to be sure we don't double import...
    cloneRow.dirty = true

    // insert head of incoming bucket list to head of this bucket list
    bucketWrite(bucket, rowOffset(cloneRow))

    while (!currentThatRow.isListEnd) {
      currentThatRow = ZapCube2Row(thatCube, currentThatRow.link)
      val tmpThisRow = cloneRowFromRow(currentThatRow)
      if (rowsLimited)
        return // always be checking
      tmpThisRow.dirty = true
      tmpThisRow.link = rowOffset(cloneRow)
      cloneRow = tmpThisRow
    }
  }

  /**
   * merge all rows from a bucket into another cube' bucket. We scan each row set and
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
  def mergeOtherRows(builder: FeltCubeBuilder, bucketIndex: Int, dimensionMask: VitalsBitMapAnyVal, aggregationMask: VitalsBitMapAnyVal,
                     thisRow: ZapCube2Row, thatCube: ZapCube2, thatRow: ZapCube2Row, intra: Boolean): Unit = {
    /**
     * scan all existing rows for matches with incoming rows - matches we merge, non matches we do nothing - they
     * are in the right place already
     */
    var thisContinue = true
    var tmpThisRow = thisRow
    while (thisContinue) {
      var thatContinue = true
      var tmpThatRow = thatRow
      while (thatContinue) {
        mergeRow(builder, bucketIndex, dimensionMask, aggregationMask, thatCube, tmpThisRow, tmpThatRow, intra)
        if (!tmpThatRow.isListEnd) tmpThatRow = ZapCube2Row(thatCube, tmpThatRow.link) else thatContinue = false
      }
      if (!tmpThisRow.isListEnd) tmpThisRow = ZapCube2Row(this, tmpThisRow.link) else thisContinue = false
    }

    /**
     * now scan all incoming rows for matches with existing rows - matches we ignore (they have been merged already)
     * non matches we import since these have not matched with existing rows and so are bonafide new keys
     * It is important for multiple reasons to link in rows at the beginning of the list. This allows us
     * always scan forward without facing a changed list, and to be able to get to pointers efficiently (the head
     * of the bucket list is always stored in a known place)
     */
    var continue = true
    var tmpThatRow = thatRow
    while (continue) {
      importRow(builder, bucketIndex, thatCube, tmpThatRow)
      if (rowsLimited)
        return // always be checking
      if (!tmpThatRow.isListEnd) tmpThatRow = ZapCube2Row(thatCube, tmpThatRow.link) else continue = false
    }
  }

  @inline private
  def mergeRow(builder: FeltCubeBuilder, bucketIndex: Int, dimensionMask: VitalsBitMapAnyVal, aggregationMask: VitalsBitMapAnyVal,
               thatCube: ZapCube2, existingRow: ZapCube2Row, incomingRow: ZapCube2Row, intra: Boolean) : Unit = {

    // if these two match then merge the rows.
    // we have to be careful to not deal with 'dirty' (already processed rows)
    if (existingRow.matchesRow(incomingRow) && !(existingRow.dirty || incomingRow.dirty)) {

      // if we have to upsize, we need to be sure we don't double merge...
      existingRow.dirty = true
      incomingRow.dirty = true

      // merge the rows for this we require aggregation semantics
      var aggregation = 0
      while (aggregation < aggCount) {
        // make sure that we do not aggregate for fields in other gathers
        if (aggregationMask.testBit(aggregation)) {
          val semantic: FeltCubeAggSemRt = builder.aggregationSemantics(aggregation)
          if (!incomingRow.aggIsNull(aggregation)) {
            builder.aggregationFieldTypes(aggregation) match {
              case BrioBooleanKey =>
                existingRow.aggWrite(aggregation, semantic.doBoolean(existingRow.aggRead(aggregation), incomingRow.aggRead(aggregation), intra))
              case BrioByteKey =>
                existingRow.aggWrite(aggregation, semantic.doByte(existingRow.aggRead(aggregation), incomingRow.aggRead(aggregation), intra))
              case BrioShortKey =>
                existingRow.aggWrite(aggregation, semantic.doShort(existingRow.aggRead(aggregation), incomingRow.aggRead(aggregation), intra))
              case BrioIntegerKey =>
                existingRow.aggWrite(aggregation, semantic.doInteger(existingRow.aggRead(aggregation), incomingRow.aggRead(aggregation), intra))
              case BrioLongKey =>
                existingRow.aggWrite(aggregation, semantic.doLong(existingRow.aggRead(aggregation), incomingRow.aggRead(aggregation), intra))
              case BrioDoubleKey =>
                existingRow.aggWrite(aggregation, semantic.doDouble(existingRow.aggRead(aggregation), incomingRow.aggRead(aggregation), intra))
              case BrioStringKey =>
                existingRow.aggWrite(aggregation, semantic.doString(existingRow.aggRead(aggregation), incomingRow.aggRead(aggregation), intra))
            }
          }
        }
        aggregation += 1
      }
    }
  }

  @inline private
  def importRow(builder: FeltCubeBuilder, bucketIndex: Int, thatCube: ZapCube2, incomingRow: ZapCube2Row) : Unit = {
    /**
     * yes for now we have to scan this over and over again - generally this is ok - bucket lists are short
     * and we are fast  and its more important to get it working and not allocate any objects. However we
     * perhaps might want to figure out how to store these without creating ephemeral objects.
     */
    var foundMatch = false
    var tmpThisRowOffset = bucketRead(bucketIndex)
    while (tmpThisRowOffset != EmptyBucket && !foundMatch) {
      val tmpThisRow = ZapCube2Row(this, tmpThisRowOffset)
      foundMatch = tmpThisRow.matchesRow(incomingRow)
      tmpThisRowOffset = tmpThisRow.link
    }
    // we did not find a match, clone and and insert the incoming row into bucket beginning
    if (!foundMatch) {
      val oldHeadOffset = bucketRead(bucketIndex)
      val newHead = cloneRowFromRow(incomingRow)
      if (rowsLimited)
        return // always be checking
      // if we have to upsize, we need to be sure we don't double import...
      newHead.dirty = true
      // link cloned row to old bucket list head
      newHead.link = oldHeadOffset
      // and then insert cloned row into bucket list head
      bucketWrite(bucketIndex, rowOffset(newHead))
    }
  }

}
