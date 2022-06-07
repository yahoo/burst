/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.reference

import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.tree.{FeltNode, FeltTreeRules}

package object path {

  /**
   * a rule to find and wire up  dynamic path '''branches''' into an overlay placed on top of the
   * object tree traversal Brio static schema driven model.
   *
   * @param node
   */
  final implicit
  class FeltPathRules(node: FeltNode) extends FeltTreeRules {

    def allFeltPaths: Array[FeltPathExpr] = node.allNodesOfType[FeltPathExpr]

    //    def bindPaths(): Unit = allFeltPaths.foreach(n => n.bind(n.global.brioSchema))

  }

  final case class FeltSimplePath(simpleName: String) extends FeltPathExpr {
    override val components: Array[String] = simpleName.split('.')
    override val key: Option[FeltExpression] = None
  }
}
