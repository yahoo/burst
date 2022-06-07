/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2.algorithms

import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubeCollector}
import org.burstsys.vitals.bitmap.VitalsBitMapAnyVal
import org.burstsys.zap.cube2
import org.burstsys.zap.cube2.{ZapCube2, state}
import org.burstsys.zap.cube2.row.{ZapCube2Row, limitExceededMarkerRow}
import org.burstsys.zap.cube2.state._

/**
 * ==Join the child into the parent.==
 * This is where we join a child cube into a parent cube. This means we scan through both parent and child
 * rows and create a result set that is a well defined combination (''join'') of parent and child
 * dimensions/aggregations. When we enter this routine we are given the set of ''active'' dimensions and
 * aggregations for both parent and child.
 *
 *
 * <ul>
 * The '''JOIN''' algorithm is as follows:
 * <li>For each active parent dimension and each active child dimension, create rows for each '''combination'''
 * of the two</li>
 * <li>For each active parent aggregation and each active child aggregation, execute the aggregation semantic
 * in any of these rows from the appropriate (parent or child) incoming aggregation</li>
 * <li>this can be thought of as the same as a SQL CROSS JOIN and creates what amount to '''groupBy''' semantics</li>
 * <li>this is used to do what amounts to a '''self join''' of multi-dimensional data (rows) from a child object in
 * its parent object in an object tree traversal</li>
 * <li>this algorithm is completely order independent</li>
 * <li>The join takes a given parent and child cube instance and places the result into a provided third cube</li>
 * </ul>
 *
 * == EXAMPLE==
 * {{{
 * Start with a parent cube instance CUBE0:
 * CUBE0 (parent)
 * |D0|A0|
 * | 1| 1|
 * | 2| 1|
 * }}}
 * {{{
 * Then join in  a child cube instance CUBE1:
 *            CUBE1 (child0)
 *            |D1|A1|
 *            | 3| 1|
 *            | 4| 1|
 *
 * We get as a result:
 *                                  RESULT
 *                             |D0|D1|A0|A1|
 *                             | 1| 3| 1| 1|
 *                             | 1| 4| 1| 1|
 *                             | 2| 3| 1| 1|
 *                             | 2| 4| 1| 1|
 * }}}
 * {{{
 * Then join in a second child cube instance CUBE2:  *
 *            CUBE2 (child1)
 *            |D2|A2|
 *            | 5| 1|
 *            | 6| 1|
 *
 * We get as a result:
 *                                  RESULT
 *                             |D0|D1|D2|A0|A1|A2|
 *                             | 1| 3| 5| 1| 1| 1|
 *                             | 1| 3| 6| 1| 1| 1|
 *                             | 1| 4| 5| 1| 1| 1|
 *                             | 1| 4| 6| 1| 1| 1|
 *                             | 2| 3| 5| 1| 1| 1|
 *                             | 2| 3| 6| 1| 1| 1|
 *                             | 2| 4| 5| 1| 1| 1|
 *                             | 2| 4| 6| 1| 1| 1|
 * }}}
 * And we are left with the cross join '''RESULT == (CUBE0 X CUBE1 X CUBE2)'''
 * <p> This means we continuously add in an combinations of multiple dimensional data from children into
 * the parent thus supporting '''group by''' semantics per Burst count/dimension/filter top level mission.
 * </p>
 *
 * '''NOTE:''' the join algorithm is perfectly re-entrant since it never does any merging of aggregations it only
 * does creation of rows and setting of aggregations. This means that we do not really need to worry about
 * upsizing causing artifacts related to stopping midway through. Cool beans!
 *
 **/
trait ZapCube2Join extends Any with ZapCube2State {

  @inline final override
  def joinWithChildCubeIntoResultCube(builder: FeltCubeBuilder,
                                      thisCube: FeltCubeCollector,
                                      thisDictionary: BrioMutableDictionary,
                                      childCube: FeltCubeCollector,
                                      resultCube: FeltCubeCollector,
                                      parentDimensionMask: VitalsBitMapAnyVal, parentAggregationMask: VitalsBitMapAnyVal,
                                      childDimensionMask: VitalsBitMapAnyVal, childAggregationMask: VitalsBitMapAnyVal): Unit = {
    val rc = resultCube.asInstanceOf[ZapCube2]
    val cc = childCube.asInstanceOf[ZapCube2]

    try {
      /**
       * if this cube as the ''parent'' is empty, then just copy any ''child'' rows in to this cube verbatim.
       */
      if (this.isEmpty) {
        var b = 0
        while (b < bucketsCount) { // same as child cube!
          cc.bucketRead(b) match {
            case EmptyBucket => // nada
            case childBucketHeadOffset =>
              var childContinue = true
              var childListRow = ZapCube2Row(cc, childBucketHeadOffset)
              while (childContinue) {
                rc.createCopyRow(
                  parentCube = this,
                  childCube = cc, childRow = childListRow,
                  resultCube = rc,
                  parentDimensionMask = parentDimensionMask, parentAggregationMask = parentAggregationMask,
                  childDimensionMask = childDimensionMask, childAggregationMask = childAggregationMask
                )
                if (rowsLimited) return
                if (childListRow.isListEnd) childContinue = false else childListRow = ZapCube2Row(cc, childListRow.link)
              }
          }
          b += 1
        }
        return
      }

      /**
       * if the parent is not empty, then we have to create a new ''joined'' row for every combination of child and parent rows
       * in a perfect nested loop. (O:N2)
       */

      // for each child bucket and each row in the bucket...
      var childBucket = 0
      while (childBucket < cc.bucketsCount) {
        cc.bucketRead(childBucket) match {
          case EmptyBucket => // nada
          case childBucketHeadOffset =>
            var childListRow = ZapCube2Row(cc, childBucketHeadOffset)

            // for each parent bucket and each row in the bucket...
            var childContinue = true
            while (childContinue) {
              var parentBucket = 0
              while (parentBucket < this.bucketsCount) {
                this.bucketRead(parentBucket) match {
                  case EmptyBucket => // nada
                  case parentBucketHeadOffset =>

                    var parentListRow = ZapCube2Row(this, parentBucketHeadOffset)
                    var parentContinue = true
                    while (parentContinue) {
                      // create join row for each row in bucket
                      rc.createJoinRow(
                        parentRow = parentListRow,
                        childCube = cc, childRow = childListRow,
                        resultCube = rc,
                        parentDimensionMask = parentDimensionMask, parentAggregationMask = parentAggregationMask,
                        childDimensionMask = childDimensionMask, childAggregationMask = childAggregationMask
                      )
                      if (rowsLimited) return
                      if (parentListRow.isListEnd) parentContinue = false else parentListRow = ZapCube2Row(this, parentListRow.link)
                    }

                }
                parentBucket += 1
              }

              if (childListRow.isListEnd) childContinue = false else childListRow = ZapCube2Row(this, childListRow.link)
            }
        }
        childBucket += 1
      }

    } finally if (!rowsLimited) resizeCount = 0 // made it all the way through without a resize
  }

  @inline final override
  def createJoinRow(
                     parentRow: ZapCube2Row,
                     childCube: ZapCube2, childRow: ZapCube2Row,
                     resultCube: ZapCube2,
                     parentDimensionMask: VitalsBitMapAnyVal, parentAggregationMask: VitalsBitMapAnyVal,
                     childDimensionMask: VitalsBitMapAnyVal, childAggregationMask: VitalsBitMapAnyVal): ZapCube2Row = {

    // get our zero gc initialized temporary join key TODO - did I convince myself we can't use cursor?
    val joinPivot = pivot
    resetPivot()

    // put the correct dimensions in it
    var d = 0
    while (d < dimCount) {
      //  copy over the parent dimension if mask so indicates
      if (parentDimensionMask.testBit(d)) if (!parentRow.dimIsNull(d)) joinPivot.dimWrite(d, parentRow.dimRead(d))

      //  copy over the child dimension if mask so indicates
      if (childDimensionMask.testBit(d)) if (!childRow.dimIsNull(d)) joinPivot.dimWrite(d, childRow.dimRead(d))
      d += 1
    }

    // place into result cube -- navigate to / insert the row pointed to by our pivot key
    val resultRow = resultCube.navigate(joinPivot)
    if (resultRow == limitExceededMarkerRow) return limitExceededMarkerRow

    // set the aggregations to the correct values
    var a = 0
    resultRow.aggNullMap = 0L
    while (a < aggCount) {
      // always copy over the parent aggregation
      if (parentAggregationMask.testBit(a)) if (!parentRow.aggIsNull(a)) resultRow.aggWrite(a, parentRow.aggRead(a))

      // only do child if this dimension comes from the child
      if (childAggregationMask.testBit(a)) if (!childRow.aggIsNull(a)) resultRow.aggWrite(a, childRow.aggRead(a))
      a += 1
    }
    resultRow
  }

  @inline final override
  def createCopyRow(
                     parentCube: ZapCube2,
                     childCube: ZapCube2, childRow: ZapCube2Row,
                     resultCube: ZapCube2,
                     parentDimensionMask: VitalsBitMapAnyVal, parentAggregationMask: VitalsBitMapAnyVal,
                     childDimensionMask: VitalsBitMapAnyVal, childAggregationMask: VitalsBitMapAnyVal): ZapCube2Row = {

    // get our zero gc initialized temporary join key TODO - did I convince myself we can't use cursor?
    val joinPivot = pivot
    resetPivot()

    // put the correct dimensions in it
    var d = 0
    while (d < dimCount) {
      //  copy over the child dimension if mask so indicates
      if (childDimensionMask.testBit(d)) if (!childRow.dimIsNull(d)) joinPivot.dimWrite(d, childRow.dimRead(d))
      d += 1
    }

    // navigate to / insert the row pointed to by our pivot key
    val resultRow = resultCube.navigate(joinPivot)
    if (resultRow == limitExceededMarkerRow) return limitExceededMarkerRow

    // set the aggregations to the correct values
    resultRow.aggNullMap = 0L
    var a = 0
    while (a < aggCount) {
      // only do child if this dimension comes from the child
      if (childAggregationMask.testBit(a)) if (!childRow.aggIsNull(a)) resultRow.aggWrite(a, childRow.aggRead(a))
      a += 1
    }
    resultRow
  }

}
