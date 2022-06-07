/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql

import org.burstsys.eql.actions.TemporaryExpression
import org.burstsys.eql.generators.hydra.actions.EqlParameterAccessorGenerator
import org.burstsys.eql.planning.lanes._
import org.burstsys.eql.planning.visits.Visits
import org.burstsys.motif.common.{DataType, NodeType}
import org.burstsys.motif.motif.tree.constant.Constant
import org.burstsys.motif.motif.tree.data.ParameterAccessor
import org.burstsys.motif.motif.tree.expression._
import org.burstsys.motif.motif.tree.logical.BooleanExpression
import org.burstsys.motif.motif.tree.values.ValueExpression
import org.burstsys.motif.paths.Path
import org.burstsys.motif.symbols.PathSymbols

import java.util
import scala.language.implicitConversions
import scala.reflect.ClassTag

package object planning {
  type VisitName = String
  trait VisitPlanner {
    def placeInVisit(lane: LaneName)(implicit visits: Visits): Unit
  }

  trait EqlExpression extends ValueExpression {
    def self: Expression = this
    def transformTree(walker: EqlExpression => EqlExpression): EqlExpression = {
      if (walker == null)
        return this

      for (idx <- 0 until this.childCount()) {
        val c = getChild(idx)
        if (c != null) {
          c match {
            case ee: EqlExpression =>
              val tc = ee.transformTree(walker)
              setChild(idx, tc)
            case e =>
              val c = expressionToEqlExpression(e).transformTree(walker)
              setChild(idx, c)
          }
        }
      }
      val t = walker(this)
      t
    }

    def traverseTree[U: ClassTag](walker: (EqlExpression, Array[U]) => U): U = {
      val childResults = (0 until this.childCount()).map(getChild).filter(_ != null).map{
        case ee: EqlExpression =>
          ee.traverseTree(walker)
        case e =>
          expressionToEqlExpression(e).traverseTree(walker)
      }.toArray
      walker(this, childResults)
    }
  }

  implicit def expressionToEqlExpression(expr: Expression): EqlExpression = {
    expr match {
      case ee: EqlExpression =>
        ee
      case be: BooleanExpression =>
        new EqlExpressionWrapper(be) with BooleanExpression
      case e =>
        new EqlExpressionWrapper(e)
    }
  }

  class EqlExpressionWrapper(override val self: Expression) extends Expression with EqlExpression {
    // Proxy the underlying motif expression
    override def canReduceToConstant: java.lang.Boolean = self.canReduceToConstant
    override def reduceToConstant(): Constant = self.reduceToConstant()
    override def getDtype: DataType = self.getDtype
    override def getLowestEvaluationPoint: Path = self.getLowestEvaluationPoint
    override def optimize(pathSymbols: PathSymbols): Evaluation = self.optimize(pathSymbols)
    override def bind(parsingSymbols: PathSymbols, stack: util.Stack[Evaluation]): Unit = {
      stack.push(this)
      self.bind(parsingSymbols, stack)
      stack.pop()
    }
    override def validate(parsingSymbols: PathSymbols, scope: Path, stack: util.Stack[Evaluation]): Unit = {
      stack.push(this)
      self.validate(parsingSymbols, scope, stack)
      stack.pop()
    }
    override def explain(): String = self.explain()
    override def explain(level: Int): String = self.explain(level)
    override def exportAsJson(): String = self.exportAsJson()
    override def getNodeType: NodeType = self.getNodeType
    override def generateMotif(level: Int): String = self.generateMotif(level)
    override def getChildren: util.List[Expression] = self.getChildren
    override def childCount(): Int = self.childCount
    override def getChild(index: Int): Expression = self.getChild(index)
    override def setChild(index: Int, value: Expression): Expression = self.setChild(index, value)
    override def walkTree(checker: Parent.NodeWalker): Unit = self.walkTree(checker)

    override def toString: VisitName = s"W[${self.toString}]"
  }

  case class PlanningSource(name: String, declaredName: String, parameters: List[ValueExpression])

  trait ParameterizedSource {
    def getParameters: Array[ParameterReference]

    def getSchemaName: String

    def getSourceNames: List[String]
  }

  case class ParameterReference(definition: ParameterDefinition, var value: ValueExpression = null)

  type ParameterMap = Map[String, ParameterReference]

  class EqlParameterAccessor(val reference:  ParameterReference, accessor: ParameterAccessor)
    extends EqlExpressionWrapper(accessor) with EqlParameterAccessorGenerator

  def escapeIdentifierName(name: String): String = s"'$name'"
}
