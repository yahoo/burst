/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.tablets

import org.burstsys.eql.actions.{ControlExpression, TemporaryExpression}
import org.burstsys.eql.GlobalContext
import org.burstsys.eql.generators.HydraVisitLanes.{PHASED, SITU, VisitType}
import org.burstsys.eql.generators.hydra.frames.{Controlled, FrameSourceGenerator, PlacedControl}
import org.burstsys.eql.generators.hydra.utils.CodeBlock
import org.burstsys.eql.generators.hydra.utils.CodeBlock.stringToCodeBlock
import org.burstsys.eql.paths.VisitPath
import org.burstsys.eql.planning.segments.Segment
import org.burstsys.eql.planning.visits.Visits
import org.burstsys.motif.motif.tree.values.ValueExpression
import org.burstsys.motif.paths.Path
import org.burstsys.motif.paths.funnels.FunnelPathBase
import org.burstsys.motif.paths.segments.SegmentPathBase
import org.burstsys.motif.schema.model.MotifSchema

final class Tablet(segment: Segment)(implicit context: GlobalContext) extends FrameSourceGenerator(segment.getName) with Controlled {

  override def visits: Visits = segment.getVisits

  override def applyParameters(parameters: List[ValueExpression]): this.type = {
    segment.getParameters.zip(parameters).foreach(x => x._1.value = x._2)
    this
  }

  override def filterPlacedControl(roots: Set[Path]): Boolean = roots.map{
    case fpb: FunnelPathBase =>
      // place it only if we use it as a source
      this.segment.getSourceNames.contains(fpb.getFunnel.getName)
    case spb: SegmentPathBase =>
      // place it only if we use it as a source
      this.segment.getSourceNames.contains(spb.getSegment.getName)
    case _ =>
      true
  }.reduce(_&&_)

  override def addPlacedControl(controls: Array[PlacedControl]): this.type = {
    // don't include controls that refer to visits outside of static scope
    val cleanedControls = controls.filter { c =>
      c.control.getLowestVisitPath match {
        case _: SegmentPathBase =>
          false
        case _: FunnelPathBase =>
          false
        case _ =>
          true
      }
    }
    addPlacedControl(visits.getAllLaneNames, controls)
  }

  override def generateDeclarationSource(): CodeBlock = CodeBlock { implicit cb =>
    s"tablet[long]".source
  }

  override def selectHydraVisitLaneType(path: VisitPath)(implicit schema: MotifSchema): VisitType = {
    SITU
  }
}
