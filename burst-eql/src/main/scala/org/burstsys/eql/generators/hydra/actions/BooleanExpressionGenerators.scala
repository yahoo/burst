/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.actions

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.generators.ActionPhase.ActionPhase
import org.burstsys.eql.generators._
import org.burstsys.eql.generators.hydra.utils.CodeBlock
import org.burstsys.motif.motif.tree.logical._

import scala.collection.JavaConverters._

trait BooleanExpressionGenerators extends Any {
  class BinaryBooleanSourceGenerator(exp: BinaryBooleanExpression) extends ActionSourceGenerator {
    override
    def generateSource()(implicit context: GlobalContext): CodeBlock = {
      val left = exp.getLeft.generateSource()
      val right = exp.getRight.generateSource()
      assert(left.length  == 1 && right.length == 1)
      val op = exp.getOp.generateMotif(0).toLowerCase()
      s"${left.head} $op ${right.head}"
    }

    override def phase(): ActionPhase = {
      if (exp.getLeft.phase() > exp.getRight.phase())
        exp.getLeft.phase()
      else
        exp.getRight.phase()
    }
  }

  class UnaryBooleanSourceGenerator(exp: UnaryBooleanExpression) extends ActionSourceGenerator {
    override
    def generateSource()(implicit context: GlobalContext): CodeBlock = {
      val e = exp.getExpr.generateSource()
      assert(e.length == 1)
      val op = exp.getOp.generateMotif(0).toLowerCase()
      s"$op ${e.head}"
    }

    override def phase(): ActionPhase = exp.getExpr.phase()
  }

  class BooleanValueSourceGenerator(exp: BooleanValueExpression) extends ActionSourceGenerator {
    override
    def generateSource()(implicit context: GlobalContext): CodeBlock = {
      exp.getExpr.generateSource()
    }

    override def phase(): ActionPhase = exp.getExpr.phase()
  }

  class ValueComparisonBooleanExpressionSourceGenerator(exp: ValueComparisonBooleanExpression) extends ActionSourceGenerator {
    override
    def generateSource()(implicit context: GlobalContext): CodeBlock = {
      val left = exp.getLeft.generateSource()
      val right = exp.getRight.generateSource()
      assert(left.length  == 1 && right.length == 1)
      val op = exp.getOp.generateMotif(0).toLowerCase()
      s"${left.head} $op ${right.head}"
    }

    override def phase(): ActionPhase = {
      if (exp.getLeft.phase() > exp.getRight.phase())
        exp.getLeft.phase()
      else
        exp.getRight.phase()
    }
  }

  class BoundsTestBooleanExpressionSourceGenerator(exp: BoundsTestBooleanExpression) extends ActionSourceGenerator {
    override
    def generateSource()(implicit context: GlobalContext): CodeBlock = {
      val left = exp.getLeft.generateSource()
      val low = exp.getLower.generateSource()
      val up = exp.getUpper.generateSource()
      assert(left.length  == 1 && up.length == 1 && low.length == 1)
      val op = exp.getOp.generateMotif(0).toLowerCase()
      s"${left.head} $op (${low.head},${up.head})"
    }

    override def phase(): ActionPhase = exp.getLeft.phase()
  }

  class NullTestBooleanExpressionSourceGenerator(exp: NullTestBooleanExpression) extends ActionSourceGenerator {
    override
    def generateSource()(implicit context: GlobalContext): CodeBlock = {
      val e = exp.getExpr.generateSource()
      assert(e.length  == 1)
      exp.getOp match {
        case NullTestOperatorType.IS_NOT_NULL =>
          s"${e.head} != null"
        case NullTestOperatorType.IS_NULL =>
          s"${e.head} == null"
      }
    }

    override def phase(): ActionPhase = exp.getExpr.phase()
  }

  class ExplicitMembershipTestBooleanExpressionSourceGenerator(exp: ExplicitMembershipTestBooleanExpression) extends ActionSourceGenerator {
    override
    def generateSource()(implicit context: GlobalContext): CodeBlock = {
      val left = exp.getLeft.generateSource()
      val members = exp.getMembers.asScala
      val valueList: List[String] = members.map{ m =>
        val sl = m.generateSource()
        assert(sl.length == 1)
        sl.head
      }.toList
      assert(left.length  == 1 && valueList.length == members.length)
      val op = exp.getOp.generateMotif(0).toLowerCase()
      s"${left.head} $op (${valueList.mkString(",")})"
    }

    override def phase(): ActionPhase = exp.getLeft.phase()
  }

  class NoopBooleanExpressionSourceGenerator(ph: ActionPhase)
    extends ActionSourceGenerator
  {
    override def phase(): ActionPhase = ph

    override def generateSource()(implicit context: GlobalContext): CodeBlock = "true"
  }
}
