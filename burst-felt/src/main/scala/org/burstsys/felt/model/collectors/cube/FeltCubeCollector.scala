/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube

import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive
import org.burstsys.fabric.wave.execution.model.result.row.FeltCubeResultData
import org.burstsys.felt.model.collectors.runtime.FeltCollector
import org.burstsys.vitals.bitmap.VitalsBitMapAnyVal
import org.burstsys.vitals.text.VitalsTextCodec

/**
 *
 */
trait FeltCubeCollector extends Any with FeltCollector {

  //////////////////////////////////////////////////////////////////////////////////////
  // Merging
  //////////////////////////////////////////////////////////////////////////////////////

  /**
   * an inter merge is done across items out side of a traversal
   *
   * @param builder
   * @param thisCube
   * @param thisDictionary
   * @param thatCube
   * @param thatDictionary
   */
  def interMerge(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary,
                 thatCube: FeltCubeCollector, thatDictionary: BrioMutableDictionary): Unit

  /**
   * an intra merge is done within an item traversal
   *
   * @param builder
   * @param thisCube
   * @param thisDictionary
   * @param thatCube
   * @param thatDictionary
   * @param dimensionMask
   * @param aggregationMask
   */
  def intraMerge(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary,
                 thatCube: FeltCubeCollector, thatDictionary: BrioMutableDictionary,
                 dimensionMask: VitalsBitMapAnyVal, aggregationMask: VitalsBitMapAnyVal): Unit

  //////////////////////////////////////////////////////////////////////////////////////
  // Joins
  //////////////////////////////////////////////////////////////////////////////////////

  /**
   * Given three Cubes; __this__ and a provided __CUBE2__ and a provided
   * empty Cube __result__ all with same exact
   * __super-gather__ schemas, do a specialized Join of __this__ with __CUBE2__, with
   * the results put into __result__.
   * All FELT cubes in a query are a subset of a 'super' (HYPER) cube which has all
   * dimension columns and all aggregation
   * columns (a super-set). A __Join Mask__ for dimensions and one for aggregations
   * is provided to determine the appropriate subset of columns for each.
   * The join takes child gathers which may have a
   * subset of the overall dimensions/aggregations, and merged them together either
   * based on the dimensions that are
   * common (a regular join) or where dimensions in sub-cubes are null (a cross-join).
   *
   * @param builder
   * @param thisCube   join source A
   * @param thisDictionary
   * @param childCube  join source B
   * @param resultCube a clean result cube to join into
   * @param parentDimensionMask
   * @param parentAggregationMask
   * @param childDimensionMask
   * @param childAggregationMask
   * @return the result cube
   */
  def joinWithChildCubeIntoResultCube(builder: FeltCubeBuilder,
                                      thisCube: FeltCubeCollector,
                                      thisDictionary: BrioMutableDictionary,
                                      childCube: FeltCubeCollector,
                                      resultCube: FeltCubeCollector,
                                      parentDimensionMask: VitalsBitMapAnyVal,
                                      parentAggregationMask: VitalsBitMapAnyVal,
                                      childDimensionMask: VitalsBitMapAnyVal,
                                      childAggregationMask: VitalsBitMapAnyVal): Unit

  //////////////////////////////////////////////////////////////////////////////////////
  // Dictionary Normalization
  //////////////////////////////////////////////////////////////////////////////////////

  /**
   * Make sure that the returned 'that' cube is normalized to the same dictionary as the 'this' cube
   * There are four cases for the type of schema involved
   * <ol>
   * <li> schema has neither string dimensions nor string aggregations</li>
   * <li> schema has no string dimensions but does have string aggregations</li>
   * <li> schema has string dimensions but not string aggregations</li>
   * <li> schema has both string dimensions and string aggregations</li>
   * </ol>
   * We handle each separately
   *
   * @param builder
   * @param thisCube
   * @param thisDictionary
   * @param thatCube
   * @param thatDictionary
   * @param text
   * @return
   */
  def normalize(builder: FeltCubeBuilder,
                thisCube: FeltCubeCollector, thisDictionary:
                BrioMutableDictionary, thatCube: FeltCubeCollector,
                thatDictionary: BrioMutableDictionary)(implicit text: VitalsTextCodec): FeltCubeCollector

  //////////////////////////////////////////////////////////////////////////////////////
  // TOP K PROCESSING
  //////////////////////////////////////////////////////////////////////////////////////

  /**
   * forward quick sort rows based on a given '''aggregation''' and then truncate to the '''k''' rows
   * that have the maximum values for that aggregation
   *
   * @param builder
   * @param thisCube
   * @param k           the count of ''bottom'' rows to return
   * @param aggregation which zero based aggregation to use as the sort pivot
   */
  def truncateToTopKBasedOnAggregation(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, k: Int, aggregation: Int): Unit

  /**
   * reverse quick sort rows based on a given '''aggregation''' and then truncate to the '''k''' rows
   * that have the minimum values for that aggregation
   *
   * @param builder
   * @param thisCube
   * @param k           the count of ''bottom'' rows to return
   * @param aggregation which zero based aggregation to use as the sort pivot
   */
  def truncateToBottomKBasedOnAggregation(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, k: Int, aggregation: Int): Unit

  /**
   * Reduce the cube to at most this many rows
   * @param limit row cut off
   */
  def truncateRows(limit: Int): Unit


  /**
   * here is where we extract all data and metrics from underlying dynamic structures.
   * Once this is called and data/metrics retrieved, then the underlying data
   * structures can be released.
   *
   * @param builder
   * @param thisCube
   * @param thisDictionary
   * @param text
   * @return
   */
  def extractRows(builder: FeltCubeBuilder,
                  thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary
                 )(implicit text: VitalsTextCodec): FeltCubeResultData

  //////////////////////////////////////////////////////////////////////////////////////
  // CURSORS
  //////////////////////////////////////////////////////////////////////////////////////

  /**
   *
   * @param builder
   * @param thisCube
   */
  def initCursor(builder: FeltCubeBuilder, thisCube: FeltCubeCollector): Unit

  /**
   *
   * @param builder
   * @param thisCube
   * @param parentCube
   * @return
   */
  def inheritCursor(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, parentCube: FeltCubeCollector): FeltCubeCollector

  //////////////////////////////////////////////////////////////////////////////////////
  // AGGREGATIONS
  //////////////////////////////////////////////////////////////////////////////////////

  /**
   * TODO
   *
   * @param builder
   * @param thisCube
   * @param column
   */
  def writeAggregationNull(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, column: Int): Unit

  /**
   * TODO
   *
   * @param builder
   * @param thisCube
   * @param aggregation
   * @return
   */
  def readAggregationPrimitive(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, aggregation: Int): BrioPrimitive

  /**
   * TODO
   *
   * @param builder
   * @param thisCube
   * @param aggregation
   * @param value
   */
  def writeAggregationPrimitive(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, aggregation: Int, value: BrioPrimitive): Unit

  /**
   * TODO
   *
   * @param builder
   * @param thisCube
   * @param aggregation
   */
  def readAggregationNull(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, aggregation: Int): Boolean

  //////////////////////////////////////////////////////////////////////////////////////
  // DIMENSIONS
  //////////////////////////////////////////////////////////////////////////////////////

  /**
   * TODO
   *
   * @param builder
   * @param thisCube
   * @return
   */
  def writeDimension(builder: FeltCubeBuilder, thisCube: FeltCubeCollector): Unit

  /**
   * TODO
   *
   * @param builder
   * @param thisCube
   * @param dimension
   */
  def writeDimensionNull(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, dimension: Int): Unit

  /**
   * TODO
   *
   * @param builder
   * @param thisCube
   * @param dimension
   * @param value
   */
  def writeDimensionPrimitive(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, dimension: Int, value: BrioPrimitive): Unit

  //////////////////////////////////////////////////////////////////////////////////////
  // TROUBLESHOOTING
  //////////////////////////////////////////////////////////////////////////////////////

  /**
   *
   * @param builder
   * @param thisCube
   * @param thisDictionary
   * @return
   */
  def printCube(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary): String

  /**
   *
   * @param builder
   * @param thisCube
   * @param thisDictionary
   * @param msg
   */
  def printCubeState(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary, msg: String): Unit

  /**
   *
   * @param builder
   * @param thisCube
   * @param thisDictionary
   * @return
   */
  def distribution(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary): Double

}
