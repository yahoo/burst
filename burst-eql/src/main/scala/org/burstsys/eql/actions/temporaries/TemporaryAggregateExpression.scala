/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.actions.temporaries

import org.burstsys.eql.generators.hydra.temporaries.{TemporaryAggregateInitializerSourceGenerator, TemporaryAggregateSourceGenerator}
import org.burstsys.eql.planning.lanes.{BasicLaneName, INIT_PREFIX, LaneControl, LaneName}
import org.burstsys.eql.planning.visits.Visits
import org.burstsys.motif.motif.tree.expression.Evaluation
import org.burstsys.motif.motif.tree.values.AggregationValueExpression
import org.burstsys.motif.paths.Path
import org.burstsys.motif.symbols.PathSymbols

case class TemporaryAggregateExpression(override val name: String, override val value: AggregationValueExpression)
  extends TemporaryExpressionContext(name, value) {

  override def getLowestVisitPath: Path =
    Path.lowest(value.getExpr.getLowestEvaluationPoint, value.getScope.getLowestEvaluationPoint)

  override def placeInVisit(lane: LaneName)(implicit visits: Visits): Unit = {
    val initializationPath = value.getScope.getLowestEvaluationPoint
    val controls = LaneControl.extractAndControls(value.getWhere)

    val temporaryLane: LaneName = BasicLaneName(name + '-' + value.generateMotif(0).take(70))
    val temporaryInitLane: LaneName = BasicLaneName(INIT_PREFIX + " " + temporaryLane.name)
    // place this aggregate's calculation
    visits.addGenerator(temporaryInitLane)(initializationPath,
      new TemporaryAggregateExpression(name, value) with TemporaryAggregateInitializerSourceGenerator  )
    // place this aggregate's temporary intialization
    visits.addGenerator(temporaryLane)(getLowestVisitPath,
      new TemporaryAggregateExpression(name, value) with TemporaryAggregateSourceGenerator  )
    // place the controls at this temporary calculation
    controls.foreach(_ placeInVisit temporaryLane)
  }

  override def optimize(pathSymbols: PathSymbols): Evaluation = {
    if (this.canReduceToConstant)
      this.reduceToConstant
    else
      this.copy(value = value.optimize(pathSymbols).asInstanceOf[AggregationValueExpression])
  }
}
