/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.actions

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.generators.ActionPhase.ActionPhase
import org.burstsys.eql.generators._
import org.burstsys.eql.generators.hydra.utils.CodeBlock
import org.burstsys.motif.motif.tree.constant.Constant
import org.burstsys.motif.motif.tree.values._

trait ValueExpressionGenerators extends Any {
  class BinaryValueExpressionSourceGenerator(exp: BinaryValueExpression) extends AnyRef with ActionSourceGenerator {
    override
    def generateSource()(implicit context: GlobalContext): CodeBlock = {
      val left = exp.getLeft.generateSource()
      val right = exp.getRight.generateSource()
      assert(left.length  == 1 && right.length == 1)
      val op = exp.getOp.generateMotif(0)
      s"(${left.head} $op ${right.head})"
    }

    override def phase(): ActionPhase = {
      if (exp.getLeft.phase() > exp.getRight.phase())
        exp.getLeft.phase()
      else
        exp.getRight.phase()
    }
  }

  class UnaryValueSourceGenerator(exp: UnaryValueExpression) extends AnyRef with ActionSourceGenerator {
    override
    def generateSource()(implicit context: GlobalContext): CodeBlock = {
      val e = exp.getExpr.generateSource()
      assert(e.length == 1)
      val op = exp.getOp.generateMotif(0)
      s"$op ${e.head}"
    }

    override def phase(): ActionPhase = exp.getExpr.phase()
  }

  class ConstantSourceGenerator(exp: Constant) extends AnyRef with ActionSourceGenerator {
    override
    def generateSource()(implicit context: GlobalContext): CodeBlock = {
      if (exp.isNull)
        s"null"
      else if (exp.isString) {
        val sVal = exp.getDataValue.toString
        val cleanVal = sVal.replaceAll("""(\\)*(")""", """\\$2""").
          replace("\t", "\\t").
          replace("\n", "\\n").
          replace("\r", "\\r")
        '"' + cleanVal + '"'
      } else
        s"${exp.getDataValue}"
    }
  }

  class ConstantValSourceGenerator(lit: AnyRef) extends AnyRef with ActionSourceGenerator {
    override
    def generateSource()(implicit context: GlobalContext): CodeBlock = {
      if (lit == null)
        s"null"
      else if (lit.isInstanceOf[String]) {
        val sVal = lit.toString
        val cleanVal = sVal.replaceAll("""(\\)*(")""", """\\$2""").
          replace("\t", "\\t").
          replace("\n", "\\n").
          replace("\r", "\\r")
        '"' + cleanVal + '"'
      } else
        s"${lit.toString}"
    }
  }
  class NowValueSourceGenerator(exp: NowValueExpression) extends AnyRef with ActionSourceGenerator {
    override
    def generateSource()(implicit context: GlobalContext): CodeBlock = {
      s"now()"
    }
  }

  class DateTimeOrdinalSourceGenerator(exp: DateTimeOrdinalExpression) extends AnyRef with ActionSourceGenerator {
    override
    def generateSource()(implicit context: GlobalContext): CodeBlock = {
      val e = exp.getExpr.generateSource()
      assert(e.length == 1)
      val op = ordinalFunctionToHydraDecl(exp.getOp)
      s"$op(${e.head})"
    }

    override def phase(): ActionPhase = exp.getExpr.phase()
  }

  class DateTimeQuantumSourceGenerator(exp: DateTimeQuantumExpression) extends AnyRef with ActionSourceGenerator {
    override
    def generateSource()(implicit context: GlobalContext): CodeBlock = {
      val e = exp.getExpr.generateSource()
      assert(e.length == 1)
      val op = quantumFunctionToHydraDecl(exp.getOp)
      s"$op(${e.head})"
    }

    override def phase(): ActionPhase = exp.getExpr.phase()
  }

  class DateTimeConversionSourceGenerator(exp: DateTimeConversionExpression) extends AnyRef with ActionSourceGenerator {
    override
    def generateSource()(implicit context: GlobalContext): CodeBlock = {
      val e = exp.getExpr.generateSource()
      assert(e.length == 1)
      val op = conversionFunctionToHydraDecl(exp.getOp)
      s"$op(${e.head})"
    }

    override def phase(): ActionPhase = exp.getExpr.phase()
  }

  class CastValueExpressionSourceGenerator(exp: CastValueExpression) extends AnyRef with ActionSourceGenerator {
    override
    def generateSource()(implicit context: GlobalContext): CodeBlock = {
      val e = exp.getExpr.generateSource()
      assert(e.length == 1)
      val typ = exp.getType.generateMotif(0).toLowerCase()
      s"cast(${e.head} as $typ)"
    }

    override def phase(): ActionPhase = exp.getExpr.phase()
  }
}
