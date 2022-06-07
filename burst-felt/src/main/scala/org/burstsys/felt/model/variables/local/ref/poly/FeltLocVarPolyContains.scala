/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.variables.local.ref.poly

import org.burstsys.brio.model.schema.types.{BrioValueArrayRelation, BrioValueMapRelation, BrioValueScalarRelation, BrioValueSetRelation}
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.poly.FeltPolyRefFuncGen
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, FeltNoCode}
import org.burstsys.felt.model.variables.local.FeltLocVarDecl

trait FeltLocVarPolyContains extends FeltPolyRefFuncGen {

  def refDecl: FeltLocVarDecl

  override
  def generateContainsPolyFunc(func: FeltFuncExpr)(implicit cursor: FeltCodeCursor): FeltCode = {
    func.parameterCountAtLeast(2)
    refDecl.typeDeclaration.feltType.form match {
      case BrioValueScalarRelation => genValueScalarRelation(func)
      case BrioValueSetRelation => genValueSetRelation(func)
      case BrioValueArrayRelation => genValueArrayRelation(func)
      case BrioValueMapRelation => genValueMapRelation(func)
      case _ => ???
    }
  }

  private
  def genValueScalarRelation(func: FeltFuncExpr)(implicit cursor: FeltCodeCursor): FeltCode = {
    FeltNoCode
  }

  private
  def genValueSetRelation(func: FeltFuncExpr)(implicit cursor: FeltCodeCursor): FeltCode = {
    FeltNoCode
  }

  private
  def genValueArrayRelation(func: FeltFuncExpr)(implicit cursor: FeltCodeCursor): FeltCode = {
    FeltNoCode
  }

  private
  def genValueMapRelation(func: FeltFuncExpr)(implicit cursor: FeltCodeCursor): FeltCode = {
    FeltNoCode
  }

}
