/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube.plan

import org.burstsys.brio.types.BrioPath.BrioPathName
import org.burstsys.felt.model.collectors.cube.FeltCubeId
import org.burstsys.felt.model.collectors.cube.decl.FeltCubeDecl
import org.burstsys.felt.model.schema.FeltSchema
import org.burstsys.vitals.bitmap.VitalsBitMapAnyVal
import org.burstsys.zap.cube.{ZapCubeAggregationKey, ZapCubeDimensionKey}

import scala.collection.mutable.ArrayBuffer

final case
class ZapCubePlanNode(cubeDecl: FeltCubeDecl, plan: ZapCubePlan, parentNode: ZapCubePlanNode) {

  val schema: FeltSchema = cubeDecl.global.feltSchema

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // public state
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  val cubeId: FeltCubeId = plan.newCubeId

  /**
   * each cube is defined at a particular path
   */
  val path: BrioPathName = cubeDecl.refTarget.fullPathNoQuotes

  val childNodes = new ArrayBuffer[ZapCubePlanNode]

  /**
   * initialize the view and join bit maps
   */
  var dimensionView: VitalsBitMapAnyVal = VitalsBitMapAnyVal()

  var dimensionJoin: VitalsBitMapAnyVal = VitalsBitMapAnyVal()

  var aggregationJoin: VitalsBitMapAnyVal = VitalsBitMapAnyVal()

  var aggregationView: VitalsBitMapAnyVal = VitalsBitMapAnyVal()

  /**
   * make room for key lookups
   */
  val dimensionKeys = new ArrayBuffer[ZapCubeDimensionKey]

  val dimensionJoinKeys = new ArrayBuffer[ZapCubeDimensionKey]

  val aggregationKeys = new ArrayBuffer[ZapCubeAggregationKey]

  val aggregationJoinKeys = new ArrayBuffer[ZapCubeAggregationKey]

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // constructor
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   *
   * @return
   */
  def initialize: this.type = {

    // TODO EXTENDED TYPES
    val pathKey = schema.nodeForPathName(path).pathKey
    plan.pathKeyToCubeIdMap += pathKey -> cubeId

    /**
     * for each of the dimension fields, install keys and initialize
     * bit maps
     */
    if (cubeDecl.dimensions != null)
      cubeDecl.dimensions.columns foreach {
        d =>
          if (d != null) {
            // create a new dimension for the overall root cube and this cube node
            val dimensionKey = plan.newCubeDimensionKey
            dimensionKeys += dimensionKey
            dimensionView = dimensionView.setBit(dimensionKey)
            plan.addCubeDimension(dimensionKey, d)
          }
      }

    /**
     * for each of the aggregation fields, install keys and initialize
     * bit maps
     */
    if (cubeDecl.aggregations != null)
      cubeDecl.aggregations.columns foreach {
        a =>
          if (a != null) {
            // create a new aggregation for the overall root cube and this cube node
            val aggregationKey = plan.newCubeAggregationKey
            aggregationKeys += aggregationKey
            aggregationView = aggregationView.setBit(aggregationKey)
            plan.addCubeAggregation(aggregationKey, a)
          }
      }
    this
  }

}
