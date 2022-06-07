/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.variables.parameter.ref

import org.burstsys.brio.model.schema.types.{BrioValueArrayRelation, BrioValueMapRelation, BrioValueScalarRelation, BrioValueSetRelation}
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.expressions.assign.FeltUpdateOp
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.reference.{FeltReference, FeltStdRefResolver}
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}
import org.burstsys.felt.model.types.FeltTypeDecl
import org.burstsys.felt.model.variables.parameter.FeltParamDecl
import org.burstsys.felt.model.variables.parameter.ref.mutable._
import org.burstsys.felt.model.variables.parameter.ref.poly.FeltParamPolyContains
import org.burstsys.felt.model.variables.parameter.ref.primitive.FeltParamValScal
import org.burstsys.vitals.strings._


object FeltParamRef {

  final case class FeltParamRefResolver(global: FeltGlobal) extends FeltStdRefResolver[FeltParamDecl] {
    override val resolverName: String = "param"

    override protected def addResolution(refName: FeltPathExpr, d: FeltParamDecl): FeltReference =
      FeltParamRef(refName, d)

    override protected def addNomination(d: FeltParamDecl): Option[FeltReference] =
      Some(FeltParamRef(d.refName, d))

  }

}

/**
 * A path reference that resolves to a parameter access
 */
final case
class FeltParamRef(refName: FeltPathExpr, refDecl: FeltParamDecl) extends FeltReference
  with FeltParamPolyContains with FeltParamValScal
  with FeltParamValSet with FeltParamValArr with FeltParamValMap {

  override val nodeName: String = "felt-param-ref"

  override val isMutable: Boolean = refDecl.isMutable

  def varName: String = refDecl.varName

  def parameterNull: String = s"${refDecl.externalName}_Null"

  def parameterValue: String = s"${refDecl.externalName}_Val"

  def builderVar: String = s"${refDecl.externalName}_builder"

  def typeDeclaration: FeltTypeDecl = refDecl.typeDeclaration

  def initializer: FeltExpression = refDecl.initializer

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def canInferTypes: Boolean = true

  override
  def resolveTypes: this.type = {
    feltType = refDecl.feltType
    this
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  code generation
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode = {
    feltType.form match {
      case BrioValueScalarRelation => genValScalDecl
      case BrioValueSetRelation => genValSetDecl
      case BrioValueArrayRelation => genValArrDecl
      case BrioValueMapRelation => genValMapDecl
      case _ => throw FeltException(location, s"can't gen declare -- invalid param form ${typeDeclaration.feltType.form}")
    }
  }

  override
  def generatePrepare(implicit cursor: FeltCodeCursor): FeltCode = {
    typeDeclaration.feltType.form match {
      case BrioValueScalarRelation => genValScalPrepare
      case BrioValueSetRelation => genValSetPrepare
      case BrioValueArrayRelation => genValArrPrepare
      case BrioValueMapRelation => genValMapPrepare
      case _ => throw FeltException(location, s"can't gen prepare -- invalid param form ${typeDeclaration.feltType.form}")
    }
  }

  override
  def generateRelease(implicit cursor: FeltCodeCursor): FeltCode = {
    typeDeclaration.feltType.form match {
      case BrioValueScalarRelation => genValScalRelease
      case BrioValueSetRelation => genValSetRelease
      case BrioValueArrayRelation => genValArrRelease
      case BrioValueMapRelation => genValMapRelease
      case _ => throw FeltException(location, s"can't gen release -- invalid param form ${typeDeclaration.feltType.form}")
    }
  }


  override
  def generateReferenceUpdate(op: FeltUpdateOp)(implicit cursor: FeltCodeCursor): FeltCode = ???

  override
  def generateReferenceAssign(implicit cursor: FeltCodeCursor): FeltCode = {
    throw FeltException(location, s"'${this.normalizedSource.condensed}' can't write to a parameter")
  }

  override
  def generateReferenceRead(implicit cursor: FeltCodeCursor): FeltCode = {
    refDecl.typeDeclaration.feltType.form match {
      case BrioValueScalarRelation => genValScalRead
      case BrioValueSetRelation => genValSetRead
      case BrioValueArrayRelation => genValArrRead
      case BrioValueMapRelation => genValMapRead
      case _ => ???
    }
  }


}
