/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.planning

import org.burstsys.eql._
import org.burstsys.eql.generators.hydra.actions.BooleanExpressionGenerators
import org.burstsys.eql.parsing.ParsedQuery
import org.burstsys.eql.planning.visits.Visits
import org.burstsys.motif.motif.tree.expression.ParameterDefinition
import org.burstsys.motif.motif.tree.logical.BooleanExpression
import org.burstsys.motif.symbols.Definition.UsageContext

package object queries {
  type QueryName = String

  final case class PlannedSelect(select:Select, visits: Visits)

  trait Query extends Iterable[PlannedSelect] with UsageContext {
    /**
     * Get the visit work for a given query
     */
    def getVisits(query: Select): Visits

    /**
     * The schema name for the entire query
     * @return name
     */
    def getSchemaName: String

    /**
     * Get the sources used in this query
     */
    def getSources:  Array[PlanningSource]

    /**
     * The global limit for the whole query
     */
    def getGlobalLimit: Integer

    /**
     * Parameters for this analysis
     */
    def getParameters: Array[ParameterDefinition]

    /**
     * The global where clause added to all sources and selects
     */
    def getGlobalWhere: BooleanExpression
  }

  object Query {
    def apply(tree: ParsedQuery)(implicit global: GlobalContext): Query = new QueryImpl(tree)
  }


}
