/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.variables.local.ref

import org.burstsys.brio.model.schema.types.{BrioValueArrayRelation, BrioValueMapRelation, BrioValueScalarRelation, BrioValueSetRelation}
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.expressions.assign.FeltUpdateOp
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.reference.{FeltReference, FeltStdRefResolver}
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.{FeltCodeCursor, _}
import org.burstsys.felt.model.types.FeltTypeDecl
import org.burstsys.felt.model.variables.local.FeltLocVarDecl
import org.burstsys.felt.model.variables.local.ref.mutable.{FeltLocVarValArr, FeltLocVarValMap, FeltLocVarValSet}
import org.burstsys.felt.model.variables.local.ref.poly.FeltLocVarPolyContains
import org.burstsys.felt.model.variables.local.ref.primitive.FeltLocVarValScal

import scala.language.postfixOps


object FeltLocVarRef {

  final case
  class FeltLocVarRefResolver(global: FeltGlobal) extends FeltStdRefResolver[FeltLocVarDecl] {

    override val resolverName: String = "local.var"

    override protected def addResolution(refName: FeltPathExpr, d: FeltLocVarDecl): FeltReference =
      FeltLocVarRef(refName, d)

    override protected def addNomination(d: FeltLocVarDecl): Option[FeltReference] =
      Some(FeltLocVarRef(d.refName, d))

  }

}

/**
 * A path reference that resolves to a variable access scoped to an expression block. These are
 * lexically scoped within a code generation translation unit (they are always within the same scala function)
 */
final case
class FeltLocVarRef(refName: FeltPathExpr, refDecl: FeltLocVarDecl) extends FeltReference
  with FeltLocVarPolyContains
  with FeltLocVarValScal with FeltLocVarValArr with FeltLocVarValSet with FeltLocVarValMap {

  override val nodeName: String = "felt-loc-var-ref"

  override val isMutable: Boolean = refDecl.isMutable

  def varName: String = refDecl.varName

  def typeDeclaration: FeltTypeDecl = refDecl.typeDeclaration

  def initializer: FeltExpression = refDecl.initializer

  def variableValue(implicit cursor: FeltCodeCursor): FeltCode = s"${refName.fullPathNoQuotes}_value"

  def variableNull(implicit cursor: FeltCodeCursor): FeltCode = s"${refName.fullPathNoQuotes}_nullity"


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
  //  CODE GENERATION
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def generateReferenceUpdate(op: FeltUpdateOp)(implicit cursor: FeltCodeCursor): FeltCode = ???

  override
  def generateReferenceAssign(implicit cursor: FeltCodeCursor): FeltCode = {
    refDecl.typeDeclaration.feltType.form match {
      case BrioValueScalarRelation => genValScalWrite
      case BrioValueSetRelation => genValSetWrite
      case BrioValueArrayRelation => genValArrWrite
      case BrioValueMapRelation => genValMapWrite
      case _ => ???
    }
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

  override
  def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode = {
    typeDeclaration.feltType.form match {
      case BrioValueScalarRelation => genValScalDecl
      case BrioValueSetRelation => genValSetDecl
      case BrioValueArrayRelation => genValArrDecl
      case BrioValueMapRelation => genValMapDecl
      case _ => throw FeltException(location, s"invalid local var form ${typeDeclaration.feltType.form}")
    }
  }

  override
  def generatePrepare(implicit cursor: FeltCodeCursor): FeltCode =
    typeDeclaration.feltType.form match {
      case BrioValueScalarRelation => genValScalPrep
      case BrioValueSetRelation => genValSetPrep
      case BrioValueArrayRelation => genValArrPrepare
      case BrioValueMapRelation => genValMapPrep
      case _ => throw FeltException(location, s"invalid local var form ${typeDeclaration.feltType.form}")
    }

  override def generateRelease(implicit cursor: FeltCodeCursor): FeltCode = ???
}
