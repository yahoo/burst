/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.tablet

import org.burstsys.felt.model.collectors.tablet.decl.FeltTabletDecl
import org.burstsys.felt.model.collectors.tablet.{FeltTabletPlan, FeltTabletProvider}
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I}

/**
 *
 */
trait ZapTabletPlan extends FeltTabletPlan

object ZapTabletPlan {
  def apply(cubeDecl: FeltTabletDecl): ZapTabletPlan = ZapTabletPlanContext(cubeDecl: FeltTabletDecl)
}

private final case
class ZapTabletPlanContext(decl: FeltTabletDecl) extends ZapTabletPlan {

  val binding: FeltTabletProvider = decl.global.binding.collectors.tablets

  override def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode = {
    val builderClass = binding.builderClassName
    s"""$I$builderClass().init($frameId, "$frameName", feltBinding)"""
  }

}
