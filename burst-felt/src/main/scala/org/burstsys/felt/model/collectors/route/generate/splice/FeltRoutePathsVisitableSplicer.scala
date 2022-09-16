/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.generate.splice

import org.burstsys.felt.model.collectors.route.decl.FeltRouteDecl
import org.burstsys.felt.model.collectors.route.decl.visit.FeltRoutePathsVisit
import org.burstsys.felt.model.collectors.route.functions.fsm.FeltRouteFsmBackFillFunc
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.sweep.splice.{FeltEmptySpliceGenerator, FeltSpliceGenerator}
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.{C, FeltCode, I, I2, I3}
import org.burstsys.felt.model.visits.decl.FeltVisitableSplicer

/**
 * =WARN: NOT TESTED OR WORKING=
 *
 * @param refName
 */
final case
class FeltRoutePathsVisitableSplicer(refName: FeltPathExpr) extends FeltVisitableSplicer {

  private
  val global: FeltGlobal = refName.global

  private
  val routeName: FeltCode =
    refName.absolutePathSansRoot.stripSuffix(FeltRoutePathsVisit.extension).stripSuffix(".")

  private
  def routeDecl: FeltRouteDecl =
    global.linker.lookupDeclFromAbsoluteOrThrow[FeltRouteDecl](routeName)

  override def generateVisitableRtDecls(visitTag: String): FeltSpliceGenerator = FeltEmptySpliceGenerator

  override def visitablePrepare(visitTag: String): FeltSpliceGenerator = FeltEmptySpliceGenerator

  override def visitableStartLoop(visitorTag: String): FeltSpliceGenerator = {
    implicit cursor => {
      val reference = routeDecl.reference
      val routeInstance = s"$sweepRuntimeSym.${reference.instanceVariable}"
      val backFillCall = FeltRouteFsmBackFillFunc.call(reference)
      s"""|
          |${C("START of route paths.steps iteration")}
          |${I}$routeInstance.startIteration ;
          |${I}var lastPathOrdinal = -1
          |${I}while ($routeInstance.firstOrNextIterable) {
          |${I2}$routeInstance.$backFillCall ;
          |${I2}if ($routeInstance.currentIteration.pathOrdinal != lastPathOrdinal) {
          |${I3}lastPathOrdinal = $routeInstance.currentIteration.pathOrdinal
          |""".stripMargin
    }
  }

  override def visitableMemberPrepare(visitTag: String): FeltSpliceGenerator = FeltEmptySpliceGenerator

  override def visitableNestedIteration(visitTag: String): FeltSpliceGenerator = FeltEmptySpliceGenerator

  override def visitableMemberCleanup(visitTag: String): FeltSpliceGenerator = FeltEmptySpliceGenerator

  override def visitableEndLoop(visitTag: String): FeltSpliceGenerator = {
    implicit cursor => {
      s"""|
          |${I}}}
          |${C("END of route steps iteration")}""".stripMargin
    }
  }

  override def visitableCleanup(visitTag: String): FeltSpliceGenerator = FeltEmptySpliceGenerator

}
