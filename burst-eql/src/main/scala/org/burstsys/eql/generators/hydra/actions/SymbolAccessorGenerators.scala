/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.actions

import org.burstsys.eql.generators.ActionPhase.ActionPhase
import org.burstsys.eql.generators._
import org.burstsys.eql.generators.hydra.utils.CodeBlock
import org.burstsys.eql.planning.escapeIdentifierName
import org.burstsys.eql.{AnalysisPropertyName, GlobalContext, qualifiedName}
import org.burstsys.motif.motif.tree.data.ParameterAccessor
import org.burstsys.motif.motif.tree.values.FunctionExpression
import org.burstsys.motif.symbols.functions.Functions
import org.burstsys.motif.symbols.functions.LastPathIsCompleteFunction.LastPathIsCompleteContext
import org.burstsys.motif.symbols.functions.LastPathStepTimeFunction.LastPathStepTimeContext

import scala.collection.JavaConverters._

trait SymbolAccessorGenerators extends Any {
  class ParameterAccessorSourceGenerator(exp: ParameterAccessor) extends ActionSourceGenerator {
    override
    def generateSource()(implicit context: GlobalContext): CodeBlock = {
      s"${context(AnalysisPropertyName)}.${escapeIdentifierName(exp.getName)}"
    }
  }

  abstract class FunctionExpressionSourceGenerator(exp: FunctionExpression) extends ActionSourceGenerator {
    override
    def generateSource()(implicit context: GlobalContext): CodeBlock = {
      s"${qualifiedName(exp.getFunctionName)}"
    }

    override def phase(): ActionPhase = {
      exp.getParms.asScala.map(_.phase).reduce { (o, t) => if (o > t) o else t }
    }
  }

  def chooseGenerator(exp: FunctionExpression):FunctionExpressionSourceGenerator = {
    exp.getFunctionName match {
      case Functions.SIZE =>
        new LengthSourceGenerator(exp)
      case Functions.DATETIME =>
        new DateTimeSourceGenerator(exp)
      case Functions.FREQUENCY =>
        new FrequencySourceGenerator(exp)
      case Functions.SPLIT =>
        new SplitSourceGenerator(exp)
      case Functions.ENUM =>
        new EnumSourceGenerator(exp)
      case Functions.LAST_PATH_IS_COMPLETE =>
        new LastPathIsCompleteSourceGenerator(exp)
      case Functions.LAST_PATH_STEP_TIME =>
        new LastPathStepTimeSourceGenerator(exp)
      case bad: String =>
        throw new IllegalStateException(s"Function $bad has no generator")
    }
  }

  class LengthSourceGenerator(exp: FunctionExpression) extends FunctionExpressionSourceGenerator(exp) {
    override def generateSource()(implicit context: GlobalContext): CodeBlock = {
      val e = exp.getParms.get(0).generateSource()
      assert(e.length == 1)
      s"size(${e.head})"
    }
  }

  class DateTimeSourceGenerator(exp: FunctionExpression) extends FunctionExpressionSourceGenerator(exp) {
    override def generateSource()(implicit context: GlobalContext): CodeBlock = {
      val e = exp.getParms.get(0).generateSource()
      assert(e.length == 1)
      s"datetime(${e.head})"
    }
  }

  class FrequencySourceGenerator(exp: FunctionExpression) extends FunctionExpressionSourceGenerator(exp) {
    override def generateSource()(implicit context: GlobalContext): CodeBlock = {
      val e = exp.getParms.get(0).generateSource()
      assert(e.length == 1)
      s"${e.head}"
    }

    override def phase(): ActionPhase = ActionPhase.Post
  }

  class SplitSourceGenerator(exp: FunctionExpression) extends FunctionExpressionSourceGenerator(exp) {
    override def generateSource()(implicit context: GlobalContext): CodeBlock = {
      val parms = exp.getParms.asScala
      val e = parms.head.generateSource()
      val b = parms.drop(1).flatMap(_.generateSource).mkString(",")
      assert(e.length == 1)
      s"split(${e.head}, $b)"
    }
  }

  class EnumSourceGenerator(exp: FunctionExpression) extends FunctionExpressionSourceGenerator(exp) {
    override def generateSource()(implicit context: GlobalContext): CodeBlock = {
      val parms = exp.getParms.asScala
      val e = parms.head.generateSource()
      val b = parms.drop(1).flatMap(_.generateSource).mkString(",")
      assert(e.length == 1)
      s"enum(${e.head}, $b)"
    }
  }

  class LastPathIsCompleteSourceGenerator(exp: FunctionExpression) extends FunctionExpressionSourceGenerator(exp) {
    override def generateSource()(implicit context: GlobalContext): CodeBlock = {
      val c: LastPathIsCompleteContext = exp.getContext.asInstanceOf[LastPathIsCompleteContext]
      s"routeLastPathIsComplete(${c.funnelName})"
    }

    override def phase(): ActionPhase = ActionPhase.Post
  }

  class LastPathStepTimeSourceGenerator(exp: FunctionExpression) extends FunctionExpressionSourceGenerator(exp) {
    override def generateSource()(implicit context: GlobalContext): CodeBlock = {
      val c: LastPathStepTimeContext = exp.getContext.asInstanceOf[LastPathStepTimeContext]
      s"routeLastStepTime(${c.funnelName})"
    }

    override def phase(): ActionPhase = ActionPhase.Post
  }
}
