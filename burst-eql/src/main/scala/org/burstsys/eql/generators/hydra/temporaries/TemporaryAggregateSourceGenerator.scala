/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.temporaries

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.actions.temporaries.TemporaryAggregateExpression
import org.burstsys.eql.generators.hydra.utils.CodeBlock
import org.burstsys.eql.generators.{ActionSourceGenerator, _}
import org.burstsys.motif.motif.tree.values.AggregationOperatorType

trait TemporaryAggregateSourceGenerator extends ActionSourceGenerator {
  self: TemporaryAggregateExpression =>

  override
  def generateSource()(implicit context: GlobalContext): CodeBlock = {
    value.getOp match {
      case AggregationOperatorType.COUNT =>
        s"$name = $name + 1"
      case AggregationOperatorType.UNIQUE =>
        s"if ($name != 1) $name = 1"
      case AggregationOperatorType.SUM =>
        val items = value.getExpr.generateSource()
        s"if ($name == null) { $name = ${items.last} } else { $name = $name + (${items.last}) }"
      case AggregationOperatorType.MIN =>
        val items = value.getExpr.generateSource()
        s"if ($name == null || $name > ${items.last}) { $name = ${items.last} }"
      case AggregationOperatorType.MAX =>
        val items = value.getExpr.generateSource()
        s"if ($name == null || $name < ${items.last}) { $name = ${items.last} }"
      case _ =>
        throw new UnsupportedOperationException
    }
  }
}
