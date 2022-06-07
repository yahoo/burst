/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube.algorithms

import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.fabric.execution.model.result.row.FabricDataKeyAnyVal
import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubeCollector}
import org.burstsys.vitals.bitmap.VitalsBitMapAnyVal
import org.burstsys.zap.cube
import org.burstsys.zap.cube.{ZapCubeRow, _}

/**
 * ==Cube Joins==
 * A Cube Join is a transformation of two cubes to create a third.
 * ==Zap Cube Schemas==
 * All Zap Cubes in a Hyper Cube are all the same schema - even if
 * the actual schema for a given cube is a restriction of this super gather
 * column set. We use __Join Maps__ to
 * determine which columns are 'active' during a join (see nested gather specs).
 * We assume that the dictionary is shared across all joined cubes.
 * ==HYPER CUBE SCHEMA==
 * {{{
 *            CUBE0(D0, A0)
 *            /        \
 *  CUBE1(D1, A1)      CUBE2(D2, A2)
 * }}}
 * Here we see a simple parent cube (Cube0) with two subcubes (CUBE1, CUBE2). When we leave the CUBE0 scope
 * we want to join in the children into the final result which gets transferred up the next scope (CUBE0 could be
 * a child of another cube scope). Note that a single child cube is all that is needed
 * for a join - we illustrate two children just to show how that would be done.
 * ==JOIN SEMANTICS ==
 * {{{
 * def CUBE1.join(CUBE2): result = {
 *    SELECT CUBE1.aggregation*, CUBE2.aggregation*
 *        FROM CUBE1 FULL OUTER JOIN CUBE2
 *          ON CUBE1.dimension* == CUBE2.dimension*
 * }
 * }}}
 * This is essentially a SQL FULL OUTER JOIN on two tables (cubes) CUBE1 and CUBE2 ON all common dimensions between
 * CUBE1 and CUBE2 with a cross join handling of all dimension fields that are not shared across gather scopes.
 * == DIMENSIONAL CROSS JOIN SEMANTICS==
 * We do a cross join between child and parent dimension such that we have a unique row for each
 * combination of dimensions across all child and the parent cubes. No nulls are allowed in dimensions.
 * ==COMMUTATIVE/ORDER-INDEPENDENCE PROPERTY==
 * Cube Joins are required to be commutative (order independent) (like SQL FULL OUTER JOINS). This means when joining
 * child cubes into a parent cubes, we can have as many children as possible with no pre-determined ordering.
 * ==CROSS JOIN DIMENSION HANDLING EXAMPLE==
 * Start with a parent cube instance CUBE0:
 * {{{
 * CUBE0 (parent)
 * |D0|A0|
 * | 1| 1|
 * | 2| 1|
 * }}}
 * Then join in  a child cube instance CUBE1:
 * {{{
 *            CUBE1 (child0)
 *            |D1|A1|
 *            | 3| 1|
 *            | 4| 1|
 *                                  RESULT0
 *                             |D0|D1|A0|A1|
 *                             | 1| 3| 1| 1|
 *                             | 1| 4| 1| 1|
 *                             | 2| 3| 1| 1|
 *                             | 2| 4| 1| 1|
 * }}}
 * Then join in a second child cube instance CUBE2:
 * {{{
 *
 *            CUBE2 (child1)
 *            |D2|A2|
 *            | 5| 1|
 *            | 6| 1|
 *                                  RESULT1
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
 * And we are left with the cross join '''(CUBE1 X CUBE2 == CUBE3)'''
 */
trait ZapCubeJoiner extends Any with ZapCube {

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // PUBLIC API
  //////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def joinWithChildCubeIntoResultCube(builder: FeltCubeBuilder,
                                      parentCube: FeltCubeCollector,
                                      parentDictionary: BrioMutableDictionary,
                                      childCube: FeltCubeCollector,
                                      resultCube: FeltCubeCollector,
                                      parentDimensionMask: VitalsBitMapAnyVal,
                                      parentAggregationMask: VitalsBitMapAnyVal,
                                      childDimensionMask: VitalsBitMapAnyVal,
                                      childAggregationMask: VitalsBitMapAnyVal
                                     ): Unit = {

    val parentC = parentCube.asInstanceOf[ZapCubeContext]
    val childC = childCube.asInstanceOf[ZapCubeContext]
    val resultC = resultCube.asInstanceOf[ZapCubeContext]
    val zb = builder.asInstanceOf[ZapCubeBuilder]

    /**
     * Join the child into the parent.
     * scan through all the rows of the parent table for each of those rows, we scan through all of the
     * second cube's rows creating new combinations. We essentially create a new row for every combination
     * of CUBE1 rows and CUBE2 rows - no aggregations because all rows in the new cube are guaranteed to be
     * new rows - we just copy over the aggregations. The interesting part is that we want to only work with
     * the dimension and column keys defined in the masks provided.
     */

    /**
     * if the parent is empty, then just copy the child rows in.
     */
    if (this.isEmpty) {
      var b = 0
      while (b < parentC.bucketCount) {
        childC.bucket(builder = zb, thisCube = childC, index = b) match {
          case ZapCubeEmptyBucket =>
          case p =>
            var row = ZapCubeRow(p)
            do {
              resultC.createCopyRow(
                builder = builder,
                thisCube = resultC,
                parentCube = parentC,
                childCube = childC, childRow = row, resultCube = resultC,
                parentDimensionMask = parentDimensionMask, parentAggregationMask = parentAggregationMask,
                childDimensionMask = childDimensionMask, childAggregationMask = childAggregationMask
              )
              if (parentC.rowLimited) return
              row = row.linkRow(builder = zb, thisCube = childC)
            } while (row.validRow)
        }
        b += 1
      }
      return
    }

    /**
     * we have to create a new row for every combination of child and parent rows - perfect nested loop
     */
    var c = 0
    while (c < parentC.bucketCount) {
      childC.bucket(builder = zb, thisCube = childC, index = c) match {
        case ZapCubeEmptyBucket =>
        case cPtr =>
          var childRow = cube.ZapCubeRow(cPtr)
          do {
            var p = 0
            while (p < parentC.bucketCount) {
              parentC.bucket(builder = zb, thisCube = parentC, index = p) match {
                case ZapCubeEmptyBucket =>
                case pPtr =>
                  var parentRow = cube.ZapCubeRow(pPtr)
                  do {

                    resultC.createJoinRow(builder = builder, parentCube,
                      parentCube = parentC,
                      parentRow = parentRow, childCube = childC, childRow = childRow, resultCube = resultC,
                      parentDimensionMask = parentDimensionMask, parentAggregationMask = parentAggregationMask,
                      childDimensionMask = childDimensionMask, childAggregationMask = childAggregationMask
                    )
                    if (parentC.rowLimited) return

                    parentRow = parentRow.linkRow(builder = zb, thisCube = parentC)
                  } while (parentRow.validRow)
              }
              p += 1
            }
            childRow = childRow.linkRow(builder = zb, thisCube = childC)
          } while (childRow.validRow)
      }
      c += 1
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // IMPLEMENTATION
  //////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * create a row in the result cube that has all the active dimension and aggregation columns from the from
   * the parent  and only the active dimension/aggregations from the child mask.
   *
   * @param parentCube
   * @param parentRow
   * @param childCube
   * @param childRow
   * @param resultCube
   * @param parentDimensionMask   active dimensions in parent
   * @param parentAggregationMask active aggregations in parent
   * @param childDimensionMask    active dimensions in child
   * @param childAggregationMask  active aggregations in child
   * @return
   */
  @inline final private[zap]
  def createJoinRow(builder: FeltCubeBuilder, thisCube: FeltCubeCollector,
                    parentCube: ZapCubeContext, parentRow: ZapCubeRow,
                    childCube: ZapCubeContext, childRow: ZapCubeRow,
                    resultCube: ZapCubeContext,
                    parentDimensionMask: VitalsBitMapAnyVal, parentAggregationMask: VitalsBitMapAnyVal,
                    childDimensionMask: VitalsBitMapAnyVal, childAggregationMask: VitalsBitMapAnyVal
                   ): ZapCubeRow = {
    val zb = builder.asInstanceOf[ZapCubeBuilder]
    val thisC = thisCube.asInstanceOf[ZapCubeContext]

    // create a key with the correct dimensions in it
    val key = FabricDataKeyAnyVal(thisC.freshKeyData)

    var d = 0
    while (d < builder.dimensionCount) {
      // always copy over the parent dimension
      if (parentDimensionMask.testBit(d)) {
        // make sure it isn't null
        if (!parentRow.readRowDimensionIsNull(zb, parentCube, d)) {
          val value = parentRow.readRowDimensionPrimitive(zb, parentCube, d)
          key.writeKeyDimensionPrimitive(d, value)
        }
      }

      // only do child if this dimension comes from the child
      if (childDimensionMask.testBit(d)) {
        // make sure it isn't null
        if (!childRow.readRowDimensionIsNull(zb, childCube, d)) {
          val value = childRow.readRowDimensionPrimitive(zb, childCube, d)
          key.writeKeyDimensionPrimitive(d, value)
        }
      }
      d += 1
    }

    // insert that row
    val resultRow = resultCube.navigate(zb, resultCube, key)
    if (thisC.rowLimited) return cube.ZapCubeRow()

    // and then set the aggregations to the correct values
    var a = 0
    resultRow.clearAggregationNullMap(zb, resultCube)
    while (a < builder.aggregationCount) {
      // always copy over the parent aggregation
      // this is in our (parent) context...
      if (parentAggregationMask.testBit(a)) {
        if (!parentRow.readRowAggregationIsNull(zb, parentCube, a)) {
          val value = parentRow.readRowAggregationPrimitive(zb, parentCube, a)
          resultRow.writeRowAggregationPrimitive(zb, resultCube, a, value)
        }
      }

      // only do child if this dimension comes from the child
      if (childAggregationMask.testBit(a)) {
        if (!childRow.readRowAggregationIsNull(zb, childCube, a)) {
          // make sure to set the child cube for context...
          val value = childRow.readRowAggregationPrimitive(zb, childCube, a)
          // make sure to set the result cube for context...
          resultRow.writeRowAggregationPrimitive(zb, resultCube, a, value)
        }
      }
      a += 1
    }
    resultRow
  }

  /**
   * Create a row where there is no parent row to join with
   *
   * @param parentCube
   * @param childCube
   * @param childRow
   * @param resultCube
   * @param parentDimensionMask
   * @param parentAggregationMask
   * @param childDimensionMask
   * @param childAggregationMask
   * @return
   */
  @inline final private[zap]
  def createCopyRow(builder: FeltCubeBuilder, thisCube: FeltCubeCollector,
                    parentCube: ZapCubeContext,
                    childCube: ZapCubeContext, childRow: ZapCubeRow,
                    resultCube: ZapCubeContext,
                    parentDimensionMask: VitalsBitMapAnyVal, parentAggregationMask: VitalsBitMapAnyVal,
                    childDimensionMask: VitalsBitMapAnyVal, childAggregationMask: VitalsBitMapAnyVal
                   ): ZapCubeRow = {
    val zb = builder.asInstanceOf[ZapCubeBuilder]
    val tc = thisCube.asInstanceOf[ZapCubeContext]

    // create a key with the all null dimensions in it
    val key = FabricDataKeyAnyVal(tc.freshKeyData)

    var d = 0
    while (d < builder.dimensionCount) {

      // only do child if this dimension comes from the child
      if (childDimensionMask.testBit(d)) {
        // check for nulls...
        if (!childRow.readRowDimensionIsNull(zb, childCube, d)) {
          val value = childRow.readRowDimensionPrimitive(zb, childCube, d)
          key.writeKeyDimensionPrimitive(d, value)
        }
      }
      d += 1
    }

    // insert that row
    val resultRow = resultCube.navigate(builder = zb, thisCube = resultCube, key = key)
    if (tc.rowLimited) return cube.ZapCubeRow()

    // and then set the aggregations to the correct values
    resultRow.clearAggregationNullMap(builder = zb, thisCube = resultCube)
    var a = 0
    while (a < builder.aggregationCount) {

      // only do child if this dimension comes from the child
      if (childAggregationMask.testBit(a)) {
        // check for nulls...
        if (!childRow.readRowAggregationIsNull(zb, childCube, a)) {
          // make sure to set the child cube for context...
          val value = childRow.readRowAggregationPrimitive(zb, childCube, a)
          // make sure to set the result cube for context...
          resultRow.writeRowAggregationPrimitive(zb, resultCube, a, value)
        }
      }
      a += 1
    }
    resultRow
  }


}
