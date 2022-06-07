/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.paths

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.generators.ActionSourceGenerator
import org.burstsys.eql.generators.hydra.utils
import org.burstsys.motif.paths.segments.{SegmentMembersFieldPath, SegmentPathBase}

class SegmentPathGenerator(segmentPath: SegmentPathBase) extends ActionSourceGenerator {
    override def generateSource()(implicit context: GlobalContext): utils.CodeBlock =
      segmentPath match {
        case path: SegmentMembersFieldPath =>
          path.getField match {
            case SegmentMembersFieldPath.Field.ID => s"tabletMemberValue(${path.getSegment.getName})"
          }
      }
}
