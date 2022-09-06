/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.brio

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.model.schema.types.BrioValueScalarRelation
import org.burstsys.felt.model.sweep.symbols.skipTunnelPathSym
import org.burstsys.felt.model.tree.code.{C, FeltCode, FeltCodeCursor, I, I2, I3}
import org.burstsys.felt.model.tree.{FeltNode, FeltTreeRules}
import org.burstsys.vitals.strings._

package object reference {

  final implicit
  class FeltBrioTreeRules(node: FeltNode) extends FeltTreeRules {

    /**
     * find all brio references within our current subtree - return those are their schema 'ancestors'...
     *
     * @return
     */
    def accessedRefScalars: Array[BrioNode] = {
      node.treeApply[BrioNode] {
        case brioRef: FeltBrioStdRef =>
          brioRef.refDecl.brioNode.transitToRoot
        case _ => Array.empty
      }.distinct.filter(_.relation.relationForm != BrioValueScalarRelation)
    }

  }

  final
  def genSwpSkipTunnels(node: FeltNode)(implicit cursor: FeltCodeCursor): FeltCode = {
    def tunneledPaths(implicit cursor: FeltCodeCursor): FeltCode = {
      node.accessedRefScalars.map {
        n =>
          s"""|
              |${I}case ${n.pathKey} => false  // '${n.pathName}'""".stripMargin
      }.stringify
    }
    s"""|
        |${C(s"which paths to tunnel and which to skip")}
        |$I@inline override
        |${I}def $skipTunnelPathSym(pathKey: Int): Boolean = {
        |${I2}pathKey match {${tunneledPaths(cursor indentRight 2)}
        |${I3}case _ => true
        |$I2}
        |$I}""".stripMargin
  }

}
