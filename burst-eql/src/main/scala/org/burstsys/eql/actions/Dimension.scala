/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.actions
import org.burstsys.eql.generators.ActionPhase.ActionPhase
import org.burstsys.eql.generators._
import org.burstsys.eql.generators.hydra.actions.{DimensionAssignSourceGenerator, DimensionInsertSourceGenerator}
import org.burstsys.eql.generators.hydra.utils.CodeBlock
import org.burstsys.eql.planning.EqlExpression
import org.burstsys.eql.{GlobalContext, qualifiedName}
import org.burstsys.motif.common.{DataType, NodeType}
import org.burstsys.motif.motif.tree.constant.Constant
import org.burstsys.motif.motif.tree.expression.{Evaluation, Expression, Parent}
import org.burstsys.motif.motif.tree.values.{DateTimeOrdinalOperatorType, DateTimeQuantumOperatorType, ValueExpression}
import org.burstsys.motif.paths.Path
import org.burstsys.motif.symbols.PathSymbols

import java.util.Collections
import java.{lang, util}
import scala.collection.JavaConverters._

trait DimensionSource {
  def getDimensions: Array[Dimension]
}

trait Dimension extends DimensionSource with CubeDeclarationGenerator with QueryAction {
  def getDimensions: Array[Dimension] = Array(this)
}

trait DimensionAssign extends Dimension with DimensionAssignSourceGenerator with ValueExpression with EqlExpression {
  def name: String
  var expression: Expression

  override def getLowestVisitPath: Path = expression.getLowestEvaluationPoint

  override def phase(): ActionPhase = ActionPhase.Pre // TODO actually do a expression.hasAggregates

  def copy(name: String): DimensionAssign
}

class DimensionInsert extends Dimension with DimensionInsertSourceGenerator {

  override def getLowestVisitPath: Path = throw new UnsupportedOperationException

  override def phase(): ActionPhase = ActionPhase.Post // TODO actually do a expression.hasAggregates

  override def generateCubeDeclarationSource(): CodeBlock = CodeBlock.Empty
}

object DimensionInsert {
  def apply():DimensionInsert  = new DimensionInsert()
}

final case class QuantaDimensionAssign
(
  name: String,
  function: DateTimeQuantumOperatorType,
  override var expression: Expression
) extends DimensionAssignContext {
  override def generateCubeDeclarationSource(): CodeBlock = {
    s"${this.name}:${quantumFunctionToHydraDecl(this.function)}[${typeToHydraTypeDecl(this.expression.getDtype)}]"
  }
  override def copy(name: String): QuantaDimensionAssign = QuantaDimensionAssign(name = name, function = this.function, expression = this.expression)

  def getLowestEvaluationPoint: Path = expression.getLowestEvaluationPoint
}

final case class OrdinalDimensionAssign
(
  name: String,
  function: DateTimeOrdinalOperatorType,
  override var expression: Expression
) extends DimensionAssignContext {
  override def generateCubeDeclarationSource(): CodeBlock = {
    s"${this.name}:${ordinalFunctionToHydraDecl(this.function)}[${typeToHydraTypeDecl(this.expression.getDtype)}]"
  }
  override def copy(name: String): OrdinalDimensionAssign = OrdinalDimensionAssign(name = name, function = this.function, expression = this.expression)

  def getLowestEvaluationPoint: Path = expression.getLowestEvaluationPoint
}

final case class SplitDimensionAssign
(
  name: String,
  override  var expression: Expression,
  bounds: List[Expression]
) extends DimensionAssignContext {
  override def generateCubeDeclarationSource(): CodeBlock = {
    val bnds = bounds.flatMap(_.generateSource()(null)).mkString("(", ",", ")")
    s"${this.name}:split[${typeToHydraTypeDecl(expression.getDtype)}]$bnds"
  }

  override def copy(name: String): SplitDimensionAssign = SplitDimensionAssign(name = name, bounds = this.bounds, expression = this.expression)

  override
  def generateSource()(implicit context: GlobalContext): CodeBlock = {
    val e = expression.generateSource()
    assert(e.length == 1)
    s"${qualifiedName(this.name)} = ${e.head}"
  }

  def getLowestEvaluationPoint: Path = expression.getLowestEvaluationPoint
}

final case class EnumDimensionAssign
(
  name: String,
  override var expression: Expression,
  values: List[Expression]
) extends DimensionAssignContext {
  override def generateCubeDeclarationSource(): CodeBlock = {
    val vls = values.flatMap(_.generateSource()(null)).mkString("(", ",", ")")
    s"${this.name}:enum[${typeToHydraTypeDecl(expression.getDtype)}]$vls"
  }

  override def copy(name: String): EnumDimensionAssign = EnumDimensionAssign(name = name, values = this.values, expression = this.expression)

  override
  def generateSource()(implicit context: GlobalContext): CodeBlock = {
    val e = expression.generateSource()
    assert(e.length == 1)
    s"${qualifiedName(this.name)} = ${e.head}"
  }

  def getLowestEvaluationPoint: Path = expression.getLowestEvaluationPoint
}

final case class VerbatimDimensionAssign
(
  name: String,
  override var expression: Expression
) extends DimensionAssignContext {
  override def generateCubeDeclarationSource(): CodeBlock = {
    s"${this.name}:verbatim[${typeToHydraTypeDecl(this.expression.getDtype)}]"
  }

  override def copy(name: String): VerbatimDimensionAssign = VerbatimDimensionAssign(name = name, expression = this.expression)

  def getLowestEvaluationPoint: Path = expression.getLowestEvaluationPoint
}

final case class ScopedVerbatimDimensionAssign
(
  name: String,
  scope: Path,
  override var expression: Expression
) extends DimensionAssignContext {
  override def getLowestVisitPath: Path = Path.lowest(scope, expression.getLowestEvaluationPoint)

  override def generateCubeDeclarationSource(): CodeBlock = {
    s"${this.name}:verbatim[${typeToHydraTypeDecl(this.expression.getDtype)}]"
  }

  override def copy(name: String): ScopedVerbatimDimensionAssign = ScopedVerbatimDimensionAssign(name = name, scope = this.scope, expression = this.expression)

  def getLowestEvaluationPoint: Path = scope
}

/**
  * This context can be placed in the motif parse tree and so mimics an expression
  */
abstract class DimensionAssignContext
  extends DimensionAssign with ValueExpression with CubeDeclarationGenerator {

  override def phase(): ActionPhase = expression.phase()

  override def canReduceToConstant: lang.Boolean = expression.canReduceToConstant

  override def reduceToConstant: Constant = expression.reduceToConstant()

  override def getDtype: DataType = expression.getDtype

  override def bind(parsingSymbols: PathSymbols, stack: util.Stack[Evaluation]): Unit = {
    stack.push(this)
    expression.bind(parsingSymbols, stack)
    stack.pop
  }

  override def validate(parsingSymbols: PathSymbols, scope: Path, stack: util.Stack[Evaluation]): Unit = {
    stack.push(this)
    expression.validate(parsingSymbols, scope, stack)
    stack.pop
  }

  override def explain(): String = expression.explain()

  override def explain(level: Int): String = expression.explain(level)

  override def exportAsJson(): String = expression.exportAsJson

  override def getNodeType: NodeType = expression.getNodeType

  override def generateMotif(level: Int): String = expression.generateMotif(level)

  override def getChildren: util.List[Expression] = Collections.singletonList(expression)

  override def childCount(): Int = 1

  override def optimize(pathSymbols: PathSymbols): Evaluation = { this }

  override def getChild(index: Int): Expression = {
    if (index == 0)
      expression
    else
      throw new IndexOutOfBoundsException
  }

  override def setChild(index: Int, expression: Expression): Expression = {
    if (index == 0) {
      val old = this.expression
      this.expression = expression
      return old
    }
    throw new IndexOutOfBoundsException()
  }

  override def walkTree(checker: Parent.NodeWalker): Unit =  {
    if (checker == null)  return
    for (e: Expression <- expression.getChildren.asScala) {
      if (e != null)
        e.walkTree(checker)
    }
    checker.check(this)
  }
}

