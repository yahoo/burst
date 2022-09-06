/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.lattice.sweep

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.model.schema.types.BrioValueMapRelation
import org.burstsys.felt.model.lattice.{FeltLatSwpGen, FeltLatticeSweepGeneratorContext}
import org.burstsys.felt.model.sweep.splice._
import org.burstsys.felt.model.sweep.symbols.{feltRuntimeClass, schemaRuntimeSym, _}
import org.burstsys.felt.model.tree.code.{C, FeltCode, FeltCodeCursor, I, I2, I3}

/**
 * code generation for value maps
 */
trait FeltLatValMapSwpGen extends FeltLatSwpGen {

  self: FeltLatticeSweepGeneratorContext =>

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def genSwpValMapSplices(implicit cursor: FeltCodeCursor): FeltCode = {
    if (valueMapNodes.isEmpty)
      s"""|
          |${C("no value map static splices")}""".stripMargin
    else
      s"""|
          |${C("value map static splices")}
          |$valMapSplice
          |$valMapMemberSplice""".stripMargin
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // INTERNALS
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final private
  def valMapSplice(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |$I@inline override
        |${I}def valueMapSplice($schemaRuntimeSym: $feltRuntimeClass, path: Int, placement: Int): Unit = {
        |${sweepRuntimeClassVal(global)(cursor indentRight 1)}
        |${I2}placement match {
        |${I3}case ${FeltVectorAllocPlace.key} =>  // $FeltVectorAllocPlace
        |${generateSpliceCallForPlace(valueMapNodes, FeltVectorAllocPlace)(cursor indentRight 3)}
        |${I3}case ${FeltVectorBeforePlace.key}  => // $FeltVectorBeforePlace
        |${generateSpliceCallForPlace(valueMapNodes, FeltVectorBeforePlace)(cursor indentRight 3)}
        |${I3}case ${FeltVectorAfterPlace.key}  => // $FeltVectorAfterPlace
        |${generateSpliceCallForPlace(valueMapNodes, FeltVectorAfterPlace)(cursor indentRight 3)}
        |${I3}case ${FeltVectorFreePlace.key}  => // $FeltVectorFreePlace
        |${generateSpliceCallForPlace(valueMapNodes, FeltVectorFreePlace)(cursor indentRight 3)}
        |${I3}case _ =>
        |$I2}
        |$I}""".stripMargin
  }

  final private
  def valMapMemberSplice(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |$I@inline override
        |${I}def valueMapMemberSplice($schemaRuntimeSym: $feltRuntimeClass, path: Int, placement: Int): Unit = {
        |${sweepRuntimeClassVal(global)(cursor indentRight 1)}
        |${I2}placement match {
        |${I3}case ${FeltVectorMemberAllocPlace.key}  => // $FeltVectorMemberAllocPlace
        |${generateSpliceCallForPlace(valueMapNodes, FeltVectorMemberAllocPlace)(cursor indentRight 3)}
        |${I3}case ${FeltVectorMemberSituPlace.key}  => // $FeltVectorMemberSituPlace
        |${generateSpliceCallForPlace(valueMapNodes, FeltVectorMemberSituPlace)(cursor indentRight 3)}
        |${I3}case ${FeltVectorMemberMergePlace.key}  => // $FeltVectorMemberMergePlace
        |${generateSpliceCallForPlace(valueMapNodes, FeltVectorMemberMergePlace)(cursor indentRight 3)}
        |${I3}case ${FeltVectorMemberFreePlace.key}  => // $FeltVectorMemberFreePlace
        |${generateSpliceCallForPlace(valueMapNodes, FeltVectorMemberFreePlace)(cursor indentRight 3)}
        |${I3}case _ =>
        |$I2}
        |$I}""".stripMargin
  }

  final private
  lazy val valueMapNodes: Array[BrioNode] = brioSchema.allNodesForForms(BrioValueMapRelation).filter(n => visitedStaticPaths.contains(n.pathKey))

}
