/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.sweep

import org.burstsys.felt.model.literals.primitive.FeltStrPrimitive
import org.burstsys.felt.model.tree.{FeltNode, FeltTreeRules}

package object lexicon {

  /**
   * turn off lexicon mode (for benchmarks)
   */
  final var globalLexiconDisable = false // false enables lexicon

  /**
   * global lexicon rules to be applied before code generation
   *
   * @param node
   */
  final implicit
  class FeltLexiconTreeRules(node: FeltNode) extends FeltTreeRules {

    def bindLexiconStrings: Array[FeltStrPrimitive] = {
      node.treeApply[FeltStrPrimitive] {
        case n: FeltStrPrimitive =>
          n.lexiconIndex = n.global.lexicon.lookupIndex(n.value)
          Array(n)
        case _ =>
          Array.empty
      }.distinct
    }

  }

}
