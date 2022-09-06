/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.lattice.sweep

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.model.schema.types.BrioValueVectorRelation
import org.burstsys.felt.model.lattice.FeltLatticeSweepGeneratorContext
import org.burstsys.felt.model.sweep.splice._
import org.burstsys.felt.model.sweep.symbols.{feltRuntimeClass, schemaRuntimeSym, _}
import org.burstsys.felt.model.tree.code.{C, FeltCode, FeltCodeCursor, I, I2, I3}

/**
 * code generation for value vectors
 */
trait FeltLatValVecSwpGen {

  self: FeltLatticeSweepGeneratorContext =>

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def genSwpValVecSplices(implicit cursor: FeltCodeCursor): FeltCode = {
    if (valueVectorNodes.isEmpty)
      s"""|
          |${C("no value vector static splices")}""".stripMargin
    else
      s"""|
          |${C("value vector static splices")}
          |$valVecSplice
          |$valVecMemSplice""".stripMargin
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // INTERNALS
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final private
  def valVecSplice(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |$I@inline override
        |${I}def valueVectorSplice($schemaRuntimeSym: $feltRuntimeClass, path: Int, placement: Int): Unit = {
        |${sweepRuntimeClassVal(global)(cursor indentRight 1)}
        |${I2}placement match {
        |${I3}case ${FeltVectorAllocPlace.key} =>  // $FeltVectorAllocPlace
        |${generateSpliceCallForPlace(valueVectorNodes, FeltVectorAllocPlace)(cursor indentRight 3)}
        |${I3}case ${FeltVectorBeforePlace.key} =>  // $FeltVectorBeforePlace
        |${generateSpliceCallForPlace(valueVectorNodes, FeltVectorBeforePlace)(cursor indentRight 3)}
        |${I3}case ${FeltVectorAfterPlace.key} =>  // $FeltVectorAfterPlace
        |${generateSpliceCallForPlace(valueVectorNodes, FeltVectorAfterPlace)(cursor indentRight 3)}
        |${I3}case ${FeltVectorFreePlace.key} =>  // $FeltVectorFreePlace
        |${generateSpliceCallForPlace(valueVectorNodes, FeltVectorFreePlace)(cursor indentRight 3)}
        |${I3}case _ =>
        |$I2}
        |$I}""".stripMargin
  }

  final private
  def valVecMemSplice(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |$I@inline override
        |${I}def valueVectorMemberSplice($schemaRuntimeSym: $feltRuntimeClass, path: Int, placement: Int): Unit = {
        |${sweepRuntimeClassVal(global)(cursor indentRight 1)}
        |${I2}placement match {
        |${I3}case ${FeltVectorMemberAllocPlace.key} =>  // $FeltVectorMemberAllocPlace
        |${generateSpliceCallForPlace(valueVectorNodes, FeltVectorMemberAllocPlace)(cursor indentRight 3)}
        |${I3}case ${FeltVectorMemberSituPlace.key} =>  // $FeltVectorMemberSituPlace
        |${generateSpliceCallForPlace(valueVectorNodes, FeltVectorMemberSituPlace)(cursor indentRight 3)}
        |${I3}case ${FeltVectorMemberMergePlace.key} =>  // $FeltVectorMemberMergePlace
        |${generateSpliceCallForPlace(valueVectorNodes, FeltVectorMemberMergePlace)(cursor indentRight 3)}
        |${I3}case ${FeltVectorMemberFreePlace.key} =>  // $FeltVectorMemberFreePlace
        |${generateSpliceCallForPlace(valueVectorNodes, FeltVectorMemberFreePlace)(cursor indentRight 3)}
        |${I3}case _ =>
        |$I2}
        |$I}""".stripMargin
  }

  final private
  lazy val valueVectorNodes: Array[BrioNode] = brioSchema.allNodesForForms(BrioValueVectorRelation).filter(n => visitedStaticPaths.contains(n.pathKey))

}
