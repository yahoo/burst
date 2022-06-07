/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.model.schema.types.BrioRelationForm
import org.burstsys.felt.model.brio.reference.FeltBrioStdRef
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.reference.path.FeltPathExpr

/**
 * =brio=
 */
package object brio {


  /**
   * support behavior that validates reachability semantics for traversals and brio references
   */
  trait FeltReachValidator extends Any {

    final
    def validateReach(traversePt: BrioNode, expr: FeltExpression, forms: BrioRelationForm*): Unit = {
      expr match {
        case path: FeltPathExpr =>
          path.reference.get match {
            case ref: FeltBrioStdRef =>
              if (!traversePt.canReachRelation(ref.refDecl.brioNode, forms: _*))
                throw FeltException(
                  expr.location,
                  s"brio object tree traverse point '${traversePt.pathName}' can't reach brio relation '${path.fullPathNoQuotes}'"
                )
            case _ =>
          }
        case _ =>
      }
    }

  }

}
