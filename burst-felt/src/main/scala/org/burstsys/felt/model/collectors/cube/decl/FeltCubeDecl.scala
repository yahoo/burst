/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.frame.FeltFrameDecl
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.{FeltCubeAggDecl, FeltCubeAggsNode}
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.{FeltCubeDimDecl, FeltCubeDimsNode}
import org.burstsys.felt.model.collectors.cube.generate.calculus.FeltCubeCalculus
import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubePlan}
import org.burstsys.felt.model.collectors.decl.FeltCollectorDecl
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree._
import org.burstsys.felt.model.tree.code.FeltNoCode
import org.burstsys.felt.model.tree.source.S
import org.burstsys.felt.model.types.FeltType
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.strings.{VitalsGeneratingArray, VitalsString}

import scala.language.postfixOps
import scala.reflect.ClassTag

/**
 *
 */
trait FeltCubeDecl extends FeltCollectorDecl[FeltCubeRef, FeltCubeBuilder] {

  final override val nodeName = "felt-cube-decl"

  ///////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ///////////////////////////////////////////////////////////////////////

  private[this]
  var _parentCubeDecl: Option[FeltCubeDecl] = None

  ///////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////

  /**
   * the target that is where in a traversal the cube is
   * attached for joins/merges.
   *
   * @return
   */
  def refTarget: FeltPathExpr

  /**
   * the cubeId is the frameId - the use of this method is a bit sketchy so
   *
   * @return
   */
  def cubeId: Int = frame.frameId

  /**
   * the name of this cube is the frameName
   *
   * @return
   */
  def cubeName: String = frame.frameName

  final override def reference: FeltCubeRef = refName.referenceGetOrThrow[FeltCubeRef]

  /**
   *
   * @return
   */
  def isRootCube: Boolean = _parentCubeDecl.isEmpty

  def rootCube: FeltCubeDecl = {
    var current: FeltCubeDecl = this
    while (!current.isRootCube) current = parentCubeDecl.get
    current
  }

  /**
   * TODO
   *
   * @return
   */
  def aggregations: FeltCubeAggsNode

  /**
   * TODO
   *
   * @return
   */
  def dimensions: FeltCubeDimsNode

  /**
   * TODO
   *
   * @return
   */
  def limit: Option[FeltExpression] = None

  def rowLimit: Int = extractLimit.toInt

  ///////////////////////////////////////////////////////////////////////
  // CUBE TOPOLOGY
  ///////////////////////////////////////////////////////////////////////

  final def setRootCube(): FeltCubeDecl = {
    aggregations.cubeDecl = this
    dimensions.cubeDecl = this
    subCubes.foreach(_.parentCubeDecl = this)
    this
  }

  final
  def parentCubeDecl: Option[FeltCubeDecl] = _parentCubeDecl

  final
  def parentCubeDecl_=(c: FeltCubeDecl): Unit = {
    _parentCubeDecl = Some(c)
    subCubes.foreach(_.parentCubeDecl = this)
  }

  /**
   * TODO
   *
   * @return
   */
  def subCubes: Array[FeltCubeDecl]

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def allCubes: Array[FeltCubeDecl] = Array(this) ++ subCubes.flatMap(_.allCubes)

  final
  def allAggregations: Array[FeltCubeAggDecl] =
    aggregations.columns ++ subCubes.flatMap(_.allAggregations)

  final
  def allDimensions: Array[FeltCubeDimDecl] =
    dimensions.columns ++ subCubes.flatMap(_.allDimensions)

  final override
  def initializeFrame(frame: FeltFrameDecl): Unit = {
    super.initializeFrame(frame)
    allCubes.foreach {
      c =>
        c.frame = frame
        c.global = frame.global
    }
    subCubes.foreach(_.frame = frame)
    var aggOrdinal = 0
    allAggregations.foreach {
      a =>
        a.global = this.global
        a.columnOrdinal = aggOrdinal
        a.cubeDecl = rootCube
        a.frame = frame
        aggOrdinal += 1
    }
    var dimOrdinal = 0
    allDimensions.foreach {
      d =>
        d.global = this.global
        d.columnOrdinal = dimOrdinal
        d.cubeDecl = rootCube
        d.frame = frame
        dimOrdinal += 1
    }

    /**
     * specialized check for invalid cube names that shadow schema paths.
     */
    allAggregations ++ allDimensions foreach {
      col =>
        if (global.feltSchema.nodeExistsForPathName(col.columnName))
          throw FeltException(col, s"FELT_CUBE_COLUMN_NAME_CHECK: column '${col.columnName}' duplicates schema path (not allowed)")
    }
  }

  final lazy val calculus: FeltCubeCalculus = FeltCubeCalculus(this)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PLANNING
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def createPlan: FeltCubePlan = {
    calculus // make sure we check the calculus involved
    global.binding.collectors.cubes.collectorPlan(this).initialize
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltCubeDecl = new FeltCubeDecl {
    sync(FeltCubeDecl.this)
    final override val refTarget: FeltPathExpr = FeltCubeDecl.this.refTarget.reduceStatics.resolveTypes
    final override val location: FeltLocation = FeltCubeDecl.this.location
    final override val aggregations: FeltCubeAggsNode = {
      if (FeltCubeDecl.this.aggregations == null) null
      else FeltCubeDecl.this.aggregations.reduceStatics.resolveTypes
    }
    final override val dimensions: FeltCubeDimsNode = {
      if (FeltCubeDecl.this.dimensions == null) null
      else FeltCubeDecl.this.dimensions.reduceStatics.resolveTypes
    }
    final override val subCubes: Array[FeltCubeDecl] = {
      if (FeltCubeDecl.this.subCubes == null) null
      else FeltCubeDecl.this.subCubes.map(_.reduceStatics.resolveTypes)
    }
    final override val limit: Option[FeltExpression] = FeltCubeDecl.this.limit
    frame = FeltCubeDecl.this.frame // after we have subcubes
    if (FeltCubeDecl.this.isRootCube) setRootCube()
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] = {
    val aggs = if (aggregations != null) aggregations.treeApply(rule) else Array.empty
    val dims = if (dimensions != null) dimensions.treeApply(rule) else Array.empty
    val subs = if (subCubes != null) subCubes.treeApply(rule) else Array.empty
    rule(this) ++ refTarget.treeApply(rule) ++ aggs ++ dims ++ limit.treeApply(rule) ++ subs
  }

  override
  def children: Array[_ <: FeltNode] =
    (refTarget.asArray ++ Array(aggregations, dimensions) ++ subCubes ++ limit.asArray).filter(_ != null)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  /////////////////////////////////////////////////////// /////////////////////////////////////////////////////

  final override def canInferTypes: Boolean = true //(isRootCube && _cubeSchema.nonEmpty) || !isRootCube

  final override
  def resolveTypes: this.type = {
    if (!canInferTypes) return this
    if (aggregations != null) aggregations.resolveTypes
    if (dimensions != null) dimensions.resolveTypes
    limit.foreach(_.resolveTypes)
    if (subCubes != null) subCubes.foreach(_.resolveTypes)
    //    staticSplices.resolveTypes()
    feltType = FeltType.unit
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String = {
    s"\n${S}cube ${
      refTarget.fullPathNoQuotes
    } {" +
      s"$printLimit${
        if (aggregations == null) FeltNoCode else aggregations.normalizedSource(index + 1)
      }${
        if (dimensions == null) FeltNoCode else dimensions.normalizedSource(index + 1)
      }${
        printSubCubes(subCubes)
      }\n$S}"
  }

  def printSubCubes(cubes: Array[FeltCubeDecl])(implicit index: Int): String = {
    if (cubes.nonEmpty)
      s"\n${cubes.map(_.normalizedSource(index + 1).singleLineEnd).stringify.trimAtEnd}"
    else ""
  }

  private
  def printLimit()(implicit index: Int): String = {
    limit match {
      case None => ""
      case Some(l) => s"\n${S(index + 1)}limit = $extractLimit"
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // internals
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected
  def extractLimit: Long = {
    limit match {
      case None =>
        throw FeltException(location, s"NO ROW LIMIT SPECIFIED!")
      case Some(l) => l.reduceToFixAtom.getOrElse(throw VitalsException(s"row limit not a fixed integer")).value
    }
  }


}
