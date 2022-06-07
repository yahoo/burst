/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.felt.model.brio.reference.FeltBrioStdRef
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.tree.{FeltNode, FeltTreeRules}
import org.burstsys.felt.model.visits.decl.FeltStaticVisitDecl

/**
 * =visits=
 */
package object visits {

  /**
   * attach ''rules'' to a felt visit tree node
   *
   * @param node
   */
  final implicit
  class FeltVisitRules(node: FeltNode) extends FeltTreeRules {

    def allFeltVisitDecls: Array[FeltStaticVisitDecl] = node.allNodesOfType[FeltStaticVisitDecl]

    def staticVisits: Array[BrioNode] = {
      allFeltVisitDecls.flatMap {
        visit => visit.traverseTarget.referenceGetOrThrow[FeltBrioStdRef].refDecl.brioNode.transitToRoot
      }.distinct
    }

    /**
     * rule to go through all brio path expressions in visit actions so that we
     * can validate the brio references.
     * Currently this is only 'static' or brio schema defined visits
     */
    def validateBrioReferences(): Unit = {
      node treeUpdate {
        case visit: FeltStaticVisitDecl =>
          val brioNode = visit.traverseTarget.referenceGetOrThrow[FeltBrioStdRef].refDecl.brioNode
          visit.actions foreach (_.allNodesOfType[FeltExpression].foreach(_.validateBrioReferences(brioNode)))
        case _ =>
      }
    }

    def validateVisits(): Unit = {
      node treeUpdate {
        case visit: FeltStaticVisitDecl =>
          visit.actions foreach {
            action =>
              val brioNode = visit.traverseTarget.referenceGetOrThrow[FeltBrioStdRef].refDecl.brioNode
              val actionType = action.actionType
              if (!actionType.validRelationForms.contains(brioNode.relation.relationForm))
                throw FeltException(action.location,
                  s"visit for '${brioNode.pathName}' had inappropriate action type '${actionType.name}' Appropriate types are ${
                    actionType.validRelationForms.mkString("{'", "', '", "'}")
                  }"
                )
          }
        case _ =>
      }
    }


  }


}
