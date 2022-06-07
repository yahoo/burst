/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.brio.decl

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.model.schema.types.BrioRelation
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode, emptyNodeArray}
import org.burstsys.felt.model.types.FeltType

import scala.reflect.ClassTag

/**
 * All Brio schema elements are created for the analysis as a set of declarations, one per schema relation in
 * the entire tree.
 */
trait FeltBrioStdDecl extends FeltBrioDecl {

  final override val nodeName = "felt-brio-decl"

  /**
   *
   * @return
   */
  final
  def relation: BrioRelation = brioNode.relation

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] = rule(this)

  final override
  def children: Array[_ <: FeltNode] = emptyNodeArray

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltBrioStdDecl = new FeltBrioStdDecl {
    sync(FeltBrioStdDecl.this)
    final override val location: FeltLocation = FeltBrioStdDecl.this.location
    final override val refName: FeltPathExpr = FeltBrioStdDecl.this.refName.resolveTypes.reduceStatics
    final override val nsName: String = FeltBrioStdDecl.this.nsName
    final override val brioNode: BrioNode = FeltBrioStdDecl.this.brioNode
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = true

  final override
  def resolveTypes: this.type = {
    super.resolveTypes
    feltType = FeltType.unit
    this
  }

}
