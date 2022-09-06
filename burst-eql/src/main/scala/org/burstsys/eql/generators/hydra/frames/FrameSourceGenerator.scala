/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.frames

import org.burstsys.eql._
import org.burstsys.eql.generators.DeclarationScope.DeclarationScope
import org.burstsys.eql.generators.hydra.utils._
import org.burstsys.eql.generators.{AnalysisSourceGenerator, Collector, DeclarationScope, GeneratorVisits, NavigatingDeclaration}
import org.burstsys.eql.paths.DynamicVisitPath
import org.burstsys.eql.planning.lanes.{LaneControl, LaneName, RESULT}
import org.burstsys.eql.planning.visits.Visits
import org.burstsys.motif.motif.tree.data.context.PathAccessorContext
import org.burstsys.motif.motif.tree.data.{FunnelBinding, SchemaBinding, SegmentBinding}
import org.burstsys.motif.motif.tree.values.ValueExpression
import org.burstsys.motif.paths.Path

import scala.collection.mutable

abstract class FrameSourceGenerator(frameName: String)(implicit context: GlobalContext)
  extends NavigatingDeclaration(frameName, DeclarationScope.Frame) with AnalysisSourceGenerator with Collector with Controlled {

  protected val attachments: mutable.Map[DynamicVisitPath, String] = mutable.Map()

  protected val isAbortableAllowed: Boolean = false

  lazy val hydraVisits: GeneratorVisits = {
    // do final placement of control tests for all lanes
    val ln = visits.getAllLaneNames
    ln.foreach{l =>
      LaneControl.placeLaneControls(l, isAbortableAllowed)(visits)
    }
    // build the hydra version of visits
    val generatorVisits = GeneratorVisits(visits)
    generatorVisits
  }

  context.addDeclaration(frameName, this)

  implicit def visits: Visits

  def addAttachment(visit: DynamicVisitPath): String = {
    val attachmentName = context.addIfAbsentAttachment(visit)
    attachments(visit) = attachmentName
    attachmentName
  }

  def getAttachments: Map[DynamicVisitPath, String] = attachments.toMap

  def applyParameters(parameters: List[ValueExpression]): this.type

  override def addPlacedControl(controls: Array[PlacedControl]): this.type = {
    addPlacedControl(Array(RESULT), controls)
  }

  protected def filterPlacedControl(roots: Set[Path]): Boolean = true

  protected def cleanControls(controls: Array[PlacedControl]): Array[PlacedControl] = {
    controls.filter{ c =>
      val roots: Set[Path] = c.control.expression.traverseTree{(expression, paths: Array[Set[Path]]) =>
        expression.self match {
          case pac: PathAccessorContext => pac.getBinding match {
            case fbc: FunnelBinding =>
              Set(Path.getRoot(fbc.getPath))
            case sbc: SegmentBinding =>
              Set(Path.getRoot(sbc.getPath))
            case rb:  SchemaBinding =>
              Set(Path.getRoot(rb.getPath))
          }
          case _ =>
            //Path.lowest(expression.getLowestEvaluationPoint, Path.lowest(paths:_*))
            if (paths.isEmpty)
              Set(Path.getRoot(expression.getLowestEvaluationPoint))
            else
              paths.reduce((o, t) => o ++ t) + Path.getRoot(expression.getLowestEvaluationPoint)
        }
      }
      filterPlacedControl(roots)
    }
  }

  def addPlacedControl(lanes:  Array[LaneName], controls: Array[PlacedControl]): this.type = {
    val cleanedControls = cleanControls(controls)

    assert(lanes != null && lanes.nonEmpty)
    // we only need to make temporaries once
    cleanedControls.foreach(_.temporaries.foreach(_ placeInVisit RESULT))
    // but we place the control for every lane were are told to
    lanes.foreach(ln => cleanedControls.foreach(_.control placeInVisit ln))
    this
  }

  override def generateSource()(implicit context: GlobalContext): CodeBlock = {
    CodeBlock { implicit cb =>
      context.addProperty(FramePropertyName, frameName)
      s"frame ${context(FramePropertyName)} {".source()
      CodeBlock { implicit cb =>
        generateDeclarationsSource(DeclarationScope.Frame).foreach(_.source())
        hydraVisits.generateSource().source()
      }.indent.source()
      s"}".source()
      context.removeProperty(FramePropertyName)
    }
  }

  override def generateDeclarationsSource(scope: DeclarationScope)(implicit context: GlobalContext): CodeBlock =
    CodeBlock { implicit cb =>
      // make sure the collectors are together
      val collectorPartition = getDeclarations(scope).partition {
        case _: Collector => true
        case _ => false
      }
      collectorPartition._2.groupBy(_.name).values.foreach(_.head.generateDeclarationSource().source())
      collectorPartition._1.groupBy(_.name).values.foreach(_.head.generateDeclarationSource().source())
    }

  override def getDeclarations(scope: DeclarationScope)(implicit context: GlobalContext): Array[generators.Declaration] = {
    hydraVisits.getDeclarations(scope) ++
      Array(this)
  }
}
