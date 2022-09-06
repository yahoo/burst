/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.parsing

import org.burstsys.eql.GlobalContext
import org.burstsys.motif.motif.tree.eql.common.Source
import org.burstsys.motif.motif.tree.eql.queries.Query
import org.burstsys.motif.motif.tree.expression.ParameterDefinition
import org.burstsys.motif.schema.model.MotifSchema

import scala.jdk.CollectionConverters._

class ParsedQueryImpl(val query: Query)(implicit globalContext: GlobalContext) extends ParsedQuery {

  private val gLimit: Integer = if (query.getLimit != null && query.getLimit > 0)
    query.getLimit
  else
    Integer.valueOf(100)

  override def getSchema: MotifSchema = query.getSchema

  override def getSchemaName: String = query.getSchemaName

  override def getSources: List[Source] = query.getSources.asScala.toList

  override def getTree: Query = query

  override def getLimit: Integer = gLimit

  override def getParameters: Array[ParameterDefinition] = query.getParameters.asScala.toArray
}
