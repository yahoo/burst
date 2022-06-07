/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.actions.temporaries

import java.util.Collections
import java.{lang, util}

import org.burstsys.eql.actions.TemporaryExpression
import org.burstsys.eql.generators.ActionPhase.ActionPhase
import org.burstsys.eql.generators.hydra.actions.TemporaryAccessorSourceGenerator
import org.burstsys.eql.generators.{ActionPhase, DeclarationScope, Var}
import org.burstsys.motif.common.{DataType, NodeType}
import org.burstsys.motif.motif.tree.constant.Constant
import org.burstsys.motif.motif.tree.expression.{Evaluation, Expression, Parent}
import org.burstsys.motif.motif.tree.values.ValueExpression
import org.burstsys.motif.paths.Path
import org.burstsys.motif.symbols.PathSymbols

import scala.collection.JavaConverters._

/**
  * This context can be placed in the motif parse tree and so mimics an expression
  */
class TemporaryExpressionContext(tempName: String, val value: ValueExpression)
  extends TemporaryExpression with TemporaryAccessorSourceGenerator {

  override val tempVar: Var = Var(tempName, DeclarationScope.Analysis, value.getDtype)

  override def name: String = tempVar.name

  override def getLowestVisitPath: Path = getLowestEvaluationPoint

  def getLowestEvaluationPoint: Path = value.getLowestEvaluationPoint

  override def phase(): ActionPhase = ActionPhase.Post

  override def canReduceToConstant: lang.Boolean = value.canReduceToConstant

  override def reduceToConstant: Constant = value.reduceToConstant()

  override def getDtype: DataType = value.getDtype

  override def bind(parsingSymbols: PathSymbols, stack: util.Stack[Evaluation]): Unit = {
    stack.push(this)
    value.bind(parsingSymbols, stack)
    stack.pop
  }

  override def validate(parsingSymbols: PathSymbols, scope: Path, stack: util.Stack[Evaluation]): Unit = {
    stack.push((this))
    value.validate(parsingSymbols, scope, stack)
    stack.pop
  }

  override def optimize(pathSymbols: PathSymbols): Evaluation = this

  override def explain(): String = value.explain()

  override def explain(level: Int): String = value.explain(level)

  override def exportAsJson(): String = value.exportAsJson

  override def getNodeType: NodeType = value.getNodeType

  override def generateMotif(level: Int): String = value.generateMotif(level)

  override def getChildren: util.List[Expression] = Collections.singletonList(value)

  override def childCount(): Int = 1

  override def getChild(index: Int): Expression = {
    if (index == 0) value else throw new IndexOutOfBoundsException
  }

  override def setChild(index: Int, value: Expression): Expression = {
    if (index == 0) {
      // only allow setting to the same values
      if (value == this.value)
        value
      else
        throw new UnsupportedOperationException
    }
    else
      throw new IndexOutOfBoundsException
  }

  override def walkTree(checker: Parent.NodeWalker): Unit =  {
    if (checker == null)  return
    for (e: Expression <- value.getChildren.asScala) {
      if (e != null)
        e.walkTree(checker)
    }
    checker.check(this)
  }
}
