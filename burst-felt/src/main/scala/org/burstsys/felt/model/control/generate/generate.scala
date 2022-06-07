/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.control

import org.burstsys.felt.model.control.functions.FeltCtrlVerbFunc
import org.burstsys.felt.model.tree.{FeltNode, FeltTreeRules}

package object generate {

  final implicit
  class FeltCtrlTreeRules(node: FeltNode) extends FeltTreeRules {

    def controlVerbs: Array[FeltCtrlVerbFunc] = {
      node.treeApply[FeltCtrlVerbFunc] {
        case n: FeltCtrlVerbFunc => Array(n)
        case _ => Array.empty
      }
    }

    def detectCtrlVerbs(): Unit = {
      node.treeUpdate {
        case n: FeltCtrlVerbFunc =>
          node.global.features.ctrlVerbs = true
        case _ =>
      }
    }

  }

}
