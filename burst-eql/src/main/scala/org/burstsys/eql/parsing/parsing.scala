/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql

import org.burstsys.motif.motif.tree.eql.common.Source
import org.burstsys.motif.motif.tree.eql.funnels.Funnel
import org.burstsys.motif.motif.tree.eql.queries.Query
import org.burstsys.motif.motif.tree.eql.segments.Segment
import org.burstsys.motif.motif.tree.expression.ParameterDefinition
import org.burstsys.motif.schema.model.MotifSchema

package object parsing {

  object ParsedBlock {
    def apply(source: String)(implicit globalContext: GlobalContext): ParsedBlock =
      new ParsedBlockImpl(source)
  }
  trait ParsedBlock {
    /**
     * The statements
     * @return
     */
    def getStatements: Array[ParsedSourcedStatement]
  }

  trait ParsedStatement

  trait ParsedSourcedStatement {
    def getSchema: MotifSchema

    def getSchemaName: String

    def getSources: List[Source]
  }

  object ParsedQuery {
    def apply(query: Query)(implicit globalContext: GlobalContext): ParsedQuery =
      new ParsedQueryImpl(query)
  }

  trait ParsedQuery extends ParsedSourcedStatement {
    /**
     * The motif query tree
     * @return A motife query object rooting the parsed tree
     */
    def getTree: Query

    /**
     * The global limit for the entire query
     * @return limit integer possible a default
     */
    def getLimit: Integer

    /**
     * Get the paremeters for the query
     */
    def getParameters: Array[ParameterDefinition]
  }

  object ParsedFunnel {
    def apply(funnel: Funnel)(implicit globalContext: GlobalContext): ParsedFunnel =
      new ParsedFunnelImpl(funnel)
  }

  trait ParsedFunnel extends ParsedSourcedStatement {

    /**
     * The name of the funnel
     * @return
     */
    def getName: String

    /**
     * The motif query tree
     * @return A motife query object rooting the parsed tree
     */
    def getTree: Funnel

    /**
     * The limit for the funnel
     * @return limit integer possible a default
     */
    def getStepLimit: Integer

    def isRepeating: Boolean

    /**
     * Get the parameters for the query
     */
    def getParameters: Array[ParameterDefinition]

    /**
     * Get any special tags
     */
    def getTags: Array[String]
  }

  object ParsedSegment {
    def apply(segment: Segment)(implicit globalContext: GlobalContext): ParsedSegment =
      new ParsedSegmentImpl(segment)
  }
  trait ParsedSegment extends ParsedSourcedStatement {

    /**
     * The name
     * @return
     */
    def getName: String

    /**
     * The motif tree
     */
    def getTree: Segment

    /**
     * Get the parameters
     */
    def getParameters: Array[ParameterDefinition]
  }
}
