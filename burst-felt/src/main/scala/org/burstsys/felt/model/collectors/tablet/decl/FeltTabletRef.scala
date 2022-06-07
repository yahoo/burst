/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.tablet.decl

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.collectors.FeltCollectorRef
import org.burstsys.felt.model.collectors.tablet.generate._
import org.burstsys.felt.model.collectors.tablet.generate.splice.FeltTabletMembersVisitableSplicer
import org.burstsys.felt.model.expressions.assign.FeltUpdateOp
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.reference.{FeltReference, FeltStdRefResolver}
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.visits.decl.{FeltVisitableRef, FeltVisitableSplicer}

object FeltTabletRef {

  final case class FeltTabletRefResolver(global: FeltGlobal) extends FeltStdRefResolver[FeltTabletDecl] {

    override val resolverName: String = "tablet"

    override protected def addResolution(refName: FeltPathExpr, d: FeltTabletDecl): FeltReference =
      FeltTabletRef(refName, d)

    override protected def addNomination(d: FeltTabletDecl): Option[FeltReference] =
      Some(FeltTabletRef(d.refName, d))

  }

}


/**
 * A path reference to a tablet
 */
final case
class FeltTabletRef(refName: FeltPathExpr, refDecl: FeltTabletDecl)
  extends FeltCollectorRef with FeltVisitableRef {

  override val nodeName: String = "tablet-ref"

  override val isMutable: Boolean = true

  sync(refDecl)

  override def visitableSplicer: FeltVisitableSplicer = FeltTabletMembersVisitableSplicer(refName)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // code generation support
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def rootVariable: String = {
    tabletRootVariable(frame.frameName)
  }

  def instanceVariable: String = {
    tabletInstanceVariable(frame.frameName)
  }

  def builderVariable: String = {
    tabletBuilderVariable(frame.frameName)
  }

  def memberValueVariable: String = {
    tabletMemberValueVariable(frame.frameName)
  }

  def memberIsFirstVariable: String = {
    tabletMemberIsFirstVariable(frame.frameName)
  }

  def memberIsLastVariable: String = {
    tabletMemberIsLastVariable(frame.frameName)
  }

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

  override def generateReferenceUpdate(op: FeltUpdateOp)(implicit cursor: FeltCodeCursor): FeltCode = {
    val brioName = refDecl.feltType.valueTypeAsBrio
    s"""|
        |${C(s"$nodeName-reference-update")}
        |${I}if( !${cursor.callScope.scopeNull}  ) {
        |$I2$sweepRuntimeSym.${instanceVariable}.tabletAdd$brioName( ${cursor.callScope.scopeVal} );
        |$I}""".stripMargin
  }

  override def generateReferenceAssign(implicit cursor: FeltCodeCursor): FeltCode =
    throw FeltException(refName.location, s"$this can't assign to  a tablet reference (yet) '${refName.fullPathAndKeyNoQuotes}'")

  override def generateReferenceRead(implicit cursor: FeltCodeCursor): FeltCode =
    throw FeltException(refName.location, s"$this can't read  a tablet reference (yet) '${refName.fullPathAndKeyNoQuotes}'")

  override def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode = ???

  override def generatePrepare(implicit cursor: FeltCodeCursor): FeltCode = ???

  override def generateRelease(implicit cursor: FeltCodeCursor): FeltCode = ???
}
