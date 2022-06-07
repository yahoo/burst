/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.decl

import org.burstsys.brio.blob.BrioBlob
import org.burstsys.felt.model.frame.FeltFrameDecl
import org.burstsys.felt.model.collectors.FeltCollectorRef
import org.burstsys.felt.model.collectors.runtime.{FeltCollector, FeltCollectorBuilder}
import org.burstsys.felt.model.reference.FeltRefDecl
import org.burstsys.felt.model.reference.path.{FeltPathExpr, FeltSimplePath}
import org.burstsys.felt.model.sweep.FeltSweep
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}

/**
 * ==Felt Collectors==
 * a [[FeltCollectorDecl]] is the metadata for a extensible set of [[FeltCollector]] instances (one per frame).
 * A collector is a mutable
 * data structure that is used to ''collect'' data during a [[FeltSweep]] traversal. During a scan, the immutable
 * information from a [[BrioBlob]] at any given point in a scan.
 * is read and processed and used to make decisions about how to update
 * a mutable collector which can then be used to update other collectors and ultimately return information
 * back as a result to the analysis.
 */
trait FeltCollectorDecl[R <: FeltCollectorRef, B <: FeltCollectorBuilder]
  extends AnyRef with FeltRefDecl {

  ///////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ///////////////////////////////////////////////////////////////////////

  private[this]
  lazy val _plan: FeltCollectorPlan[R, B] = createPlan

  ///////////////////////////////////////////////////////////////////////
  // ACCESSORS
  ///////////////////////////////////////////////////////////////////////

  final override
  lazy val refName: FeltPathExpr = {
    val name = FeltSimplePath(frame.frameName)
    name.sync(this)
    name
  }

  /**
   * convenient accessor to underlying reference
   *
   * @return
   */
  def reference: R

  ///////////////////////////////////////////////////////////////////////

  /**
   * generate the plan
   *
   * @return
   */
  def createPlan: FeltCollectorPlan[R, B]

  /**
   * every collector has a ''plan'' that once created can be used
   * to generate code for the sweep.
   *
   * @return
   */
  final def plan: FeltCollectorPlan[R, B] = _plan

  def initializeFrame(frame: FeltFrameDecl): Unit = {
    this.frame = frame
    this.global = frame.global
  }

  ///////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ///////////////////////////////////////////////////////////////////////

  override def reduceStatics: FeltCollectorDecl[_, _] = this

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode = _plan.generateDeclaration

}
