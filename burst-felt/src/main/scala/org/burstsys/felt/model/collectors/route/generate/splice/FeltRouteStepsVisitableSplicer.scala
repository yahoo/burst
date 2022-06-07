/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.generate.splice

import org.burstsys.felt.model.collectors.route.decl.FeltRouteDecl
import org.burstsys.felt.model.collectors.route.decl.visit.FeltRouteStepsVisit
import org.burstsys.felt.model.collectors.route.functions.fsm.FeltRouteFsmBackFillFunc
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.sweep.splice.{FeltEmptySpliceGenerator, FeltSpliceGenerator}
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.visits.decl.FeltVisitableSplicer

final case
class FeltRouteStepsVisitableSplicer(refName: FeltPathExpr) extends FeltVisitableSplicer {

  private
  val global: FeltGlobal = refName.global

  private
  val routeName: FeltCode =
    refName.absolutePathSansRoot.stripSuffix(FeltRouteStepsVisit.extension).stripSuffix(".")

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
          |${C("START of route steps iteration")}
          |${I1}$routeInstance.$backFillCall ;
          |${I1}var i = 0;
          |${I1}val entryCount = $routeInstance.routeStepCount;
          |${I1}while ($routeInstance.firstOrNextIterable) {
          |${I2}$sweepRuntimeSym.${reference.stepIsFirstVariable} = (i == 0) ;
          |${I2}$sweepRuntimeSym.${reference.pathIsFirstVariable} = (i == 0) ;
          |${I2}$sweepRuntimeSym.${reference.stepIsLastVariable} = (i == (entryCount - 1))
          |${I2}//$sweepRuntimeSym.${reference.pathIsLastVariable} = ??? ;""".stripMargin
      // TODO %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
      // TODO pathIsLastVariable
      // TODO %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    }
  }

  override def visitableMemberPrepare(visitTag: String): FeltSpliceGenerator = FeltEmptySpliceGenerator

  override def visitableNestedIteration(visitTag: String): FeltSpliceGenerator = FeltEmptySpliceGenerator

  override def visitableMemberCleanup(visitTag: String): FeltSpliceGenerator = FeltEmptySpliceGenerator

  override def visitableEndLoop(visitTag: String): FeltSpliceGenerator = {
    implicit cursor => {
      val reference = routeDecl.reference
      val routeInstance = s"$sweepRuntimeSym.${reference.instanceVariable}"
      s"""|
          |${I2}i += 1;
          |${I1}}
          |${C("END of route steps iteration")}""".stripMargin
    }
  }

  override def visitableCleanup(visitTag: String): FeltSpliceGenerator = {
    implicit cursor => {
      val routeInstance = s"$sweepRuntimeSym.${routeDecl.reference.instanceVariable}"
      s"""|
          |${C("make ready for another use on this route")}
          |$I//$routeInstance.reset ; // Reset shouldn't happen here, but on entry of route scope""".stripMargin
      //TODO this requires major rethinking of scoping of routes
    }
  }

}
