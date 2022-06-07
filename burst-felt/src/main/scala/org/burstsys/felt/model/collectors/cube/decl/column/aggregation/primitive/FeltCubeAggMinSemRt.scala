/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive

import org.burstsys.brio.types.BrioPrimitives.{BrioPrimitive, _}
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.{FeltCubeAggSemRt, MIN_AGGREGATION_SEMANTIC}
import org.burstsys.felt.model.tree.code.{FeltCodeCursor, _}

final case
class FeltCubeAggMinSemRt() extends FeltCubeAggSemRt {

  semanticType = MIN_AGGREGATION_SEMANTIC

  ///////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ///////////////////////////////////////////////////////////////////////////////////

  def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode =
    s"$I${classOf[FeltCubeAggMinSemRt].getName}()"

  ///////////////////////////////////////////////////////////////////////////////////
  // runtime
  ///////////////////////////////////////////////////////////////////////////////////

  @inline override
  def doBoolean(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive =
    brioBooleanToPrimitive(brioPrimitiveToBoolean(a) && brioPrimitiveToBoolean(b))

  @inline override
  def doByte(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = math.min(a, b)

  @inline override
  def doByteInit(): BrioPrimitive = maxBrioNumber

  @inline override
  def doShort(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = math.min(a, b)

  @inline override
  def doShortInit(): BrioPrimitive = maxBrioNumber

  @inline override
  def doInteger(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = math.min(a, b)

  @inline override
  def doIntegerInit(): BrioPrimitive = maxBrioNumber

  @inline override
  def doLong(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = {
    math.min(a, b)
  }

  @inline override
  def doLongInit(): BrioPrimitive = maxBrioNumber

  @inline override
  def doDouble(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive =
    brioDoubleToPrimitive(math.min(brioPrimitiveToDouble(a), brioPrimitiveToDouble(b)))

  @inline override
  def doDoubleInit(): BrioPrimitive = maxBrioNumber

  @inline override
  def doString(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean): BrioPrimitive = ???


  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  def normalizedSource(implicit index: Int): String =
    s"${classOf[FeltCubeAggMinSemRt].getName}()"
}
