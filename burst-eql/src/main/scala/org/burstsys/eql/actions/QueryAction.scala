/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.actions

import org.burstsys.eql.generators.ActionPhase.ActionPhase
import org.burstsys.eql.planning.{EqlExpression, EqlParameterAccessor, ParameterMap, ParameterReference, VisitPlanner}
import org.burstsys.eql.planning.lanes._
import org.burstsys.eql.planning.visits.Visits
import org.burstsys.eql.generators.{ActionPhase, ActionSourceGenerator}
import org.burstsys.motif.motif.tree.data.ParameterAccessor
import org.burstsys.motif.paths.Path

trait QueryAction extends AnyRef with ActionSourceGenerator with VisitPlanner {
  def getLowestVisitPath: Path
  override def phase(): ActionPhase = ActionPhase.Pre

  /**
    * For most query actions just place the generator at the evaluation point
    * @param visits the implicit visit manager
    */
  override def placeInVisit(lane: LaneName)(implicit visits: Visits): Unit = {
    val path = getLowestVisitPath
    if (path ==  null)
      throw new RuntimeException(s"Action has no lowest evaluation point for generation:  $this")
    visits.addGenerator(lane)(path, this)
  }

  protected def transformToParameterReferences(e: EqlExpression, parameters: Map[String, ParameterReference]): EqlExpression = {
    e.transformTree { n =>
      n.self match {
        case pa: ParameterAccessor =>
          new EqlParameterAccessor(parameters(pa.getName), pa)
        case d => n
      }
    }
  }

  def transformParameterReferences(parameters: ParameterMap): Unit = {}
}
