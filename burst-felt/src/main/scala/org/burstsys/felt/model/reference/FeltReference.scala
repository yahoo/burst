/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.reference

import org.burstsys.brio.types.BrioTypes.BrioRelationCount
import org.burstsys.felt.model.expressions.assign.FeltUpdateOp
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}

import scala.reflect.ClassTag

/**
 * References are used to instrument a [[FeltPathExpr]] to allow it to ''refer'' to a
 * [[FeltRefDecl]] such as a BRIO relation, variable/parameter, or other FELT compatible
 * referenceable artifacts (cubes, tables etc). Expressions are scanned for paths that a
 * [[FeltRefResolver]] grabs as referring to something it manages.
 *
 * [[FeltReference]] instances have the ability to code generate either ''read'' or ''write'' accesses to
 * those artifacts.
 */
trait FeltReference extends FeltNode {

  /**
   * the path that refers to the artifact
   *
   * @return
   */
  def refName: FeltPathExpr

  /**
   * the declaration of the referenced artifact
   *
   * @return
   */
  def refDecl: FeltRefDecl

  /**
   * does this reference support writes?
   *
   * @return
   */
  def isMutable: Boolean

  final override
  def location: FeltLocation = refName.location

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] = rule(this)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltReference = this

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: BrioRelationCount): String = refName.normalizedSource

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode

  def generatePrepare(implicit cursor: FeltCodeCursor): FeltCode

  def generateRelease(implicit cursor: FeltCodeCursor): FeltCode

  def generateReferenceUpdate(op: FeltUpdateOp)(implicit cursor: FeltCodeCursor): FeltCode

  def generateReferenceAssign(implicit cursor: FeltCodeCursor): FeltCode

  def generateReferenceRead(implicit cursor: FeltCodeCursor): FeltCode

}
