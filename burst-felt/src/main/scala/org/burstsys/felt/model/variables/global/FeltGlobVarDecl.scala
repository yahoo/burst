/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.variables.global

import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.reference.names.FeltNamedNode
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.runtime.FeltRuntime
import org.burstsys.felt.model.tree.FeltLocation
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}
import org.burstsys.felt.model.types.FeltTypeDecl
import org.burstsys.felt.model.variables.FeltVarDecl
import org.burstsys.felt.model.variables.global.ref.FeltGlobVarRef
import org.burstsys.felt.model.variables.local.FeltLocVarDecl

/**
 * A type declaration for a ''global'' variable (these have appropriate forms of references installed)
 * Global variables are placed various points in an AST but generally have a lifetime that extends across the entire
 * scan. The ''visibility'' of these is scoped by their location in the AST. GLobal variable generally end up as fields
 * in the code generated [[FeltRuntime]] and thus are slower than
 * [[FeltLocVarDecl]]
 */
trait FeltGlobVarDecl extends FeltVarDecl with FeltNamedNode {

  final override val nodeName = "felt-glob-var-decl"

  override lazy val nsName: String = refName.fullPathNoQuotes

  lazy val externalName: String = s"gv_${global.newTreeId}_${refName.fullPathNoQuotes}"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltGlobVarDecl = new FeltGlobVarDecl {
    sync(FeltGlobVarDecl.this)
    final override val isMutable: Boolean = FeltGlobVarDecl.this.isMutable
    final override val refName: FeltPathExpr = FeltGlobVarDecl.this.refName.reduceStatics.resolveTypes
    final override val typeDeclaration: FeltTypeDecl = FeltGlobVarDecl.this.typeDeclaration.reduceStatics.resolveTypes
    final override val initializer: FeltExpression = FeltGlobVarDecl.this.initializer.reduceStatics.resolveTypes
    final override val location: FeltLocation = FeltGlobVarDecl.this.location
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode =
    refName.referenceGetOrThrow[FeltGlobVarRef].generateDeclaration

  final override
  def generatePrepare(implicit cursor: FeltCodeCursor): FeltCode =
    refName.referenceGetOrThrow[FeltGlobVarRef].generatePrepare

  final override
  def generateRelease(implicit cursor: FeltCodeCursor): FeltCode =
    refName.referenceGetOrThrow[FeltGlobVarRef].generateRelease

}
