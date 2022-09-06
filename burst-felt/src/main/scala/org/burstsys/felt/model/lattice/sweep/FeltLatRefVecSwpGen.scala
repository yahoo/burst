/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.lattice.sweep

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.model.schema.types.BrioReferenceVectorRelation
import org.burstsys.felt.model.lattice.FeltLatticeSweepGeneratorContext
import org.burstsys.felt.model.sweep.splice._
import org.burstsys.felt.model.sweep.symbols.{feltRuntimeClass, schemaRuntimeSym, _}
import org.burstsys.felt.model.tree.code.{C, FeltCode, FeltCodeCursor, I, I2, I3}

/**
 * code generation for reference vectors
 */
trait FeltLatRefVecSwpGen {

  self: FeltLatticeSweepGeneratorContext =>

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def genSwpRefVecSplices(implicit cursor: FeltCodeCursor): FeltCode = {
    if (referenceVectorNodes.isEmpty)
      s"""|
          |${C("no reference vector static splices")}""".stripMargin
    else
      s"""|
          |${C("reference vector static splices")}
          |$refVecSplice
          |$refVecMemSplice""".stripMargin
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // INTERNALS
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final private
  def refVecSplice(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |$I@inline override
        |${I}def referenceVectorSplice($schemaRuntimeSym: $feltRuntimeClass, path: Int, placement: Int): Unit = {
        |${sweepRuntimeClassVal(global)(cursor indentRight 1)}
        |${I2}placement match {
        |${I3}case ${FeltVectorAllocPlace.key} =>  // $FeltVectorMemberAllocPlace
        |${generateSpliceCallForPlace(referenceVectorNodes, FeltVectorAllocPlace)(cursor indentRight 3)}
        |${I3}case ${FeltVectorBeforePlace.key} =>  // $FeltVectorBeforePlace
        |${generateSpliceCallForPlace(referenceVectorNodes, FeltVectorBeforePlace)(cursor indentRight 3)}
        |${I3}case ${FeltVectorAfterPlace.key} =>  // $FeltVectorAfterPlace
        |${generateSpliceCallForPlace(referenceVectorNodes, FeltVectorAfterPlace)(cursor indentRight 3)}
        |${I3}case ${FeltVectorFreePlace.key} =>  // $FeltVectorFreePlace
        |${generateSpliceCallForPlace(referenceVectorNodes, FeltVectorFreePlace)(cursor indentRight 3)}
        |${I3}case _ =>
        |$I2}
        |$I}""".stripMargin
  }

  final private
  def refVecMemSplice(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |$I@inline override
        |${I}def referenceVectorMemberSplice($schemaRuntimeSym: $feltRuntimeClass, path: Int, placement: Int): Unit = {
        |${sweepRuntimeClassVal(global)(cursor indentRight 1)}
        |${I2}placement match {
        |${I3}case ${FeltVectorMemberAllocPlace.key} =>  // $FeltVectorMemberAllocPlace
        |${generateSpliceCallForPlace(referenceVectorNodes, FeltVectorMemberAllocPlace)(cursor indentRight 3)}
        |${I3}case ${FeltVectorMemberMergePlace.key} => // $FeltVectorMemberMergePlace
        |${generateSpliceCallForPlace(referenceVectorNodes, FeltVectorMemberMergePlace)(cursor indentRight 3)}
        |${I3}case ${FeltVectorMemberFreePlace.key} => // $FeltVectorMemberFreePlace
        |${generateSpliceCallForPlace(referenceVectorNodes, FeltVectorMemberFreePlace)(cursor indentRight 3)}
        |${I3}case _ =>
        |$I2}
        |$I}""".stripMargin
  }

  final private
  lazy val referenceVectorNodes: Array[BrioNode] = brioSchema.allNodesForForms(BrioReferenceVectorRelation).filter(n => visitedStaticPaths.contains(n.pathKey))

}
