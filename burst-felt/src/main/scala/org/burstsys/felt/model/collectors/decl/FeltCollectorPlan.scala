/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.decl

import org.burstsys.felt.model.collectors.FeltCollectorRef
import org.burstsys.felt.model.collectors.runtime.FeltCollectorBuilder
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}

/**
 *
 * @tparam R
 * @tparam B
 */
trait FeltCollectorPlan[R <: FeltCollectorRef, B <: FeltCollectorBuilder] extends AnyRef {

  ////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ////////////////////////////////////////////////////////////////////////

  /**
   * the final output of the planning machinations within
   */
  private[this]
  var _builder: B = _

  ////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////

  final def builder: B = _builder

  final def builder_=(b: B): Unit = _builder = b

  def binding: FeltCollectorProvider[_, _, _, _, _]

  ////////////////////////////////////////////////////////////////////////
  // SUBTYPE
  ////////////////////////////////////////////////////////////////////////

  def initialize: FeltCollectorPlan[R, B]

  def decl: FeltCollectorDecl[R, B]

  final def frameId: Int = decl.frame.frameId

  final def frameName: String = decl.frame.frameName

  /**
   *
   * @param cursor
   * @return
   */
  def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode

}
