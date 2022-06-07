/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.shrub.generate

import org.burstsys.brio.types.BrioPath.BrioPathName
import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.collectors.generate.FeltCollectorSweepGenerator
import org.burstsys.felt.model.collectors.shrub.generate.splice.FeltShrubSplicer
import org.burstsys.felt.model.sweep.splice.{FeltPlacement, FeltSplice}
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, FeltNoCode}

/**
 *
 */
trait FeltShrubSweepGenerator extends FeltCollectorSweepGenerator {

}

object FeltShrubSweepGenerator {
  def apply(analysis: FeltAnalysisDecl): FeltShrubSweepGenerator =
    FeltShrubSweepGeneratorContext(analysis: FeltAnalysisDecl)
}

final case
class FeltShrubSweepGeneratorContext(analysis: FeltAnalysisDecl) extends AnyRef
  with FeltShrubSweepGenerator {

  override def global: FeltGlobal = analysis.global

  override def collectSplices: Array[FeltSplice] =
    analysis.shrubs.flatMap(FeltShrubSplicer(_).collectSplices)

  override def genRtCollectorBlk(implicit cursor: FeltCodeCursor): FeltCode = FeltNoCode

  override def genSwpCollectorMetadata(implicit cursor: FeltCodeCursor): FeltCode = FeltNoCode

  override def generateCollectorPreludesForPlace(pathName: BrioPathName, placement: FeltPlacement)(implicit cursor: FeltCodeCursor): FeltCode = FeltNoCode

  override def generateCollectorPostludesForPlace(pathName: BrioPathName, placement: FeltPlacement)(implicit cursor: FeltCodeCursor): FeltCode = FeltNoCode

}
