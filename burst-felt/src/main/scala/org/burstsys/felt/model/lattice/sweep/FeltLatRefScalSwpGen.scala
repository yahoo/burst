/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.lattice.sweep

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.model.schema.types.{BrioReferenceScalarRelation, BrioReferenceVectorRelation, BrioValueMapRelation, BrioValueVectorRelation}
import org.burstsys.felt.model.lattice.FeltLatticeSweepGeneratorContext
import org.burstsys.felt.model.sweep.splice._
import org.burstsys.felt.model.sweep.symbols.{feltRuntimeClass, schemaRuntimeSym, _}
import org.burstsys.felt.model.tree.code.{C, FeltCode, FeltCodeCursor, I, I2, I3}

/**
 * code generation for reference scalars
 */
trait FeltLatRefScalSwpGen {

  self: FeltLatticeSweepGeneratorContext =>

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def genSwpRefScalSplices(implicit cursor: FeltCodeCursor): FeltCode = {
    if (referenceScalarNodes.isEmpty)
      s"""|
          |${C("no reference scalar static splices")}""".stripMargin
    else
      s"""|
          |${C("reference scalar static splices")}
          |$genStatRefScalSplice""".stripMargin
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // INTERNALS
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final private
  def genStatRefScalSplice(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |$I@inline override
        |${I}def referenceScalarSplice($schemaRuntimeSym: $feltRuntimeClass, path: Int, placement: Int): Unit = {
        |${sweepRuntimeClassVal(global)(cursor indentRight 1)}
        |${I2}placement match {
        |${I3}case ${FeltInstanceAllocPlace.key} ⇒ // $FeltInstanceAllocPlace
        |${generateSpliceCallForPlace(referenceScalarNodes, FeltInstanceAllocPlace)(cursor indentRight 3)}
        |${I3}case ${FeltInstancePrePlace.key} ⇒ // $FeltInstancePrePlace
        |${generateSpliceCallForPlace(referenceScalarNodes, FeltInstancePrePlace)(cursor indentRight 3)}
        |${I3}case ${FeltChildMergePlace.key} ⇒ // $FeltChildMergePlace
        |${generateSpliceCallForPlace(referenceScalarNodes, FeltChildMergePlace)(cursor indentRight 3)}
        |${I3}case ${FeltInstancePostPlace.key} ⇒ // $FeltInstancePostPlace
        |${generateSpliceCallForPlace(referenceScalarNodes, FeltInstancePostPlace)(cursor indentRight 3)}
        |${I3}case ${FeltChildJoinPlace.key} ⇒ // $FeltChildJoinPlace
        |${generateSpliceCallForPlace(referenceScalarNodes, FeltChildJoinPlace)(cursor indentRight 3)}
        |${I3}case ${FeltInstanceFreePlace.key} ⇒ // $FeltInstanceFreePlace
        |${generateSpliceCallForPlace(referenceScalarNodes, FeltInstanceFreePlace)(cursor indentRight 3)}
        |${I3}case _ ⇒
        |$I2}
        |$I}""".stripMargin
  }

  /**
   * Note that reference vectors include a scalar reference for each vector member...
   */
  final private
  lazy val referenceScalarNodes: Array[BrioNode] =
    (brioSchema.allNodesForForms(BrioReferenceScalarRelation) ++ brioSchema.allNodesForForms(BrioReferenceVectorRelation) ++
      brioSchema.allNodesForForms(BrioValueVectorRelation) ++ brioSchema.allNodesForForms(BrioValueMapRelation)).filter(n => visitedStaticPaths.contains(n.pathKey))

}
