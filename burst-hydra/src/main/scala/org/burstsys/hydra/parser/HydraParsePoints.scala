/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.collectors.cube.decl.FeltCubeDecl
import org.burstsys.felt.model.collectors.route.decl.FeltRouteDecl
import org.burstsys.felt.model.collectors.tablet.decl.FeltTabletDecl
import org.burstsys.felt.model.visits.decl.{FeltActionDecl, FeltDynamicVisitDecl, FeltStaticVisitDecl}
import org.burstsys.felt.model.expressions.flow.conditional.FeltCondExpr
import org.burstsys.felt.model.expressions.flow.pattern.FeltMatchExpr
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.method.FeltMethodDecl
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.variables.FeltVarDecl
import org.burstsys.felt.model.expressions.{FeltExprBlock, FeltExpression}

/**
 * clause specific end points
 */
trait HydraParsePoints extends Any {

  self: HydraParser =>

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // declarations
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def parseAnalysis(analysisSource: String, defaultSchema: BrioSchema): FeltAnalysisDecl = {
    doParse(source = analysisSource, defaultSchema = defaultSchema, treeGenerator = _.analysisDeclaration())
  }


}
