/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.parsing

import org.burstsys.eql._
import org.burstsys.motif.Motif
import org.burstsys.motif.motif.tree.eql.funnels.Funnel
import org.burstsys.motif.motif.tree.eql.queries.Query
import org.burstsys.motif.motif.tree.eql.segments.Segment

import scala.collection.JavaConverters._

class ParsedBlockImpl(source: String)(implicit globalContext: GlobalContext) extends ParsedBlock {
  private val motif: Motif = Motif.build()
  private val statements: Array[ParsedSourcedStatement] =
    motif.parseMotifStatements(source).getStatements.asScala.map {
      case q: Query => ParsedQuery(q)
      case f: Funnel => ParsedFunnel(f)
      case s: Segment => ParsedSegment(s)
    }.toArray

  override def getStatements: Array[ParsedSourcedStatement] = statements

}
