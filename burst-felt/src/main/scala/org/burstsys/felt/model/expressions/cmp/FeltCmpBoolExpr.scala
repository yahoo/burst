/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.cmp

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.model.schema.types._
import org.burstsys.felt.model.brio.FeltReachValidator
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.expressions.bool.FeltBoolExpr
import org.burstsys.felt.model.expressions.op.FeltBinOpExpr
import org.burstsys.felt.model.literals.FeltLiteral
import org.burstsys.felt.model.tree.FeltLocation
import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.types.FeltType

import scala.language.postfixOps


/**
 * a  ''left hand side'' `OPERATOR` ''right hand side'' that returns a boolean/logical datatype
 */
trait FeltCmpBoolExpr extends FeltBoolExpr with FeltBinOpExpr with FeltReachValidator {

  final override val nodeName = "felt-cmp-bool-expr"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * the boolean comparison operation
   *
   * @return
   */
  def op: FeltCmpBoolOp

  /**
   * the left hand side expression of the comparison
   *
   * @return
   */
  def lhs: FeltExpression

  /**
   * the right hand side expression of the comparison
   *
   * @return
   */
  def rhs: FeltExpression

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  validation
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * validate references for a given traversal point
   *
   * @param traversePt
   */
  override final
  def validateBrioReferences(traversePt: BrioNode): Unit = {
    val relations = Array(BrioValueScalarRelation, BrioValueMapRelation, BrioValueVectorRelation, BrioReferenceScalarRelation, BrioReferenceVectorRelation)
    validateReach(traversePt, lhs, relations.toIndexedSeq: _*)
    validateReach(traversePt, rhs, relations.toIndexedSeq: _*
    )
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = lhs.canInferTypes && rhs.canInferTypes

  final override
  def resolveTypes: this.type = {
    lhs.resolveTypes
    rhs.resolveTypes
    // TODO check to see if these are comparable
    feltType = FeltType.valScal[Boolean]
    op.feltType = FeltType.valScal[Boolean]
    this
  }

  final override
  def reduceToLiteral: Option[FeltLiteral] =
    op.reduceToLiteral(lhs.reduceStatics.reduceToLiteral, rhs.reduceStatics.reduceToLiteral)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltExpression = {
    val lhsLit = lhs.reduceToLiteral
    val rhsLit = rhs.reduceToLiteral
    op.reduceToLiteral(lhsLit, rhsLit) match {
      case Some(bl) => bl
      case None => new FeltCmpBoolExpr {
        sync(FeltCmpBoolExpr.this)
        final override val op: FeltCmpBoolOp = FeltCmpBoolExpr.this.op
        final override val lhs: FeltExpression = FeltCmpBoolExpr.this.lhs.reduceStatics.resolveTypes
        final override val rhs: FeltExpression = FeltCmpBoolExpr.this.rhs.reduceStatics.resolveTypes
        final override val location: FeltLocation = FeltCmpBoolExpr.this.location
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {

    /**
     * if the RHS evaluates to a null AND this is an '==' or '!=' then generate
     * special code
     */
    rhs.reduceToNull match {
      case None => scopeBinaryOp(nodeName) // no null literal
      case Some(_) =>
        op.determinativeNullComparison match {
          case None => scopeBinaryOp(nodeName) // OP does not support this
          case Some(value) => determinativeNullComparison(nodeName, value) // OP supports comparison to a literal null
        }
    }
  }

  /**
   * special case ``LHS == null``
   *
   * @param tag
   * @param cursor
   * @return
   */
  final
  def determinativeNullComparison(tag: String, value: Boolean)(implicit cursor: FeltCodeCursor): FeltCode = {
    val lhsCursor = cursor indentRight 1 scopeDown;
    s"""|
        |${C("special case for determinative null comparison...")}
        |${I}var ${lhsCursor.callScope.scopeNull}:Boolean = false; var ${lhsCursor.callScope.scopeVal}:${lhs.feltType.valueTypeAsCode} = ${lhs.feltType.valueDefaultAsCode}; // $tag-LHS DECL${lhs.generateExpression(lhsCursor)}
        |${I}if( ${lhsCursor.callScope.scopeNull} ) { ${cursor.callScope.scopeNull} = false; ${cursor.callScope.scopeVal} = $value; } else { ${cursor.callScope.scopeNull} = false; ${cursor.callScope.scopeVal} = !$value; } // $tag-NULL-TEST""".stripMargin
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String = s"${lhs.normalizedSource} ${op.symbol} ${rhs.normalizedSource}"

}
