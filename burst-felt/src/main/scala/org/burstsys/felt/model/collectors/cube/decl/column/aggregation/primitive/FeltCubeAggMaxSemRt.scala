/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive

import org.burstsys.brio.types.BrioPrimitives._
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.{FeltCubeAggSemRt, MAX_AGGREGATION_SEMANTIC}
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}

final case
class FeltCubeAggMaxSemRt() extends AnyRef with FeltCubeAggSemRt {

  semanticType = MAX_AGGREGATION_SEMANTIC

  ///////////////////////////////////////////////////////////////////////////////////
  // code generator
  ///////////////////////////////////////////////////////////////////////////////////

  def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode =
    s"${classOf[FeltCubeAggMaxSemRt].getName}()"

  ///////////////////////////////////////////////////////////////////////////////////
  // runtime
  ///////////////////////////////////////////////////////////////////////////////////

  @inline override
  def doBoolean(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive =
    brioBooleanToPrimitive(brioPrimitiveToBoolean(a) || brioPrimitiveToBoolean(b))

  @inline override
  def doByte(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = math.max(a, b)

  @inline override
  def doByteInit(): BrioPrimitive = minBrioNumber

  @inline override
  def doShort(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = math.max(a, b)

  @inline override
  def doShortInit(): BrioPrimitive = minBrioNumber

  @inline override
  def doInteger(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = math.max(a, b)

  @inline override
  def doIntegerInit(): BrioPrimitive = minBrioNumber

  @inline override
  def doLong(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = math.max(a, b)

  @inline override
  def doLongInit(): BrioPrimitive = minBrioNumber

  @inline override
  def doDouble(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive =
    brioDoubleToPrimitive(math.max(brioPrimitiveToDouble(a), brioPrimitiveToDouble(b)))

  @inline override
  def doDoubleInit(): BrioPrimitive = minBrioNumber

  @inline override
  def doString(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean): BrioPrimitive = ???

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def normalizedSource(implicit index: Int): String =
    s"${classOf[FeltCubeAggMaxSemRt].getName}()"

}
