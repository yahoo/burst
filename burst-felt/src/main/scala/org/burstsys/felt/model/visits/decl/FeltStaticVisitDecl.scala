/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.visits.decl

import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.frame.FeltFrameDecl
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.FeltLocation
import org.burstsys.felt.model.tree.code.FeltNoCode
import org.burstsys.felt.model.tree.source.{S, SL}
import org.burstsys.felt.model.variables.global.FeltGlobVarDecl

/**
 * A static ''visit'' within a [[FeltFrameDecl]] within an [[FeltAnalysisDecl]]
 */
trait FeltStaticVisitDecl extends FeltVisitDecl {

  final override val nodeName = "felt-static-visit-decl"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveTypes: this.type = {
    super.resolveTypes
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltStaticVisitDecl = new FeltStaticVisitDecl {
    sync(FeltStaticVisitDecl.this)
    final override val traverseTarget: FeltPathExpr =
      FeltStaticVisitDecl.this.traverseTarget.reduceStatics.resolveTypes
    final override val variables: Array[FeltGlobVarDecl] =
      FeltStaticVisitDecl.this.variables.map(_.reduceStatics.resolveTypes)
    final override val actions: Array[FeltActionDecl] =
      FeltStaticVisitDecl.this.actions.map(_.reduceStatics.resolveTypes)
    final override val location: FeltLocation = FeltStaticVisitDecl.this.location
    final override val ordinalExpression: Option[FeltExpression] = {
      if (FeltStaticVisitDecl.this.ordinalExpression.isEmpty) None
      else Some(FeltStaticVisitDecl.this.ordinalExpression.get.reduceStatics.resolveTypes)
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String = {
    val ordinalCode = if (ordinalExpression.isEmpty) FeltNoCode else s"($ordinal)"
    s"""|$S${traverseTarget.normalizedSource} $ordinalCode ⇒ {${SL(variables)}${SL(actions)}
        |$S}""".stripMargin
  }

}
