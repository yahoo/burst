/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.planning

import org.burstsys.eql._
import org.burstsys.eql.parsing.ParsedSegment
import org.burstsys.eql.planning.visits.Visits

package object segments {

  trait Segment extends ParameterizedSource {
    /**
     * Name of the funnel
     */
    def getName: String

    /**
     * Get the visit work
     *
     * @return
     */
    def getVisits: Visits

    def getDefinitions: Seq[Definition]
  }

  object Segment {
    def apply(tree: ParsedSegment)(implicit global: GlobalContext): Segment = new SegmentImpl(tree)
  }

}
