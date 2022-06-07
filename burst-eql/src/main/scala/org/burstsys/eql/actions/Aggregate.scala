/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.actions

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.generators.ActionPhase.ActionPhase
import org.burstsys.eql.generators.hydra.actions.AggregateSourceGenerator
import org.burstsys.eql.generators.hydra.utils._
import org.burstsys.eql.generators.{ActionPhase, CubeDeclarationGenerator, typeToHydraTypeDecl}
import org.burstsys.motif.common.DataType
import org.burstsys.motif.motif.tree.expression.Expression
import org.burstsys.motif.motif.tree.values.{AggregationOperatorType, AggregationValueExpression}
import org.burstsys.motif.paths.Path

import scala.collection.mutable.ArrayBuffer

object Aggregate {
  def apply(name: String, aggNode: AggregationValueExpression): Aggregate = {
    assert(aggNode.getWhere == null)
    Aggregate(name, aggNode.getOp, aggNode.getExpr, aggNode.getScope, aggNode.getSize)
  }
}

final case class Aggregate
(
  name: String,
  function: AggregationOperatorType,
  var value: Expression,
  scope: Expression,
  size: Integer
) extends QueryAction with AggregateSourceGenerator with CubeDeclarationGenerator {
  override def getLowestVisitPath: Path = Path.lowest(value.getLowestEvaluationPoint, scope.getLowestEvaluationPoint)

  override def phase(): ActionPhase = ActionPhase.Post

  override def generateCubeDeclarationSource(): CodeBlock = {
    this.function match {
      case AggregationOperatorType.COUNT =>
        s"${this.name}:sum[long]"
      case AggregationOperatorType.UNIQUE =>
        s"${this.name}:unique[long]"
      case AggregationOperatorType.TOP =>
        s"${this.name}:top[long](${this.size})"
      case _ =>
        if (DataType.findCommonDtype(this.value.getDtype, DataType.LONG) == DataType.LONG)
          s"${this.name}:${this.function.toString.toLowerCase}[long]"
        else if (DataType.findCommonDtype(this.value.getDtype, DataType.DOUBLE) == DataType.DOUBLE)
          s"${this.name}:${this.function.toString.toLowerCase}[double]"
        else
          s"${this.name}:${this.function.toString.toLowerCase}[${typeToHydraTypeDecl(this.value.getDtype)}]"
    }
  }

  def placeTemporaries()(implicit global: GlobalContext): Array[TemporaryExpression] = {
    val temporaries: ArrayBuffer[TemporaryExpression] = new ArrayBuffer()
    value = Temporary.transformToTemporaries(value, temporaries)
    temporaries.toArray
  }
}
