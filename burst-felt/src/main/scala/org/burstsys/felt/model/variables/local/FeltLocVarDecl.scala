/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.variables.local

import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.reference.names.FeltNamedNode
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.FeltLocation
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}
import org.burstsys.felt.model.types.FeltTypeDecl
import org.burstsys.felt.model.variables.FeltVarDecl
import org.burstsys.felt.model.variables.global.FeltGlobVarDecl
import org.burstsys.felt.model.variables.local.ref.FeltLocVarRef

/**
 * A type declaration for a ''local'' variable (these have appropriate forms of references installed).
 * The lifetime of a local variable is only within the 'code generated lexical scope' where the variable is introduced.
 * Generally this means within a [[org.burstsys.felt.model.expressions.FeltExprBlock]]. Local variable visibility is
 * scoped by the location of the declaration in the AST. Local variable generally end up be placed on stack or registers
 * and are considerably faster than [[FeltGlobVarDecl]]. They are
 * also optimized by static compilation as well as dynamically by JVM JIT processing
 */
trait FeltLocVarDecl extends FeltVarDecl with FeltNamedNode {

  final override val nodeName = "felt-loc-var-decl"

  override lazy val nsName: String = refName.fullPathAndKey

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////


  final override
  def reduceStatics: FeltLocVarDecl = new FeltLocVarDecl {
    sync(FeltLocVarDecl.this)
    final override val isMutable: Boolean = FeltLocVarDecl.this.isMutable
    final override val refName: FeltPathExpr = FeltLocVarDecl.this.refName.reduceStatics.resolveTypes
    final override val typeDeclaration: FeltTypeDecl = FeltLocVarDecl.this.typeDeclaration.reduceStatics.resolveTypes
    final override val initializer: FeltExpression = FeltLocVarDecl.this.initializer.reduceStatics.resolveTypes
    final override val location: FeltLocation = FeltLocVarDecl.this.location
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////`

  final override
  def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode =
    refName.referenceGetOrThrow[FeltLocVarRef].generateDeclaration

  final override
  def generatePrepare(implicit cursor: FeltCodeCursor): FeltCode =
    refName.referenceGetOrThrow[FeltLocVarRef].generatePrepare

  final override
  def generateRelease(implicit cursor: FeltCodeCursor): FeltCode =
    refName.referenceGetOrThrow[FeltLocVarRef].generateRelease

}
