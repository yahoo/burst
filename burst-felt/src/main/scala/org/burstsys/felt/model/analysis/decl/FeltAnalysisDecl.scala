/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.analysis.decl

import org.burstsys.felt.model.FeltDeclaration
import org.burstsys.felt.model.frame.FeltFrameDecl
import org.burstsys.felt.model.collectors.cube.decl.FeltCubeDecl
import org.burstsys.felt.model.collectors.decl.FeltCollectorDecl
import org.burstsys.felt.model.collectors.route.decl.FeltRouteDecl
import org.burstsys.felt.model.collectors.shrub.decl.FeltShrubDecl
import org.burstsys.felt.model.collectors.tablet.decl.FeltTabletDecl
import org.burstsys.felt.model.reference.names.FeltNamedNode
import org.burstsys.felt.model.schema.decl.FeltSchemaDecl
import org.burstsys.felt.model.tree.source.{S, S2}
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}
import org.burstsys.felt.model.types.FeltType
import org.burstsys.felt.model.variables.global.FeltGlobVarDecl
import org.burstsys.felt.model.variables.parameter.FeltParamDecl
import org.burstsys.felt.model.visits.decl.FeltVisitDecl
import org.burstsys.vitals.strings.{VitalsGeneratingArray, VitalsString}
import org.burstsys.vitals.uid.newBurstUid
import org.joda.time.DateTimeZone

import scala.language.postfixOps
import scala.reflect.ClassTag

/**
 * ==Felt Analysis Decl Tree==
 * This is the root of a FELT based behavioral analysis tree structure.
 * It is code generated into a [[org.burstsys.felt.model.sweep.FeltSweep]]
 * which is the compiled, cached, and executed as a scan against a [[org.burstsys.brio.blob.BrioBlob]].
 * An analysis is comprised of one or more [[FeltFrameDecl]] each of which represents a discrete parallel
 * sub analysis sharing the same blob scan operation.
 */
trait FeltAnalysisDecl extends FeltDeclaration with FeltNamedNode {

  final override val nodeName: String = "felt-analysis-decl"

  final override def nsName: String = analysisName

  /**
   * the prefix used for language translation units
   *
   * @return
   */
  def sourcePrefix: String

  /**
   * the class name for the generated [[org.burstsys.felt.model.sweep.FeltSweep]]
   */
  final
  val sweepClassName: String = newBurstUid

  /**
   * name of the analysis
   *
   * @return
   */
  def analysisName: String

  /**
   * the name of the schema as captured in the [[FeltAnalysisDecl]] tree schema settings
   *
   * @return
   */
  def schemaDecl: FeltSchemaDecl

  /**
   * the analysis global setting for the default time zone as captured in the [[FeltAnalysisDecl]] time zone settings
   *
   * @return
   */
  def timezone: DateTimeZone

  /**
   * the set of parallel frames within the analysis
   *
   * @return
   */
  def frames: Array[FeltFrameDecl]

  final
  def assertFrame(): Unit = frames.foreach(_.assertFrame())

  final
  def assertCollectors(): Unit = frames.foreach {
    frame =>
      frame.collectorDecl.refName.sync(this)
      frame.collectorDecl.initializeFrame(frame)
  }

  private def collectorDecls[T <: FeltCollectorDecl[_, _] : ClassTag]: Array[T] = frames.map(_.collectorDecl match {
    case c: T => c.asInstanceOf[T]
    case _ => null.asInstanceOf[T]
  }).filter(_ != null)

  final def cubes: Array[FeltCubeDecl] = collectorDecls

  final def routes: Array[FeltRouteDecl] = collectorDecls

  final def tablets: Array[FeltTabletDecl] = collectorDecls

  final def shrubs: Array[FeltShrubDecl] = collectorDecls

  final def visits: Array[FeltVisitDecl] = frames.flatMap(_.visits)

  /**
   * the set of global variables within the analysis. This includes top level (analysis),
   * query level, and visit level
   *
   * @return
   */
  final
  def globalVariables: Array[FeltGlobVarDecl] =
    variables ++ frames.flatMap(q => q.variables ++ q.visits.flatMap(_.variables))

  /**
   * the parameters for the analysis as a function call
   *
   * @return
   */
  def parameters: Array[FeltParamDecl]

  /**
   * the root level variables within the analysis
   *
   * @return
   */
  def variables: Array[FeltGlobVarDecl]

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ frames.treeApply(rule) ++ parameters.treeApply(rule) ++ variables.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = frames ++ parameters ++ variables ++ schemaDecl.asArray

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = {
    schemaDecl.canInferTypes &&
      parameters.canInferTypes &&
      variables.canInferTypes &&
      frames.canInferTypes
  }

  final override
  def resolveTypes: this.type = {
    schemaDecl.resolveTypes
    parameters.foreach(_.resolveTypes)
    variables.foreach(_.resolveTypes)
    frames.foreach(_.resolveTypes)
    feltType = FeltType.unit
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltAnalysisDecl = new FeltAnalysisDecl {
    sync(FeltAnalysisDecl.this)
    final override val location: FeltLocation = FeltAnalysisDecl.this.location
    final override val sourcePrefix: String = FeltAnalysisDecl.this.sourcePrefix
    final override val analysisName: String = FeltAnalysisDecl.this.analysisName
    final override val schemaDecl: FeltSchemaDecl = FeltAnalysisDecl.this.schemaDecl
    final override val timezone: DateTimeZone = FeltAnalysisDecl.this.timezone
    final override val frames: Array[FeltFrameDecl] = FeltAnalysisDecl.this.frames.map(_.reduceStatics.resolveTypes)
    final override val parameters: Array[FeltParamDecl] = FeltAnalysisDecl.this.parameters.map(_.reduceStatics.resolveTypes)
    final override val variables: Array[FeltGlobVarDecl] = FeltAnalysisDecl.this.variables.map(_.reduceStatics.resolveTypes)
    global.analysis = this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String = {
    val schema = schemaDecl.normalizedSource
    val parameterList = parameters.map(_.normalizedSource(index + 1)).mkString(",\n")
    val signatureSource = if (parameters.isEmpty) "()" else s"(\n$S$parameterList\n$S)"
    val variableSource = if (variables.isEmpty) "" else s"\n${variables.map(_.normalizedSource(index + 1) singleLineEnd).stringify.trimAtEnd}"
    val frameSource: String = if (frames.isEmpty) "" else s"\n${frames.map(_.normalizedSource(index + 1) singleLineEnd).stringify.trimAtEnd}"
    val s =
      s"""|$S$sourcePrefix $analysisName$signatureSource {
          |${S2}$schema$variableSource$frameSource
          |$S}""".stripMargin
    s
  }

}
