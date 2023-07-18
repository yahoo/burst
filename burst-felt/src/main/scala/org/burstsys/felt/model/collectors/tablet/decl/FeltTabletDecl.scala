/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.tablet.decl

import org.burstsys.felt.model.collectors.decl.FeltCollectorDecl
import org.burstsys.felt.model.collectors.tablet.{FeltTabletBuilder, FeltTabletPlan}
import org.burstsys.felt.model.tree.source.S
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}
import org.burstsys.felt.model.types.FeltPrimTypeDecl

import scala.reflect.ClassTag

/**
 *
 */
trait FeltTabletDecl extends FeltCollectorDecl[FeltTabletRef, FeltTabletBuilder] {

  final override val nodeName = "felt-tablet-decl"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * the tabletId is the frameId
   *
   * @return
   */
  def tabletId: Int = frame.frameId

  /**
   * the name of this tablet is the frameName
   *
   * @return
   */
  def tabletName: String = frame.frameName

  /**
   * This is a static determinable semantic
   *
   * @return
   */
  def typeDeclaration: FeltPrimTypeDecl

  final override def reference: FeltTabletRef = refName.referenceGetOrThrow[FeltTabletRef]

  def membersDecl: FeltTabletMembersDecl

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PLANNING
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def createPlan: FeltTabletPlan =
    global.binding.collectors.tablets.collectorPlan(this).initialize

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // rules
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ membersDecl.treeApply(rule) ++ refName.treeApply(rule) ++ typeDeclaration.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] =
    refName.asArray ++ membersDecl.asArray ++ typeDeclaration.asArray

  final
  def sync(tablet: FeltTabletDecl): Unit = {
    super.sync(tablet)
    this.membersDecl.sync(tablet.membersDecl)
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = true

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override def resolveTypes: this.type = {
    feltType = typeDeclaration.resolveTypes.feltType
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltTabletDecl = new FeltTabletDecl {
    final override val typeDeclaration: FeltPrimTypeDecl = FeltTabletDecl.this.typeDeclaration
    final override val location: FeltLocation = FeltTabletDecl.this.location
    final override val membersDecl: FeltTabletMembersDecl = FeltTabletDecl.this.membersDecl
    this.sync(FeltTabletDecl.this)
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String = {
    s"${S}tablet [ ${typeDeclaration.normalizedSource} ]"
  }

}
