/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.planning.segments

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.actions.{ControlExpression, QueryAction}
import org.burstsys.eql.generators.ActionPhase
import org.burstsys.eql.generators.ActionPhase.ActionPhase
import org.burstsys.eql.generators.hydra.tablets.TabletId
import org.burstsys.eql.generators.hydra.utils._
import org.burstsys.eql.planning.ParameterMap
import org.burstsys.eql.planning.lanes.LaneControl.extractAndControls
import org.burstsys.motif.motif.tree.eql.segments.SegmentDefinition
import org.burstsys.motif.paths.Path

class Definition(val segmentName: String, val treeDefinition: SegmentDefinition)
  extends QueryAction
{
  val id: TabletId = treeDefinition.getName
  override def phase(): ActionPhase = ActionPhase.Post

  var controls: Array[ControlExpression] = extractAndControls(treeDefinition.getWhere)

  override def getLowestVisitPath: Path = {
    controls.map(_.getLowestVisitPath).reduce((l, r) => Path.lowest(l, r))
  }

  def this(definition: Definition) {
    this(definition.segmentName, definition.treeDefinition)
  }

  override def transformParameterReferences(parameters: ParameterMap): Unit = {
    controls = controls.map(ce => ControlExpression(transformToParameterReferences(ce.expression, parameters)))
  }

  override def generateSource()(implicit context: GlobalContext): CodeBlock = CodeBlock { implicit cb =>
    s"tabletMemberAdd($segmentName, $id)".source
  }
}


