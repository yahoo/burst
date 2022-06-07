/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.variables.parameter

import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.reference.names.FeltNamedNode
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.FeltLocation
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}
import org.burstsys.felt.model.tree.source.S
import org.burstsys.felt.model.types.FeltTypeDecl
import org.burstsys.felt.model.variables.FeltVarDecl
import org.burstsys.felt.model.variables.parameter.ref.FeltParamRef

/**
 * A type declaration for a parameter. Parameters are used for a function-domain ''signature'' in
 * various places. They are immutable but can be ''initialized'' which essentially means they
 * have a ''default'' that must be a statically reduceable expression (often a simple literal)
 *
 * ==parameter initialization ==
 * Parameters are  ''initialized'' to a immutable value in the generated ''prepare'' i.e. it is done once
 * per scan of a brio blob (object tree).
 * {{{
 * 1) the FabricCall object is checked for a parameter of the same name as the this parameter.
 * 2) If the call provided same named parameter exists then the this parameter is initialized to that value
 * 3) if the call parameter does not exist, then the initializer provided in the felt tree is executed.
 * }}}
 */
trait FeltParamDecl extends FeltVarDecl with FeltNamedNode {

  final override val nodeName = "felt-param-decl"

  final def isMutable: Boolean = false

  override lazy val nsName: String = refName.fullPathAndKey

  /**
   * a unique (within an analysis) identifier for a parameter within the code generated artifacts
   */
  lazy val externalName: String = s"pv_${global.newTreeId}_${refName.fullPathNoQuotes}"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltParamDecl = new FeltParamDecl {
    sync(FeltParamDecl.this)
    final override val refName: FeltPathExpr = FeltParamDecl.this.refName.reduceStatics.resolveTypes
    final override val typeDeclaration: FeltTypeDecl = FeltParamDecl.this.typeDeclaration.reduceStatics.resolveTypes
    final override val initializer: FeltExpression = FeltParamDecl.this.initializer.reduceStatics.resolveTypes
    final override val location: FeltLocation = FeltParamDecl.this.location
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode =
    refName.referenceGetOrThrow[FeltParamRef].generateDeclaration

  final override
  def generatePrepare(implicit cursor: FeltCodeCursor): FeltCode =
    refName.referenceGetOrThrow[FeltParamRef].generatePrepare

  final override
  def generateRelease(implicit cursor: FeltCodeCursor): FeltCode =
    refName.referenceGetOrThrow[FeltParamRef].generateRelease

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"$S${refName.fullPathAndKey}:${typeDeclaration.normalizedSource} = ${initializer.normalizedSource}"

}
