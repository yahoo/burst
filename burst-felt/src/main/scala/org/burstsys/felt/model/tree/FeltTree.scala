/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.tree

import scala.reflect.ClassTag

/**
 * operations on node trees and subtrees
 */
trait FeltTree extends AnyRef {

  def children: Array[_ <: FeltNode] = emptyNodeArray

  /**
   * Apply a ''rule'' to this node and recursively to it's children returning an array of type [[R]]
   * <p>'''NOTE:''' DO NOT MODIFY THE TOPOLOGY OF THE TREE IN THIS CALL!
   *
   * @param rule
   * @tparam R
   * @return
   */
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R]

  /**
   * Apply a ''rule'' to this node and recursively to it's children
   * <p>'''NOTE:''' DO NOT MODIFY THE TOPOLOGY OF THE TREE IN THIS CALL!
   *
   * @param rule
   */
  final
  def treeUpdate(rule: FeltNode => Unit): Unit = {
    treeApply[FeltNode]({
      n =>
        rule(n)
        Array.empty
    })
  }

  /**
   * helper function to return all nodes in a Felt tree of a specific node '''type'''
   *
   * @tparam N
   * @return
   */
  final
  def allNodesOfType[N <: FeltNode : ClassTag]: Array[N] = {
    treeApply[N] {
      case pe: N => Array(pe)
      case _ => Array.empty
    }
  }

  /**
   * is this node ready for TYPE INFERENCE?
   *
   * @return
   */
  def canInferTypes: Boolean

}
