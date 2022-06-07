/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.actions

import org.burstsys.eql.generators.hydra.actions.ControlExpressionSourceGenerator
import org.burstsys.eql.planning.EqlExpression
import org.burstsys.motif.motif.tree.expression.Expression
import org.burstsys.motif.paths.Path


final case class ControlExpression(
       expression: EqlExpression
  ) extends QueryAction with ControlExpressionSourceGenerator
{
  // override def getLowestVisitPath: Path = expression.getLowestEvaluationPoint
  override def getLowestVisitPath: Path = {
    expression.getLowestEvaluationPoint
  }

}
