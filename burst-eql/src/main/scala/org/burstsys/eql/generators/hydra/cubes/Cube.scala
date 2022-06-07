/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.cubes

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.actions._
import org.burstsys.eql.generators.HydraVisitLanes.VisitType
import org.burstsys.eql.generators._
import org.burstsys.eql.generators.hydra.frames.FrameSourceGenerator
import org.burstsys.eql.generators.hydra.utils.{CodeBlock, _}
import org.burstsys.eql.paths.{DynamicVisitPath, FunnelVisitPath, VisitPath}
import org.burstsys.eql.planning.lanes._
import org.burstsys.eql.planning.queries.PlannedSelect
import org.burstsys.eql.planning.visits.Visits
import org.burstsys.motif.motif.tree.values.ValueExpression
import org.burstsys.motif.schema.model.MotifSchema

final class Cube(frameName: String, analysis: PlannedSelect)(implicit context: GlobalContext)
  extends FrameSourceGenerator(frameName) {

  override val isAbortableAllowed = true
  
  def collectorLimit: Integer = {
    if (noDimensions)
      1
    else if (analysis.select.limit != null && analysis.select.limit > 0)
      analysis.select.limit
    else
      context("globalLimit").toInt
  }

  private var noDimensions = true

  private val root: CubeScope = {
    val result: Option[CubeScope] = visits.postWalkVisits[CubeScope](RESULT){ (path, steps, children) =>

      val aggs = extractAggregates(steps)
      val dims = extractDimensions(steps)
      if (dims.nonEmpty)
        noDimensions = false

      path match {
        case dvp: DynamicVisitPath =>
          if (steps.nonEmpty)
            addAttachment(dvp)
        case _ =>
      }
      // plan the cube
      if (children.isEmpty)
        planWithNoChildren(path, steps, aggs, dims)
      else if (children.lengthCompare(1) == 0)
        planWithOneChild(path, children.head, steps, aggs, dims)
      else
        planWithMultipleChildren(path, children, steps, aggs, dims)

    }
    if (result.isEmpty)
      assert(result.nonEmpty)

    val r = result.get
    def updateLabels(s: CubeScope): CubeScope = {
      s.path match {
        case dvp: DynamicVisitPath =>
          val extensionPoint = addAttachment(dvp)
          s.cubeLabel = dvp.getAttachmentPath.toString + "." + extensionPoint
        case _ =>
      }
      s.children.foreach(updateLabels)
      s
    }
    updateLabels(r)
  }

  override def generateDeclarationSource(): CodeBlock = generateCubeDeclarationSource(root, withLimit = true)

  override def visits: Visits = analysis.visits

  /**
   * Cubes don't have applied parameters since they use parameters from the analysis.  Might change in the future,
   * though
   */
  override def applyParameters(parameters: List[ValueExpression]): this.type = {
    this
  }

  /**
   * Isolate the dimensions processed at this level
   */
  private def extractDimensions(steps: Option[LaneActions]): Array[Dimension] = {
    if (steps.isEmpty) {
      return Array()
    }

    steps.get.actions.flatMap{
      case d: DimensionSource => d.getDimensions
      case _ => Array[Dimension]()
    }.toArray
  }

  /**
   * Isolate the aggregates processed in this path level
   */
  private def extractAggregates(steps: Option[LaneActions]): Array[Aggregate] = {
    if (steps.isEmpty) {
      return Array()
    }

    {for (step <- steps.get.actions) yield step match {
        case a: Aggregate => Some(a)
        case _ => None
      }}.flatten.toArray
  }

  /**
   * This cube is a leaf.
   * We need to see if it does any work or not
   */
  private def planWithNoChildren(path: VisitPath,
                                 steps: Option[LaneActions],
                                 aggs: Array[Aggregate],
                                 dims: Array[Dimension]): Option[CubeScope] = {
    if (steps.isEmpty || steps.get.actions.isEmpty)
      // nothing done in this cube so discard it
      return None

    checkForDimensionWrite(path, aggs, dims)

    // we have work
    Some(CubeScope(path, aggs, dims))
  }

  /**
   *  This cube has exactly one child
   *  Look for opportunities for merging the child into the parent
   */
  private def planWithOneChild(path: VisitPath,
                               child: CubeScope,
                               steps: Option[LaneActions],
                               aggs: Array[Aggregate],
                               dims: Array[Dimension]
                              ): Option[CubeScope] = {
    val cubePath = path match {
      case fvp: FunnelVisitPath =>
        if (fvp.isPathPath) {
          // TODO:  try to come up with a better way of handling the fact that `paths` and `paths.steps` can't really nest
          child.path
        } else
          path
      case _ =>
        path
    }
    if (aggs.isEmpty && dims.isEmpty) {
      // this level does nothing so just move the child cube up
      Some(child.copy(path = cubePath))
    } else if (cubePath == child.path) {
      // so we have been moved down to the child path...just merge everything into the child
      Some(child.merge(cubePath, aggs, dims))
    } else if (!child.hasAnyDimensions && !dims.exists(_.phase() == ActionPhase.Post)) {
      // aggregation only in the lower visits can be combined into one cube as long as we don't have to delay
      // a dimension write
      Some(child.merge(cubePath, aggs, dims))
    } else if (aggs.isEmpty && !dims.exists(_.phase() == ActionPhase.Post)) {
      // this level only sets dimensions in the early phases with no aggregations.  the dimensins can be used by lower
      // joins
      Some(child.merge(cubePath, aggs, dims))
    } else {
      checkForDimensionWrite(path, aggs, dims)
      Some(CubeScope(cubePath, aggs, dims, List(child)))
    }
  }

  /**
   *  This cube has multiple child cubes.
   *  Mainly requires a join
   */
  private def planWithMultipleChildren(path: VisitPath,
                                       children: List[CubeScope],
                                       steps: Option[LaneActions],
                                       aggs: Array[Aggregate],
                                       dims: Array[Dimension]): Option[CubeScope] = {
    checkForDimensionWrite(path, aggs, dims)
    // there is more than one collection visit below us so make a join
    Some(CubeScope(path, aggs, dims, children))
  }

  private def checkForDimensionWrite(path: VisitPath,
                                     aggs: Array[Aggregate],
                                     dims: Array[Dimension]): Unit = {
    if (aggs.isEmpty && dims.nonEmpty)
      visits.addDimensionWrite(RESULT)(path)
  }

  private def generateCubeDeclarationSource(c: CubeScope, withLimit: Boolean = false): CodeBlock = CodeBlock { implicit cb =>
    s"cube ${c.cubeLabel} {".source
    CodeBlock { implicit cb =>
      if (withLimit)
        s"limit = $collectorLimit".source

      if (c.aggregates.nonEmpty) {
        s"aggregates {".source
        // aggregate declarations
        CodeBlock { implicit cb =>
          c.aggregates.foreach{a => a.generateCubeDeclarationSource().source}}.indent.source
        s"}".source
      }

      if (c.dimensions.nonEmpty) {
        s"dimensions {".source
        // dimension declarations
        CodeBlock { implicit cb =>
          c.dimensions.foreach{a => a.generateCubeDeclarationSource().source}}.indent.source
        s"}".source
      }

      // sub cubes
      c.children.foreach { cc =>
        generateCubeDeclarationSource(cc).source
      }
    }.indent.source()
    s"}".source()
  }

  override def selectHydraVisitLaneType(path: VisitPath)(implicit schema: MotifSchema): VisitType = {
    throw new IllegalAccessException(s"should not every call this")
  }

  case class CubeScope(path: VisitPath, aggregates: Array[Aggregate], dimensions: Array[Dimension], children: List[CubeScope] = List()) {
    var cubeLabel:String = path.toString
    def hasChildren: Boolean  = children.nonEmpty

    def hasAggregates: Boolean = aggregates.nonEmpty
    def hasDimensions: Boolean = dimensions.nonEmpty
    def hasAnyAggregates: Boolean = hasAggregates || children.exists(_.hasAggregates)
    def hasAnyDimensions: Boolean = hasDimensions || children.exists(_.hasDimensions)

    def merge(cubePath: VisitPath, aggs: Array[Aggregate], dims: Array[Dimension]): CubeScope =
      CubeScope(cubePath, aggregates ++ aggs, dimensions ++ dims, children)
  }

}
