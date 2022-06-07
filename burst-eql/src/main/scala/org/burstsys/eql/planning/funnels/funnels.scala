/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.planning

import org.burstsys.eql._
import org.burstsys.eql.actions.{ControlExpression, QueryAction}
import org.burstsys.eql.generators.hydra.routes.StepTag
import org.burstsys.eql.parsing.ParsedFunnel
import org.burstsys.eql.planning.lanes.LaneControl.extractAndControls
import org.burstsys.eql.planning.visits.Visits
import org.burstsys.motif.motif.tree.eql.funnels.{FunnelMatchDefinition, StepDefinition}
import org.burstsys.motif.paths.Path

package object funnels {

  trait Funnel extends ParameterizedSource {
    /**
     * Name of the funnel
     */
    def getName: String

    /**
     * Get the visit work for a given query
     *
     * @return
     */
    def getVisits: Visits

    /**
     * The global step limit for the funnel
     *
     * @return integer limit value
     */
    def getStepLimit: Integer

    /**
     * The global path limit for the funnel
     *
     * @return integer limit value
     */
    def getPathLimit: Integer

    def getWithin: Long

    def getSteps: Seq[Step]

    def getMatchDefinition: FunnelMatchDefinition

    def getTags: Array[String]

    def formPath(pathString: String): Path
  }

  object Funnel {
    def apply(tree: ParsedFunnel)(implicit global: GlobalContext): Funnel = new FunnelImpl(tree)
  }

  abstract class Step (treeStep: StepDefinition) extends QueryAction {
    val id: StepTag = treeStep.getId

    var controls: Array[ControlExpression] = extractAndControls(treeStep.getWhen)

    override def getLowestVisitPath: Path = {
      controls.map(_.getLowestVisitPath).reduce((l, r) => Path.lowest(l, r))
    }

  }
}
