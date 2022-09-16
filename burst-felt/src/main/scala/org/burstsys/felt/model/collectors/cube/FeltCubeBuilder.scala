/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube

import org.burstsys.brio.types.BrioTypes.{BrioRelationName, BrioTypeKey}
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.FeltCubeAggSemRt
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.FeltCubeDimSemRt
import org.burstsys.felt.model.collectors.cube.plane.FeltCubePlaneContext
import org.burstsys.felt.model.collectors.cube.runtime.FeltCubeTreeMask
import org.burstsys.felt.model.collectors.runtime.{FeltCollectorBuilder, FeltCollectorBuilderContext, FeltCollectorPlane}
import org.burstsys.tesla.TeslaTypes.TeslaMemoryOffset

trait FeltCubeBuilder extends FeltCollectorBuilder {

  /**
   *
   * @return
   */
  def totalMemorySize: TeslaMemoryOffset

  /**
   *
   * @return
   */
  def rowLimit: Int

  /**
   * the number of 'buckets' in the cube
   *
   * @return
   */
  def bucketCount(maxRowCount: Int): Int

  ///////////////////////////////////////////////////////////////////////////////////////////////
  // FIELDS
  ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * '''NOTE:''' this is not peformant, use only in unit tests
   *
   * @return
   */
  def fieldKeyMap: Map[BrioRelationName, Int]

  /**
   * dimension names come first...
   *
   * @return
   */
  def fieldNames: Array[BrioRelationName]

  ///////////////////////////////////////////////////////////////////////////////////////////////
  // DIMENSIONS
  ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   *
   * @param d
   * @return
   */
  def dimensionName(d: Int): String

  /**
   *
   * @return
   */
  def dimensionSemantics: Array[FeltCubeDimSemRt]

  /**
   *
   * @return
   */
  def dimensionFieldTypes: Array[BrioTypeKey]

  /**
   *
   * @return
   */
  def dimensionCount: Int

  /**
   *
   * @return
   */
  def hasStringDimensions: Boolean

  /**
   *
   * @return
   */
  def dimensionCubeJoinMask: FeltCubeTreeMask

  ///////////////////////////////////////////////////////////////////////////////////////////////
  // AGGREGATIONS
  ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * aggregation name to index map (GIST ONLY)
   *
   * @param a
   * @return
   */
  def aggregationName(a: Int): String

  /**
   *
   * @return
   */
  def aggregationSemantics: Array[FeltCubeAggSemRt]

  /**
   *
   * @return
   */
  def aggregationFieldTypes: Array[BrioTypeKey]

  /**
   *
   * @return
   */
  def aggregationCount: Int

  /**
   *
   * @return
   */
  def hasStringAggregations: Boolean

  /**
   *
   * @return
   */
  def aggregationCubeJoinMask: FeltCubeTreeMask

}

abstract
class FeltCubeBuilderContext extends FeltCollectorBuilderContext with FeltCubeBuilder {

  final override def collectorPlaneClass[C <: FeltCollectorPlane[_, _]]: Class[C] =
    classOf[FeltCubePlaneContext].asInstanceOf[Class[C]]

}
