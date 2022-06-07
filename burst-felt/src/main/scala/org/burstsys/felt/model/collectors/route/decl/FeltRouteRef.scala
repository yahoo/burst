/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.decl

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.collectors.FeltCollectorRef
import org.burstsys.felt.model.collectors.route.generate._
import org.burstsys.felt.model.expressions.assign.FeltUpdateOp
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.reference.{FeltReference, FeltStdRefResolver}
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}

object FeltRouteRef {

  final case class FeltRouteRefResolver(global: FeltGlobal) extends FeltStdRefResolver[FeltRouteDecl] {
    override val resolverName: String = "route"

    override protected def addResolution(refName: FeltPathExpr, d: FeltRouteDecl): FeltReference =
      FeltRouteRef(refName, d)

    override protected def addNomination(d: FeltRouteDecl): Option[FeltReference] =
      Some(FeltRouteRef(d.refName, d))

  }

}

/**
 * A path reference to a route
 */
final case
class FeltRouteRef(refName: FeltPathExpr, refDecl: FeltRouteDecl) extends FeltCollectorRef {

  override val nodeName: String = "route-ref"

  override val isMutable: Boolean = true

  sync(refDecl)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // code generation support
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def rootVariable: String = routeRootVariable(refDecl.routeName)

  def instanceVariable: String = routeInstanceVariable(refDecl.routeName)

  /*
    def pathOrdinalVariable: String = routeVisitPathOrdinalVariable(refDecl.routeName)
    def stepOrdinalVariable: String = routeVisitStepOrdinalVariable(refDecl.routeName)
    def stepKeyVariable: String = routeVisitStepKeyVariable(refDecl.routeName)
    def stepTagVariable: String = routeVisitStepTagVariable(refDecl.routeName)
    def stepTimeVariable: String = routeVisitStepTimeVariable(refDecl.routeName)
  */
  def stepIsFirstVariable: String = routeVisitStepIsFirstVariable(refDecl.routeName)

  def stepIsLastVariable: String = routeVisitStepIsLastVariable(refDecl.routeName)

  def pathIsFirstVariable: String = routeVisitPathIsFirstVariable(refDecl.routeName)

  def pathIsLastVariable: String = routeVisitPathIsLastVariable(refDecl.routeName)

  def builderVariable: String = routeBuilderVariable(refDecl.routeName)

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
    throw FeltException(refName.location, s"$this can't update a route reference '${refName.fullPathAndKeyNoQuotes}'")

  override def generateReferenceAssign(implicit cursor: FeltCodeCursor): FeltCode =
    throw FeltException(refName.location, s"$this can't assign to  a route reference '${refName.fullPathAndKeyNoQuotes}'")

  override def generateReferenceRead(implicit cursor: FeltCodeCursor): FeltCode =
    throw FeltException(refName.location, s"$this can't read  a route reference '${refName.fullPathAndKeyNoQuotes}'")

  override def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode = ???

  override def generatePrepare(implicit cursor: FeltCodeCursor): FeltCode = ???

  override def generateRelease(implicit cursor: FeltCodeCursor): FeltCode = ???
}
