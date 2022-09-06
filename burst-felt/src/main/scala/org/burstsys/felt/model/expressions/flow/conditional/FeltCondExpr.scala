/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.flow.conditional

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.expressions.bool.FeltBoolExpr
import org.burstsys.felt.model.expressions.flow.FeltFlowExpr
import org.burstsys.felt.model.expressions.{FeltExprBlock, FeltExpression}
import org.burstsys.felt.model.literals.primitive.FeltNullPrimitive
import org.burstsys.felt.model.tree._
import org.burstsys.felt.model.tree.source._
import org.burstsys.felt.model.types.FeltType
import org.burstsys.vitals.strings._

import scala.reflect.ClassTag


/**
 * A value expression that is comprised of a set of one ''if'', zero or more ''else-if'',
 * and zero or one ''else'' clauses
 */
trait FeltCondExpr extends FeltFlowExpr with FeltCondGen {

  final override val nodeName = "felt-cond-expr"

  /**
   * the boolean expression that is the conditional ''if'' part
   *
   * @return
   */
  def ifConditionTest: FeltBoolExpr

  /**
   * the expression block that is chosen if the conditional ''if'' resolves to true at runtime
   *
   * @return
   */
  def ifExpressionBlock: FeltExprBlock

  /**
   * the set of boolean expressions that are the conditional ''else if'' part
   *
   * @return
   */
  def elseIfConditionTest: Array[FeltBoolExpr]

  /**
   * the expression blocks that are chosen if a respective conditional ''else if'' resolves to true at runtime
   *
   * @return
   */
  def elseIfExpressionBlock: Array[FeltExprBlock]

  /**
   * the optional expression block that is chosen if all of the ''if'' or ''else if'' conditions resolve to null
   *
   * @return
   */
  def elseNullExpressionBlock: Option[FeltExprBlock]

  /**
   * the expression block that is chosen if all of the ''if'' or ''else if'' conditions resolve to false or null
   *
   * @return
   */
  def elseExpressionBlock: Option[FeltExprBlock]

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ ifConditionTest.treeApply(rule) ++ ifExpressionBlock.treeApply(rule) ++ elseIfConditionTest.treeApply(rule) ++
      elseIfExpressionBlock.treeApply(rule) ++ elseNullExpressionBlock.treeApply(rule) ++ elseExpressionBlock.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = ifConditionTest.asArray ++ ifExpressionBlock.asArray ++ elseIfConditionTest ++
    elseIfExpressionBlock ++ elseNullExpressionBlock ++ elseExpressionBlock

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean =
    ifConditionTest.canInferTypes && ifExpressionBlock.canInferTypes && elseIfConditionTest.canInferTypes &&
      elseIfExpressionBlock.canInferTypes && elseNullExpressionBlock.canInferTypes && elseExpressionBlock.canInferTypes

  final override
  def resolveTypes: this.type = {
    ifConditionTest.resolveTypes
    ifExpressionBlock.resolveTypes
    elseIfConditionTest.foreach(_.resolveTypes)
    elseIfExpressionBlock.foreach(_.resolveTypes)
    elseNullExpressionBlock.foreach(_.resolveTypes)
    elseExpressionBlock.foreach(_.resolveTypes)
    feltType = FeltType.combine(
      (Array( ifExpressionBlock.feltType) ++
        elseIfConditionTest.map(_.feltType) ++
        elseIfExpressionBlock.map(_.feltType) ++
        elseNullExpressionBlock.map(_.feltType) ++
        elseExpressionBlock.map(_.feltType)).toIndexedSeq
        : _*
    )
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltCondExpr = {
    // make sure we have boolean expressions for if tests
    val ifConditionReduction = ifConditionTest.reduceStatics match {
      case na: FeltNullPrimitive =>
        throw FeltException(location, s"$printSource: if condition test currently cannot reduce to a null literal")
      case be: FeltBoolExpr => be
      case _ => throw FeltException(location, s"$printSource: if condition test does not reduce to a boolean expression")
    }
    val elseIfConditionTests = elseIfConditionTest.map {
      ct =>
        ct.reduceStatics match {
          case na: FeltNullPrimitive =>
            throw FeltException(location, s"$printSource: else if condition test currently cannot reduce to a null literal")
          case be: FeltBoolExpr => be
          case _ => throw FeltException(location, s"$printSource: else if condition test does not reduce to a boolean expression")
        }
    }
    new FeltCondExpr {
      sync(FeltCondExpr.this)
      final override val ifConditionTest: FeltBoolExpr = ifConditionReduction
      final override val ifExpressionBlock: FeltExprBlock = FeltCondExpr.this.ifExpressionBlock.reduceStatics.resolveTypes
      final override val elseIfConditionTest: Array[FeltBoolExpr] = elseIfConditionTests
      final override val elseIfExpressionBlock: Array[FeltExprBlock] = FeltCondExpr.this.elseIfExpressionBlock.map(_.reduceStatics.resolveTypes)
      final override val elseNullExpressionBlock: Option[FeltExprBlock] = FeltCondExpr.this.elseNullExpressionBlock match {
        case None => None
        case Some(e) => Some(e.reduceStatics.resolveTypes)
      }
      final override val elseExpressionBlock: Option[FeltExprBlock] = FeltCondExpr.this.elseExpressionBlock match {
        case None => None
        case Some(e) => Some(e.reduceStatics.resolveTypes)
      }
      final override val location: FeltLocation = FeltCondExpr.this.location
    }

  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String = {

    def elsePrint(implicit index: Int): String = elseExpressionBlock match {
      case None => ""
      case Some(e) =>
        val eSource = e.normalizedSource
        s"""|
            |${S}else
            |$eSource""".stripMargin
    }

    def elseIfPrint(implicit index: Int): String = (for (i <- elseIfConditionTest.indices) yield {
      val elseIfExpBlk = elseIfExpressionBlock(i).normalizedSource
      s"""|
          |${S}else if(${elseIfConditionTest(i).normalizedSource})
          |$elseIfExpBlk""".stripMargin
    }).stringify.trimAtEnd

    val s =
      s"""|
          |${S}if( ${ifConditionTest.normalizedSource} )
          |${ifExpressionBlock.normalizedSource}$elseIfPrint$elsePrint""".stripMargin
    s
  }

}
