/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.sweep.splice

import org.burstsys.brio.types.BrioPath.BrioPathName
import org.burstsys.felt.model.expressions.{FeltExprBlock, FeltExpression}
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}
import org.burstsys.felt.model.tree.{FeltGlobal, FeltLocation}
import org.burstsys.felt.model.visits.decl.{FeltActionDecl, FeltVisitDecl}

/**
 * A Felt Code Splice based on a provided [[FeltExpression]]
 * Generally this is used to splice FELT executable expression subtrees such as
 * the [[FeltExprBlock]] found in an [[FeltActionDecl]] within a [[FeltVisitDecl]]
 */
trait FeltExprSplice extends FeltSplice {

  /**
   * the provided felt expression
   *
   * @return
   */
  def expression: FeltExpression

}

object FeltExprSplice {
  def apply(global: FeltGlobal, location: FeltLocation, spliceTag: String, pathName: BrioPathName,
            placement: FeltPlacement, expression: FeltExpression, ordinal: Int = 0): FeltSplice =
    FeltExprSpliceContext(global: FeltGlobal, location: FeltLocation, spliceTag: String, pathName: BrioPathName,
      placement: FeltPlacement, expression: FeltExpression, ordinal: Int)
}

private final case
class FeltExprSpliceContext(global: FeltGlobal, location: FeltLocation, spliceName: String, pathName: BrioPathName,
                            placement: FeltPlacement, expression: FeltExpression, ordinal: Int)
  extends FeltSpliceContext(global = global, location = location, spliceName = spliceName, pathName = pathName,
    placement = placement) with FeltExprSplice {

  feltType = expression.resolveTypes.feltType

  override protected
  def generateCode(implicit cursor: FeltCodeCursor): FeltCode = {
    expression.resolveTypes.generateExpression
  }

}
