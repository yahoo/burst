/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.planning.queries

import org.burstsys.eql.planning.visits.Visits
import org.burstsys.motif.schema.model.MotifSchema

import scala.collection.mutable

class QueryVisits(val schema: MotifSchema) {
  private[planning] val queryMap = mutable.Map[Select, Visits]()

  /**
    * Allocate a new visits manager for this query
    * @param query the query
    * @return the visits manager
    */
  def createAndReturnNewQueryVisits(query: Select): Visits = {
    val visits = new Visits(schema)
    queryMap += query -> visits
    visits
  }

  def getQueryVisits(query:Select): Visits = {
    queryMap(query)
  }
}
