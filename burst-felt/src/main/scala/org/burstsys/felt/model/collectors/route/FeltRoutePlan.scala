/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route

import org.burstsys.felt.model.collectors.decl.FeltCollectorPlan
import org.burstsys.felt.model.collectors.route.decl.{FeltRouteDecl, FeltRouteRef}
import org.burstsys.felt.model.collectors.route.generate.FeltRouteBuilderGenerator
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}

trait FeltRoutePlan extends AnyRef
  with FeltCollectorPlan[FeltRouteRef, FeltRouteBuilder] {

  def decl: FeltRouteDecl

  def binding: FeltRouteProvider

  private val builderGenerator = FeltRouteBuilderGenerator(decl)

  override lazy val initialize: FeltRoutePlan = {
    builderGenerator.initialize()
    this
  }

  override
  def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode =
    builderGenerator.genBuilder

}
