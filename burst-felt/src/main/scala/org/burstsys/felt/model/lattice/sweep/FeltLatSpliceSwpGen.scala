/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.lattice.sweep

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.types.BrioPath.BrioPathKey
import org.burstsys.felt.model.lattice.{FeltLatSwpGen, FeltLatticeSweepGeneratorContext}
import org.burstsys.felt.model.sweep.splice.{FeltPlacement, FeltSplice, FeltSpliceStore}
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, FeltNoCode, I, I2}
import org.burstsys.felt.model.visits.FeltVisitRules

import scala.collection.mutable.ArrayBuffer

/**
 * static methods are those that implement a visit action to a schema based path (not a dynamic code generated collector visit)
 */
trait FeltLatSpliceSwpGen extends FeltLatSwpGen with FeltSpliceStore {

  self: FeltLatticeSweepGeneratorContext =>

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * all paths that are visited or accessed as well as their transitive closure paths to the root.
   */
  private[this] final lazy val _visitedStaticPaths: Map[BrioPathKey, BrioNode] = {
    analysis.staticVisits.map(n => n.pathKey -> n).toMap
  }

  /**
   * these are the splices that are actually ''called'' (because their data is visited)
   */
  private[this]
  val _calledStaticSplices = new ArrayBuffer[FeltSplice]

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def visitedStaticPaths: Map[BrioPathKey, BrioNode] = _visitedStaticPaths

  /**
   * TODO
   *
   * @param treeNodes
   * @param placement
   * @param cursor
   * @return
   */
  final override
  def generateSpliceCallForPlace(treeNodes: Array[BrioNode], placement: FeltPlacement)(implicit cursor: FeltCodeCursor): FeltCode = {
    if (treeNodes.isEmpty) return FeltNoCode
    var hasContent = false
    val staticSpliceCalls = treeNodes.flatMap {
      treeNode =>
        val pathName = treeNode.pathName
        val splices = splicesFor(pathName, placement)
        val spliceCalls = splices.map {
          splice =>
            val call = splice.generateSpliceMethodCall(cursor indentRight 2)
            // track which calls are actually needed...
            if (call.nonEmpty) _calledStaticSplices += splice
            call
        }.mkString
        if (spliceCalls.isEmpty) FeltNoCode else {
          hasContent = true
          s"""|
              |${I2}case  ${treeNode.pathKey} => // $pathName
              |${collectors.generateCollectorPreludesForPlace(pathName, placement)(cursor indentRight 2)}$spliceCalls
              |${collectors.generateCollectorPostludesForPlace(pathName, placement)(cursor indentRight 2)}""".stripMargin
        }
    }.mkString
    if (!hasContent) return FeltNoCode

    s"""|
        |${I}path match {
        |$staticSpliceCalls
        |${I2}case _ =>
        |$I}""".stripMargin
  }


  final override
  def genSwpStaticSpliceBodies(implicit cursor: FeltCodeCursor): FeltCode = {
    _calledStaticSplices.map {
      sm => sm.generateSpliceMethodBody
    }.mkString
  }

}
