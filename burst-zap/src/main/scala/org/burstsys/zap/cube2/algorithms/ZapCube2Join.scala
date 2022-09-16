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
    assert(this == thisCube)
    assert(this != resultCube)
    assert(childCube != resultCube)
    val rc = resultCube.asInstanceOf[ZapCube2]
    val cc = childCube.asInstanceOf[ZapCube2]

    var cCount = 0
    while (cCount < cc.rowsCount) {
      val currentChildRow = cc.row(cCount)

      if (this.rowsCount > 0) {
        var pCount = 0
        while (pCount < this.rowsCount) {
          val currentParentRow = this.row(pCount)
          rc.createJoinRow(
            parentRow = currentParentRow,
            childRow = currentChildRow,
            resultCube = rc,
            parentDimensionMask = parentDimensionMask, parentAggregationMask = parentAggregationMask,
            childDimensionMask = childDimensionMask, childAggregationMask = childAggregationMask
          )
          pCount += 1
        }
      } else {
        // Since there are no parent rows
        // just copy the child row info and mask the parent out
        rc.createJoinRow(
          parentRow = null,
          childRow = currentChildRow,
          resultCube = rc,
          parentDimensionMask = VitalsBitMapAnyVal(), parentAggregationMask = VitalsBitMapAnyVal(),
          childDimensionMask = childDimensionMask, childAggregationMask = childAggregationMask
        )
      }
      cCount += 1
    }
    rc.validate()
  }

  /**
   *  Make a join row in the result cube.
   *
   *  There are some strange unstated assumptions that are going on here that should be mentioned so that
   *  the reader isn't too confused.   These "assumptions" should be tracked up through the generated sweep
   *  code to correct them
   *
   *  The new row created in the result cube will have it's null bit set already.
   *  The parent and child can have overlapping masks
   *  The assumption is that only one of the child or parent will have data in a field even if the mask says both will
   *  One of child or parent will have null data for the field it set in duplicate with the other
   *  The fact that this code only write a field to the result if the source is not null, but doesn't set the result to null if it is, is
   *     crucial.  This means one of the child or parent will set the field and neither will "accidently" set it to null after the other set it.
   *  If both the parent and the child are null, then the fact that the new row is initialized to null covers that.
   *
   *  It's not clear why the parent mask overlaps the child.  This really causes problems with two dynamic paths on the
   *  same parent using the same cube, since each child seems to have the same mask and so trounce on each other.  I guess one could argue
   *  that this should be allowed.
   */
  @inline final
  def createJoinRow( parentRow: ZapCube2Row,
                     childRow: ZapCube2Row,
                     resultCube: ZapCube2,
                     parentDimensionMask: VitalsBitMapAnyVal, parentAggregationMask: VitalsBitMapAnyVal,
                     childDimensionMask: VitalsBitMapAnyVal, childAggregationMask: VitalsBitMapAnyVal): ZapCube2Row = {

    // get our zero gc initialized temporary join key TODO - did I convince myself we can't use cursor?
    val joinPivot = pivot
    resetPivot()

    // put the correct dimensions in it
    var d = 0
    while (d < dimCount) {
      if (childDimensionMask.testBit(d)) {
        if (!childRow.dimIsNull(d)) {
          joinPivot.dimWrite(d, childRow.dimRead(d))
        }
      }
      if (parentDimensionMask.testBit(d)) {
        if (!parentRow.dimIsNull(d)) {
          joinPivot.dimWrite(d, parentRow.dimRead(d))
        }
      }

      d += 1
    }

    // place into result cube -- navigate to / insert the row pointed to by our pivot key
    val resultRow = resultCube.navigate(joinPivot)
    if (resultRow == limitExceededMarkerRow)
      return limitExceededMarkerRow

    // set the aggregations to the correct values
    var a = 0
    resultRow.aggNullMap = 0L
    while (a < aggCount) {
      if (childAggregationMask.testBit(a)) {
        if (!childRow.aggIsNull(a)) {
          resultRow.aggWrite(a, childRow.aggRead(a))
        }
      }
      if (parentAggregationMask.testBit(a)) {
        if (!parentRow.aggIsNull(a)) {
          resultRow.aggWrite(a, parentRow.aggRead(a))
        }
      }

      a += 1
    }
    resultRow
  }
}
