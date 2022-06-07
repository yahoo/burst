/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.decl.visit

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.collectors.FeltCollectorRef
import org.burstsys.felt.model.collectors.route.generate.splice.FeltRouteStepsVisitableSplicer
import org.burstsys.felt.model.expressions.assign.FeltUpdateOp
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.reference.{FeltReference, FeltStdRefResolver}
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}
import org.burstsys.felt.model.visits.decl.{FeltVisitableRef, FeltVisitableSplicer}

object FeltRouteStepsRef {

  final case
  class FeltRouteStepsRefResolver(global: FeltGlobal) extends FeltStdRefResolver[FeltRouteStepsDecl] {

    override val resolverName: String = "route.steps"

    override protected def addResolution(refName: FeltPathExpr, d: FeltRouteStepsDecl): FeltReference =
      FeltRouteStepsRef(refName, d)

    override protected def addNomination(d: FeltRouteStepsDecl): Option[FeltReference] =
      Some(FeltRouteStepsRef(d.refName, d))

  }

}

/**
 * A path reference to a route
 */
final case
class FeltRouteStepsRef(refName: FeltPathExpr, refDecl: FeltRouteStepsDecl)
  extends FeltCollectorRef with FeltVisitableRef {

  override val nodeName: String = "felt-route-steps-ref"

  override val isMutable: Boolean = true

  sync(refDecl)

  override def visitableSplicer: FeltVisitableSplicer = FeltRouteStepsVisitableSplicer(refName)

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
    throw FeltException(refName.location, s"$this can't update a route steps reference '${refName.fullPathAndKeyNoQuotes}'")

  override def generateReferenceAssign(implicit cursor: FeltCodeCursor): FeltCode =
    throw FeltException(refName.location, s"$this can't assign to  a route steps reference '${refName.fullPathAndKeyNoQuotes}'")

  override def generateReferenceRead(implicit cursor: FeltCodeCursor): FeltCode =
    throw FeltException(refName.location, s"$this can't read  a route steps reference '${refName.fullPathAndKeyNoQuotes}'")

  override def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode = ???

  override def generatePrepare(implicit cursor: FeltCodeCursor): FeltCode = ???

  override def generateRelease(implicit cursor: FeltCodeCursor): FeltCode = ???
}
