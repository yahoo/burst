/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.tablet.decl

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.collectors.FeltCollectorRef
import org.burstsys.felt.model.collectors.tablet.generate.splice.FeltTabletMembersVisitableSplicer
import org.burstsys.felt.model.expressions.assign.FeltUpdateOp
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.reference.{FeltReference, FeltStdRefResolver}
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}
import org.burstsys.felt.model.visits.decl.{FeltVisitableRef, FeltVisitableSplicer}

object FeltTabletMembersRef {

  final case
  class FeltTabletMembersRefResolver(global: FeltGlobal)
    extends FeltStdRefResolver[FeltTabletMembersDecl] {
    override val resolverName: String = "tablet.member"

    override protected def addResolution(refName: FeltPathExpr, d: FeltTabletMembersDecl): FeltReference =
      FeltTabletMembersRef(refName, d)

    override protected def addNomination(d: FeltTabletMembersDecl): Option[FeltReference] =
      Some(FeltTabletMembersRef(d.refName, d))

  }

}

/**
 * A path reference to a tablet
 */
final case
class FeltTabletMembersRef(refName: FeltPathExpr, refDecl: FeltTabletMembersDecl)
  extends FeltCollectorRef with FeltVisitableRef {

  override val nodeName: String = "felt-tablet-members-ref"

  override val isMutable: Boolean = true

  sync(refDecl)

  override def visitableSplicer: FeltVisitableSplicer = FeltTabletMembersVisitableSplicer(refName)

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
    throw FeltException(refName.location, s"$this can't update a tablet members reference '${refName.fullPathAndKeyNoQuotes}'")

  override def generateReferenceAssign(implicit cursor: FeltCodeCursor): FeltCode =
    throw FeltException(refName.location, s"$this can't assign to  a tablet members reference '${refName.fullPathAndKeyNoQuotes}'")

  override def generateReferenceRead(implicit cursor: FeltCodeCursor): FeltCode =
    throw FeltException(refName.location, s"$this can't read  a tablet members reference '${refName.fullPathAndKeyNoQuotes}'")

  override def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode = ???

  override def generatePrepare(implicit cursor: FeltCodeCursor): FeltCode = ???

  override def generateRelease(implicit cursor: FeltCodeCursor): FeltCode = ???
}
