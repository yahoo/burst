/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.generate

import org.burstsys.brio.model.schema
import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.model.schema.types.BrioValueScalarRelation
import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.collectors.cube.generate.splice.FeltCubeSplicer
import org.burstsys.felt.model.collectors.generate.FeltCollectorSweepGenerator
import org.burstsys.felt.model.sweep.splice.FeltSplice
import org.burstsys.felt.model.tree.FeltGlobal

/**
 *
 */
trait FeltCubeSweepGenerator extends FeltCollectorSweepGenerator {

}

object FeltCubeSweepGenerator {
  def apply(analysis: FeltAnalysisDecl): FeltCubeSweepGenerator =
    FeltCubeSweepGeneratorContext(analysis: FeltAnalysisDecl)
}

final case
class FeltCubeSweepGeneratorContext(analysis: FeltAnalysisDecl) extends AnyRef
  with FeltCubeSweepGenerator with FeltCubeSweepMetaGen with FeltCubeSweepRtGen with FeltCubeSweepLudeGen {

  override def global: FeltGlobal = analysis.global

  override def collectSplices: Array[FeltSplice] =
    analysis.cubes.flatMap(FeltCubeSplicer(_).collectSplices)

  def allCubeTargetNodes: Array[BrioNode] = {
    (for (i <- schema.rootPathKey to brioSchema.pathCount) yield {
      val node = brioSchema.nodeForPathKey(i)
      if (node.relation.relationForm != BrioValueScalarRelation) node else null
    }).filter(_ != null).toArray
  }

}
