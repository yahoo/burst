/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.variables.global.ref

import org.burstsys.brio.model.schema.types.{BrioValueArrayRelation, BrioValueMapRelation, BrioValueScalarRelation, BrioValueSetRelation}
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.expressions.assign.FeltUpdateOp
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.reference.{FeltReference, FeltStdRefResolver}
import org.burstsys.felt.model.runtime.FeltRuntime
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.{FeltCodeCursor, _}
import org.burstsys.felt.model.types.FeltTypeDecl
import org.burstsys.felt.model.variables.global.FeltGlobVarDecl
import org.burstsys.felt.model.variables.global.ref.mutable.{FeltGlobVarValArr, FeltGlobVarValMap, FeltGlobVarValSet}
import org.burstsys.felt.model.variables.global.ref.poly.FeltGlobVarPolyContains
import org.burstsys.felt.model.variables.global.ref.primitive.FeltGlobVarValScal

import scala.language.postfixOps


object FeltGlobVarRef {

  final case class FeltGlobalVarRefResolver(global: FeltGlobal) extends FeltStdRefResolver[FeltGlobVarDecl] {

    override val resolverName: String = "glob.var"

    override protected def addResolution(refName: FeltPathExpr, d: FeltGlobVarDecl): FeltReference =
      FeltGlobVarRef(refName, d)

    override protected def addNomination(d: FeltGlobVarDecl): Option[FeltReference] =
      Some(FeltGlobVarRef(d.refName, d))

  }

}

/**
 * A path reference that resolves to a variable access
 */
final case
class FeltGlobVarRef(refName: FeltPathExpr, refDecl: FeltGlobVarDecl) extends FeltReference
  with FeltGlobVarPolyContains
  with FeltGlobVarValArr with FeltGlobVarValSet with FeltGlobVarValScal with FeltGlobVarValMap {

  override val nodeName: String = "felt-glob-var-ref"

  override val isMutable: Boolean = refDecl.isMutable

  def varName: String = refDecl.varName

  def typeDeclaration: FeltTypeDecl = refDecl.typeDeclaration

  def initializer: FeltExpression = refDecl.initializer

  def variableNull: String = s"${refDecl.externalName}_Null"

  def variableValue: String = s"${refDecl.externalName}_Val"

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  TYPE INFERENCE
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def canInferTypes: Boolean = true

  override def resolveTypes: this.type = {
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
      case _ => throw FeltException(location, s"invalid global var form ${typeDeclaration.feltType.form}")
    }
  }

  override
  def generateReferenceRead(implicit cursor: FeltCodeCursor): FeltCode = {
    refDecl.typeDeclaration.feltType.form match {
      case BrioValueScalarRelation => genValScalRead
      case BrioValueSetRelation => genValSetRead
      case BrioValueArrayRelation => genValArrRead
      case BrioValueMapRelation => genValMapRead
      case _ => throw FeltException(location, s"invalid global var form ${typeDeclaration.feltType.form}")
    }
  }

  override
  def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode = {
    typeDeclaration.feltType.form match {
      case BrioValueScalarRelation => genValScalDecl
      case BrioValueSetRelation => genValSetDecl
      case BrioValueArrayRelation => genValArrDecl
      case BrioValueMapRelation => genValMapDecl
      case _ => throw FeltException(location, s"invalid global var form ${typeDeclaration.feltType.form}")
    }
  }

  /**
   * we separate out the initializer so that we can put it into the [[FeltRuntime]] prepare routine so initialization
   * happens each time the runtime is used
   *
   * @param cursor
   * @return
   */
  override
  def generatePrepare(implicit cursor: FeltCodeCursor): FeltCode = {
    typeDeclaration.feltType.form match {
      case BrioValueScalarRelation => genValScalPrep
      case BrioValueSetRelation => genValSetPrep
      case BrioValueArrayRelation => genValArrPrep
      case BrioValueMapRelation => genValMapPrep
      case _ => throw FeltException(location, s"invalid global var form ${typeDeclaration.feltType.form}")
    }
  }

  override def generateRelease(implicit cursor: FeltCodeCursor): FeltCode = ???

}
