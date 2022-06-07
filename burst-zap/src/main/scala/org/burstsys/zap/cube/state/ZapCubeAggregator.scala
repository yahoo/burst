/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube.state

import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive
import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubeCollector}
import org.burstsys.zap.cube.{ZapCube, ZapCubeBuilder, ZapCubeContext}

/**
 * When you write into the aggregation, you must have a dimension cursor set to indicate the appropriate row.
 * Any or all of the dimensions may be null, which is a valid value for a dimension (it means non-set/wild card/null)
 * and indicates that a cross join is desired.
 */
trait ZapCubeAggregator extends Any with ZapCube {

  @inline final override
  def readAggregationNull(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, aggregation: Int): Boolean = {
    val zb = builder.asInstanceOf[ZapCubeBuilder]
    val tc = thisCube.asInstanceOf[ZapCubeContext]
    tc.navigate(zb, tc)
    tc.cursorRow.readRowAggregationIsNull(zb, tc, aggregation)
  }

  @inline final override
  def writeAggregationNull(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, column: Int): Unit = {
    val zb = builder.asInstanceOf[ZapCubeBuilder]
    val tc = thisCube.asInstanceOf[ZapCubeContext]
    tc.navigate(zb, tc)
    tc.cursorRow.writeAggregationNullMap(zb, tc, column)
  }

  @inline final override
  def readAggregationPrimitive(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, aggregation: Int): BrioPrimitive = {
    val zb = builder.asInstanceOf[ZapCubeBuilder]
    val tc = thisCube.asInstanceOf[ZapCubeContext]
    tc.navigate(zb, tc)
    tc.cursorRow.readRowAggregationPrimitive(zb, tc, aggregation)
  }

  @inline final override
  def writeAggregationPrimitive(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, aggregation: Int, value: BrioPrimitive): Unit = {
    val zb = builder.asInstanceOf[ZapCubeBuilder]
    val tc = thisCube.asInstanceOf[ZapCubeContext]
    tc.navigate(zb, tc)
    tc.cursorRow.writeRowAggregationPrimitive(zb, tc, aggregation, value)
  }

}
