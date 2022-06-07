/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.planning.queries

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.actions.Temporary
import org.burstsys.eql.parsing.ParsedQuery
import org.burstsys.eql.planning.PlanningSource
import org.burstsys.eql.planning.lanes._
import org.burstsys.eql.planning.visits.Visits
import org.burstsys.motif.motif.tree.expression.ParameterDefinition
import org.burstsys.motif.motif.tree.logical.BooleanExpression

import scala.collection.JavaConverters._
import scala.language.postfixOps

class QueryImpl(tree: ParsedQuery)(implicit global: GlobalContext) extends Query {

  override val getSources:  Array[PlanningSource] = tree.getTree.getSources.asScala.map(
    s => PlanningSource(s.getName, s.getDeclaredName, s.getParms.asScala.toList)
  ).toArray

  // Extract the analysis level structures
  private[planning] val parameters = tree.getParameters
  private[planning] val queryVisits = new QueryVisits(tree.getSchema)
  private[planning] val selects: Array[Select] = tree.getTree.getSelects.asScala.map(Select(_)).toArray

  // Iterate through every query in the analysis
  selects.foreach { s =>
    implicit val visits: Visits = queryVisits.createAndReturnNewQueryVisits(s)

    // calculate temporaries
    val queryTemps =
      s.controls.flatMap(Temporary placeTemporaries _.expression) ++
      s.dimensions.flatMap(Temporary placeTemporaries _) ++
      s.aggregates.flatMap(_.placeTemporaries())

    // Set initial basic actions in visits
    queryTemps.foreach(_ placeInVisit RESULT)
    s.dimensions.foreach(_ placeInVisit RESULT)
    s.aggregates.foreach(_ placeInVisit RESULT)
    s.controls.foreach(_ placeInVisit RESULT)
    s.specials.foreach(_ placeInVisit RESULT)
  }

  override def getGlobalWhere: BooleanExpression = tree.getTree.getWhere

  override def getVisits(query: Select): Visits = queryVisits.getQueryVisits(query)

  override def iterator: Iterator[PlannedSelect] = selects.map(s => PlannedSelect(s, getVisits(s))).iterator

  override val getSchemaName: String = tree.getSchema.getSchemaName

  override val getGlobalLimit: Integer = tree.getLimit

  override val getParameters: Array[ParameterDefinition] = tree.getParameters
}
