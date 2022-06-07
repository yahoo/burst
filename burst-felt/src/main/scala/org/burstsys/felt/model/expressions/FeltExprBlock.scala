/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions

import org.burstsys.felt.model.literals.FeltLiteral
import org.burstsys.felt.model.reference.names.FeltNamedNode
import org.burstsys.felt.model.tree.code.{FeltNoCode, _}
import org.burstsys.felt.model.tree.source._
import org.burstsys.felt.model.tree.{FeltLocation, _}
import org.burstsys.felt.model.types.FeltType
import org.burstsys.felt.model.variables.local.FeltLocVarDecl
import org.burstsys.felt.model.visits.decl.{FeltActionDecl, FeltStaticVisitDecl}
import org.burstsys.vitals.strings._

import scala.language.postfixOps
import scala.reflect.ClassTag

/**
 * A ''scoped'' sequence of expressions managed as a coordinated block with commonality such as locally bound/visible
 * variables. In this context, we call each expression a 'statement'. Expression blocks are the primary way that
 * Felt expressions are 'executed' as in they are evaluated as part of a
 * [[FeltActionDecl]] in a [[FeltStaticVisitDecl]].
 * Expression blocks can be nested in side other expression blocks e.g.
 * [[org.burstsys.felt.model.expressions.flow.conditional.FeltCondExpr]] which has an expr block in each if and else
 * clause.
 */
trait FeltExprBlock extends FeltExpression with FeltNamedNode {

  final override val nodeName = "felt-eblk"

  /**
   * does this expression block have a return type/value?
   * If so we must make sure we do not attempt to return a value to parent scope
   *
   * @return
   */
  final
  def outerBlock: Boolean = _outerBlock

  final
  def outerBlock_=(s: Boolean): Unit = _outerBlock = s

  private[this]
  var _outerBlock: Boolean = false

  /**
   * the sequence of statements in the block
   *
   * @return
   */
  def statements: Array[FeltExpression]

  final
  def isEmpty: Boolean = variables.isEmpty && statements.isEmpty

  final
  def nonEmpty: Boolean = !isEmpty

  /**
   * each expression block has an optional prefix of locally visible variables
   *
   * @return
   */
  def variables: Array[FeltLocVarDecl]

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////


  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] = {
    rule(this) ++ statements.treeApply(rule) ++ variables.treeApply(rule)
  }

  final override
  def children: Array[_ <: FeltNode] = statements ++ variables

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = variables.canInferTypes && statements.canInferTypes

  final override
  def resolveTypes: this.type = {
    variables.resolveTypes()
    statements.resolveTypes()
    feltType = if (statements.isEmpty) FeltType.unit else statements.last.resolveTypes.feltType
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceToLiteral: Option[FeltLiteral] = None

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltExprBlock = new FeltExprBlock {
    sync(FeltExprBlock.this)
    final override val statements: Array[FeltExpression] = FeltExprBlock.this.statements.map(_.reduceStatics.resolveTypes)
    final override val variables: Array[FeltLocVarDecl] = FeltExprBlock.this.variables.map(_.reduceStatics.resolveTypes)
    final override val location: FeltLocation = FeltExprBlock.this.location
    final override val nsName = FeltExprBlock.this.nsName
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    if (statements.isEmpty) return FeltNoCode

    val callerCursor = cursor

    def exprBlkVar(variable: FeltLocVarDecl)(implicit cursor: FeltCodeCursor): FeltCode =
      s"""|$I${variable.generateDeclaration}""".stripMargin

    def exprBlkVars(implicit cursor: FeltCodeCursor): FeltCode =
      if (variables.isEmpty) FeltNoCode else
        s"""|${variables.map(exprBlkVar(_)(cursor indentRight)).stringify}""".stripMargin

    def exprBlkStat(statIndex: Int, expression: FeltExpression)(implicit cursor: FeltCodeCursor): FeltCode = {
      val expressionCursor = cursor indentRight 1
      val rangeReturn =
        if (!expression.feltType.unitTypeType && statIndex == statements.length)
          s"\n${calleeRangeReturn(callerCursor, expressionCursor, nodeName)(cursor indentRight 1)}"
        else FeltNoCode
      s"""|
          |$I{ // $nodeName-stat#$statIndex [${expression.printSource}]
          |${callerRangeDeclare(expression.feltType, s"$nodeName-stat#$statIndex")(expressionCursor)}${expression.generateExpression(expressionCursor indentRight 1)}$rangeReturn
          |$I}""".stripMargin
    }

    def expressionBlkStats(implicit cursor: FeltCodeCursor): FeltCode = {
      var statIndex = 0
      s"""|${
        statements.map {
          expression =>
            statIndex += 1
            s"""|${exprBlkStat(statIndex, expression)(cursor indentRight 1 scopeDown)}""".stripMargin
        }.stringify
      }""".stripMargin
    }

    s"""|
        |$I{ // ${M(this)}$exprBlkVars$expressionBlkStats
        |$I}""".stripMargin

  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String = {

    val variableSource =
      if (variables.isEmpty) FeltNoCode else
        s"""
           |${variables.map(_.normalizedSource(index + 1).singleLineEnd).stringify.singleLineEnd}""".stripMargin

    val expressionSource =
      if (statements.isEmpty) FeltNoCode else
        s"""
           |${statements.map(_.normalizedSource(index + 1).singleLineEnd).stringify.singleLineEnd}""".stripMargin

    s"""|$S{$variableSource$expressionSource
        |$S}""".stripMargin
  }

}

