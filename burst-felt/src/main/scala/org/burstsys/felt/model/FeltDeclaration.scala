/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model

import org.burstsys.felt.model.runtime.FeltRuntime
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, FeltNoCode}
import org.burstsys.felt.model.types._

/**
 * Marker trait for those AST nodes that are declarations
 *
 */
trait FeltDeclaration extends FeltNode {

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def reduceStatics: FeltDeclaration = this

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def resolveTypes: this.type = {
    feltType = FeltType.unit
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // code generation
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Generate executable code to be used in a Felt sweep.
   * <p/> '''NOTE:''' generally speaking, its important to push all code generation (implementation) into the
   * reference level i.e. that is where the exact nature of the  declaration details are known
   *
   * @param cursor
   * @return
   */
  def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode = FeltNoCode

  /**
   * some declarations need to have a set up happen each time a
   * [[FeltRuntime]] is prepared for a traversal
   * <p/> '''NOTE:''' generally speaking, its important to push all code generation (implementation) into the
   * reference level i.e. that is where the exact nature of the  declaration details are known
   *
   * @param cursor
   * @return
   */
  def generatePrepare(implicit cursor: FeltCodeCursor): FeltCode = FeltNoCode

  /**
   * some declarations need to have a break down happen after each
   * [[FeltRuntime]] is traversed
   * <p/> '''NOTE:''' generally speaking, its important to push all code generation (implementation) into the
   * reference level i.e. that is where the exact nature of the  declaration details are known
   *
   * @param cursor
   * @return
   */
  def generateRelease(implicit cursor: FeltCodeCursor): FeltCode = FeltNoCode

}
