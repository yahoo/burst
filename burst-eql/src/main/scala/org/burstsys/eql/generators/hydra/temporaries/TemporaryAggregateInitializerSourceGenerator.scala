/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.temporaries

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.actions.temporaries.TemporaryAggregateExpression
import org.burstsys.eql.generators.ActionPhase.ActionPhase
import org.burstsys.eql.generators.hydra.utils.CodeBlock
import org.burstsys.eql.generators.{ActionPhase, ActionSourceGenerator}
import org.burstsys.motif.motif.tree.values.AggregationOperatorType

trait TemporaryAggregateInitializerSourceGenerator extends ActionSourceGenerator {
  self: TemporaryAggregateExpression =>

  override def phase(): ActionPhase = ActionPhase.Pre

  override
  def generateSource()(implicit context: GlobalContext): CodeBlock = {
      value.getOp match {
        case AggregationOperatorType.COUNT =>
          s"$name = 0"
        case AggregationOperatorType.UNIQUE =>
          s"$name = 0"
        case AggregationOperatorType.SUM =>
          s"$name = null"
        case AggregationOperatorType.MIN =>
          s"$name = null"
        case AggregationOperatorType.MAX =>
          s"$name = null"
        case _ =>
          throw new UnsupportedOperationException
      }
  }
}
