/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.shrub.decl

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.collectors.FeltCollectorRef
import org.burstsys.felt.model.expressions.assign.FeltUpdateOp
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}


/**
 * A path reference to a route
 */
final case
class FeltShrubRef(refName: FeltPathExpr, refDecl: FeltShrubDecl) extends FeltCollectorRef {

  override val nodeName: String = "felt-shrub-ref"

  override val isMutable: Boolean = true

  sync(refDecl)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def canInferTypes: Boolean = refDecl.canInferTypes

  override
  def resolveTypes: this.type = {
    if (canInferTypes) {
      refDecl.resolveTypes
      feltType = refDecl.feltType
    }
    this
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  code generation
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def generateReferenceUpdate(op: FeltUpdateOp)(implicit cursor: FeltCodeCursor): FeltCode =
    throw FeltException(refName.location, s"$this can't update a shrub reference '${refName.fullPathAndKeyNoQuotes}'")

  override def generateReferenceAssign(implicit cursor: FeltCodeCursor): FeltCode =
    throw FeltException(refName.location, s"$this can't assign to  a shrub reference '${refName.fullPathAndKeyNoQuotes}'")

  override def generateReferenceRead(implicit cursor: FeltCodeCursor): FeltCode =
    throw FeltException(refName.location, s"$this can't read  a shrub reference '${refName.fullPathAndKeyNoQuotes}'")

  override def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode = ???

  override def generatePrepare(implicit cursor: FeltCodeCursor): FeltCode = ???

  override def generateRelease(implicit cursor: FeltCodeCursor): FeltCode = ???
}
