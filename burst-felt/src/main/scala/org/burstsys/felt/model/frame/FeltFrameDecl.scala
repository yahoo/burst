/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.frame

import org.burstsys.felt.model.FeltDeclaration
import org.burstsys.felt.model.collectors.decl.FeltCollectorDecl
import org.burstsys.felt.model.reference.names.FeltNamedNode
import org.burstsys.felt.model.tree.source.{S, SL}
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}
import org.burstsys.felt.model.types.FeltType
import org.burstsys.felt.model.variables.global.FeltGlobVarDecl
import org.burstsys.felt.model.visits.decl.FeltVisitDecl
import org.joda.time.DateTimeZone

import scala.reflect.ClassTag

/**
 * A frame is container for a single collector and a set of visits. An analysis is one or more
 * of frames. Each frame is executed in parallel during the analysis.
 */
trait FeltFrameDecl extends FeltDeclaration with FeltNamedNode with FeltFrameElement {

  final override val nodeName = "felt-frame-decl"

  final override def nsName: String = frameName

  /**
   *
   * @return
   */
  def timezone: DateTimeZone

  /**
   *
   * @return
   */
  def collectorDecl: FeltCollectorDecl[_, _]

  /**
   * The set of variables within a query
   *
   * @return
   */
  def variables: Array[FeltGlobVarDecl]

  /**
   * The set of visits within a query
   *
   * @return
   */
  def visits: Array[FeltVisitDecl]

  /**
   * make sure all decls in frame know which frame they are part of
   */
  final
  def assertFrame(): Unit = treeUpdate(_.frame = this)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ collectorDecl.treeApply(rule) ++ variables.treeApply(rule) ++ visits.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = collectorDecl.asArray ++ variables ++ visits

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltFrameDecl = new FeltFrameDecl {
    sync(FeltFrameDecl.this)
    final override val location: FeltLocation = FeltFrameDecl.this.location
    final override val frameId: Int = FeltFrameDecl.this.frameId
    final override val frameName: String = FeltFrameDecl.this.frameName
    final override val timezone: DateTimeZone = FeltFrameDecl.this.timezone
    final override val collectorDecl: FeltCollectorDecl[_, _] = FeltFrameDecl.this.collectorDecl.reduceStatics.resolveTypes
    final override val variables: Array[FeltGlobVarDecl] = FeltFrameDecl.this.variables.map(_.reduceStatics.resolveTypes)
    final override val visits: Array[FeltVisitDecl] = FeltFrameDecl.this.visits.map(_.reduceStatics.resolveTypes)
    assertFrame()
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = {
    // add in hydra specific collectors
    collectorDecl.canInferTypes && variables.canInferTypes && visits.canInferTypes
  }

  final override
  def resolveTypes: this.type = {
    super.resolveTypes
    // add in hydra specific collectors
    collectorDecl.resolveTypes
    variables.resolveTypes()
    visits.resolveTypes()
    feltType = FeltType.unit
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"${S}frame ${frameName} {${SL(variables)}${collectorDecl.normalizedSource(index + 1)}${SL(visits)}\n$S}"

}
