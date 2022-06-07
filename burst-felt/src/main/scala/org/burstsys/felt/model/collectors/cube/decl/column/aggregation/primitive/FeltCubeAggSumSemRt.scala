/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive

import org.burstsys.brio.types.BrioPrimitives._
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.{FeltCubeAggSemRt, SUM_AGGREGATION_SEMANTIC}
import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.tree.source._

final case
class FeltCubeAggSumSemRt() extends AnyRef with FeltCubeAggSemRt {

  semanticType = SUM_AGGREGATION_SEMANTIC

  ///////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ///////////////////////////////////////////////////////////////////////////////////

  def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode =
    s"$I${classOf[FeltCubeAggSumSemRt].getName}()"

  ///////////////////////////////////////////////////////////////////////////////////
  // runtime operations
  ///////////////////////////////////////////////////////////////////////////////////

  @inline override
  def doBoolean(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive =
    brioBooleanToPrimitive(brioPrimitiveToBoolean(a) || brioPrimitiveToBoolean(b))

  @inline override
  def doByte(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = a + b

  @inline override
  def doShort(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = a + b

  @inline override
  def doInteger(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = a + b

  @inline override
  def doLong(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = a + b

  @inline override
  def doDouble(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive =
    brioDoubleToPrimitive(brioPrimitiveToDouble(a) + brioPrimitiveToDouble(b))

  @inline override
  def doString(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean): BrioPrimitive = ???


  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  def normalizedSource(implicit index: Int): String =
    s"$S${classOf[FeltCubeAggSumSemRt].getName}()"
}
