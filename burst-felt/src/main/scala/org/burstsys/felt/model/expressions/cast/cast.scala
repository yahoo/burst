/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions

import org.burstsys.felt.model.tree.{FeltNode, FeltTreeRules}
import org.burstsys.vitals.logging.VitalsLogger

/**
 * =casts=
 */
package object cast extends VitalsLogger {

  trait FeltCastExpr extends FeltExpression

  final implicit
  class FeltCastExprTreeRules(node: FeltNode) extends FeltTreeRules {

    def castExprRules(): Unit = {
      node.treeUpdate {
        case cast: FeltCastNumberExpr =>
          if (cast.typeDeclaration.feltType.isString && cast.global.lexicon.enabled) {
            log warn s"FELT_CAST_EXPR_LEXICON_OFF -- cast as string not supported using lexicon optimization yet ${cast.printSource} "
            cast.global.lexicon.disable()
          }
        case _ =>
      }
    }

  }

}
