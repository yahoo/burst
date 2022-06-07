/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive

import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.{FeltCubeAggSemRt, PROJECT_AGGREGATION_SEMANTIC}
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I}

/**
 * Always just use the last value
 */
final case
class FeltCubeAggProjectSemRt() extends FeltCubeAggSemRt {

  semanticType = PROJECT_AGGREGATION_SEMANTIC

  ///////////////////////////////////////////////////////////////////////////////////
  // Generation
  ///////////////////////////////////////////////////////////////////////////////////

  def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode =
    s"$I${classOf[FeltCubeAggProjectSemRt].getName}()"

  ///////////////////////////////////////////////////////////////////////////////////
  // runtime operations
  ///////////////////////////////////////////////////////////////////////////////////

  @inline override
  def doBoolean(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = b

  @inline override
  def doByte(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = b

  @inline override
  def doShort(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = b

  @inline override
  def doInteger(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = b

  @inline override
  def doLong(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = b

  @inline override
  def doDouble(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = b

  @inline override
  def doString(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean): BrioPrimitive = b


  ///////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ///////////////////////////////////////////////////////////////////////////////////

  def normalizedSource(implicit index: Int): String =
    s"S${classOf[FeltCubeAggProjectSemRt].getName}()"
}
