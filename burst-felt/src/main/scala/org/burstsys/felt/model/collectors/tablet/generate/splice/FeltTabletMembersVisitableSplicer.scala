/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.tablet.generate.splice

import org.burstsys.brio.types.BrioTypes._
import org.burstsys.felt.model.collectors.tablet.decl.{FeltTabletDecl, FeltTabletMembersVisit}
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.sweep.splice.{FeltEmptySpliceGenerator, FeltSpliceGenerator}
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.{C, FeltCode, I, I2}
import org.burstsys.felt.model.visits.decl.FeltVisitableSplicer

/**
 *
 * @param refName
 */
final case
class FeltTabletMembersVisitableSplicer(refName: FeltPathExpr) extends FeltVisitableSplicer {

  private
  val global: FeltGlobal = refName.global

  private
  val tabletName: FeltCode =
    refName.absolutePathSansRoot.stripSuffix(FeltTabletMembersVisit.extension).stripSuffix(".")

  private
  def tabletDecl: FeltTabletDecl =
    global.linker.lookupDeclFromAbsoluteOrThrow[FeltTabletDecl](tabletName)

  private
  def contentTypeKey: BrioTypeKey = tabletDecl.feltType.valueType

  override def visitablePrepare(visitTag: String): FeltSpliceGenerator = FeltEmptySpliceGenerator

  override def visitableStartLoop(visitorTag: String): FeltSpliceGenerator = {
    implicit cursor => {
      val reference = tabletDecl.reference
      val routeInstance = s"$sweepRuntimeSym.${reference.instanceVariable}"
      val accessor = contentTypeKey match {
        case BrioBooleanKey => s"$routeInstance.tabletBooleanAt( i )"
        case BrioByteKey => s"$routeInstance.tabletByteAt( i )"
        case BrioShortKey => s"$routeInstance.tabletShortAt( i )"
        case BrioIntegerKey => s"$routeInstance.tabletIntAt( i )"
        case BrioLongKey => s"$routeInstance.tabletLongAt( i )"
        case BrioDoubleKey => s"$routeInstance.tabletDoubleAt( i )"
        case BrioStringKey => s"$routeInstance.tabletStringAt( i )( $sweepRuntimeSym, runtime.dictionary )"
      }
      s"""|
          |${C("START of TABLET MEMBER iteration")}
          |${I}val memberCount: Int = $routeInstance.tabletSize
          |${I}var i = 0;
          |${I}while (i < memberCount) {
          |${I2}$sweepRuntimeSym.${reference.memberIsFirstVariable} = (i == 0) ;
          |${I2}$sweepRuntimeSym.${reference.memberIsLastVariable} = (i == (memberCount - 1)) ;
          |${I2}$sweepRuntimeSym.${reference.memberValueVariable} = $accessor;""".stripMargin
    }
  }

  override def visitableMemberPrepare(visitTag: String): FeltSpliceGenerator = FeltEmptySpliceGenerator

  override def visitableNestedIteration(visitTag: String): FeltSpliceGenerator = FeltEmptySpliceGenerator

  override def visitableMemberCleanup(visitTag: String): FeltSpliceGenerator = FeltEmptySpliceGenerator

  override def visitableEndLoop(visitTag: String): FeltSpliceGenerator = {
    implicit cursor => {
      s"""|
          |${I2}i += 1;
          |${I}}
          |${C("END of TABLET MEMBER iteration")}""".stripMargin
    }
  }

  override def visitableCleanup(visitTag: String): FeltSpliceGenerator = FeltEmptySpliceGenerator

  override def generateVisitableRtDecls(visitTag: String): FeltSpliceGenerator = FeltEmptySpliceGenerator

}
