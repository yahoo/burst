/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.collectors.FeltCollectorRef
import org.burstsys.felt.model.collectors.cube.generate.splice.visitor.FeltCubeVisitorSplicer
import org.burstsys.felt.model.expressions.assign.FeltUpdateOp
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.reference.{FeltReference, FeltStdRefResolver}
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.{FeltCodeCursor, _}
import org.burstsys.felt.model.visits.decl.{FeltVisitorRef, FeltVisitorSplicer}

import scala.language.postfixOps


object FeltCubeRef {

  final case class FeltCubeRefResolver(global: FeltGlobal) extends FeltStdRefResolver[FeltCubeDecl] {
    override val resolverName: String = "cube"

    override protected
    def addResolution(refName: FeltPathExpr, d: FeltCubeDecl): FeltReference =
      FeltCubeRef(refName, d.rootCube)

    override protected
    def addNomination(c: FeltCubeDecl): Option[FeltReference] = {
      // make sure get the root *not* the subcubes...
      if (!c.isRootCube) return None
      Some(FeltCubeRef(c.refName, c))
    }

  }

}

/**
 * installed for a cube reference within an expression. The reference can be either to the cube itself
 * (for cube level ops) or to a field (aggregation or dimension) within the cube.
 */
final case
class FeltCubeRef(refName: FeltPathExpr, refDecl: FeltCubeDecl) extends FeltCollectorRef
  with FeltVisitorRef {

  sync(refDecl)

  override val nodeName: String = "felt-cube-ref"

  override val isMutable: Boolean = true

  def cubeName: String = refDecl.cubeName

  override def visitorSplicer: FeltVisitorSplicer = FeltCubeVisitorSplicer(refName)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def canInferTypes: Boolean = false

  override
  def resolveTypes: this.type = this

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  CODE GENERATION
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def generateReferenceUpdate(op: FeltUpdateOp)(implicit cursor: FeltCodeCursor): FeltCode = ???

  override
  def generateReferenceAssign(implicit cursor: FeltCodeCursor): FeltCode = {
    throw FeltException(refName.location, message = s"can't generate a reference assign to a pure cube reference")
  }

  override
  def generateReferenceRead(implicit cursor: FeltCodeCursor): FeltCode =
    throw FeltException(refName.location, s"does not makes sense to read from a cube '${refName.fullPathAndKeyNoQuotes}'")

  override def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode = ???

  override def generatePrepare(implicit cursor: FeltCodeCursor): FeltCode = ???

  override def generateRelease(implicit cursor: FeltCodeCursor): FeltCode = ???
}
