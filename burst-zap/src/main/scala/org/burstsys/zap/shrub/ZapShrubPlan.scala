/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.shrub

import org.burstsys.felt.model.collectors.shrub.{FeltShrubPlan, FeltShrubProvider}
import org.burstsys.felt.model.collectors.shrub.decl.FeltShrubDecl
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I}

/**
 *
 */
trait ZapShrubPlan extends FeltShrubPlan

object ZapShrubPlan {
  def apply(cubeDecl: FeltShrubDecl): ZapShrubPlan = ZapShrubPlanContext(cubeDecl: FeltShrubDecl)
}

private final case
class ZapShrubPlanContext(decl: FeltShrubDecl) extends ZapShrubPlan {

  val binding: FeltShrubProvider = decl.global.binding.collectors.shrubs

  override def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode =  ???

}
