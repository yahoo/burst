/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive

import org.burstsys.brio.types.BrioPrimitives._
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.{FeltCubeAggSemRt, UNIQUE_AGGREGATION_SEMANTIC}
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I}

/**
 * if an intra merge (within an item traversal) then limit result to a ceiling of 1
 * if an inter merge (merges across items/traversals) then do normal sum
 */
final case
class FeltCubeAggUniqueSemRt() extends AnyRef with FeltCubeAggSemRt {

  semanticType = UNIQUE_AGGREGATION_SEMANTIC

  ///////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ///////////////////////////////////////////////////////////////////////////////////

  def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode =
    s"$I${classOf[FeltCubeAggUniqueSemRt].getName}()"

  ///////////////////////////////////////////////////////////////////////////////////
  // runtime operations
  ///////////////////////////////////////////////////////////////////////////////////

  @inline override
  def doBoolean(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = {
    if (intra) {
      if (a > 0 || b > 0) 1L else 0L
    } else {
      brioBooleanToPrimitive(brioPrimitiveToBoolean(a) || brioPrimitiveToBoolean(b))
    }
  }

  @inline override
  def doByte(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = {
    if (intra) {
      if (a > 0 || b > 0) 1L else 0L
    } else {
      a + b
    }
  }

  @inline override
  def doShort(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = {
    if (intra) {
      if (a > 0 || b > 0) 1L else 0L
    } else {
      a + b
    }
  }

  @inline override
  def doInteger(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = {
    if (intra) {
      if (a > 0 || b > 0) 1L else 0L
    } else {
      a + b
    }
  }

  @inline override
  def doLong(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = {
    if (intra) {
      if (a > 0 || b > 0) 1L else 0L
    } else {
      a + b
    }
  }

  @inline override
  def doDouble(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean = true): BrioPrimitive = {
    if (intra) {
      if (a > 0 || b > 0) 1L else 0L
    } else {
      brioDoubleToPrimitive(brioPrimitiveToDouble(a) + brioPrimitiveToDouble(b))
    }
  }

  @inline override
  def doString(a: BrioPrimitive, b: BrioPrimitive, intra: Boolean): BrioPrimitive = ???


  ///////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ///////////////////////////////////////////////////////////////////////////////////

  def normalizedSource(implicit index: Int): String =
    s"S${classOf[FeltCubeAggUniqueSemRt].getName}()"
}
