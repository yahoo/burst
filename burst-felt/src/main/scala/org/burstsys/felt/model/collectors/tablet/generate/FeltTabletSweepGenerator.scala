/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.tablet.generate

import org.burstsys.brio.types.BrioPath.BrioPathName
import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.collectors.generate.FeltCollectorSweepGenerator
import org.burstsys.felt.model.collectors.tablet.decl.FeltTabletDecl
import org.burstsys.felt.model.collectors.tablet.generate.splice.FeltTabletSplicer
import org.burstsys.felt.model.sweep.splice.{FeltPlacement, FeltSplice}
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.{C, FeltCode, FeltCodeCursor, FeltNoCode, I}
import org.burstsys.vitals.strings.VitalsGeneratingArray

/**
 *
 */
trait FeltTabletSweepGenerator extends FeltCollectorSweepGenerator {

}

object FeltTabletSweepGenerator {
  def apply(analysis: FeltAnalysisDecl): FeltTabletSweepGenerator =
    FeltTabletSweepGeneratorContext(analysis: FeltAnalysisDecl)
}

final case
class FeltTabletSweepGeneratorContext(analysis: FeltAnalysisDecl) extends AnyRef
  with FeltTabletSweepGenerator with FeltSweepTabletGen {

  override def global: FeltGlobal = analysis.global

  private val splicer = FeltTabletSplicer(analysis)

  def tablets: Array[FeltTabletDecl] = analysis.tablets

  override def collectSplices: Array[FeltSplice] = splicer.collectSplices

  override def genRtCollectorBlk(implicit cursor: FeltCodeCursor): FeltCode = {

    def tablet(tablet: FeltTabletDecl)(implicit cursor: FeltCodeCursor): FeltCode = {
      val reference = tablet.reference
      val contentTypeKey = tablet.feltType.valueTypeAsCode
      s"""|
          |${C(s"tablet variable(s) for '${tablet.tabletName}'")}
          |${I}var ${reference.rootVariable} : ${binding.collectors.tablets.collectorClassName} = _ ;
          |${I}var ${reference.instanceVariable} : ${binding.collectors.tablets.collectorClassName} = _ ;
          |${I}var ${reference.memberIsFirstVariable} : Boolean = _ ;
          |${I}var ${reference.memberIsLastVariable} :  Boolean = _ ;
          |${I}var ${reference.memberValueVariable} : $contentTypeKey = _ ;
          |""".stripMargin
    }

    if (tablets.isEmpty) FeltNoCode
    else
      s"""|
          |${C("tablet variable(s)")}
          |${tablets.map(tablet).stringify}""".stripMargin
  }

  override def genSwpCollectorMetadata(implicit cursor: FeltCodeCursor): FeltCode = genSwpTabletMetadata

  override def generateCollectorPreludesForPlace(pathName: BrioPathName, placement: FeltPlacement)(implicit cursor: FeltCodeCursor): FeltCode = FeltNoCode

  override def generateCollectorPostludesForPlace(pathName: BrioPathName, placement: FeltPlacement)(implicit cursor: FeltCodeCursor): FeltCode = FeltNoCode

}
