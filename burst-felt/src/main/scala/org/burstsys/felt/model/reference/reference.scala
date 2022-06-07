/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model

import org.burstsys.felt.model.reference.path._
import org.burstsys.felt.model.tree._
import org.burstsys.vitals.logging.VitalsLogger

/**
 * =references=
 * References along with 'paths' allow a Felt tree to have declarations tied to names that refer
 * to declarations.
 *
 * @see [[FeltPathExpr]] names that refer to declarations
 * @see [[FeltReference]] artifacts that represent the link between path names and declarations
 */
package object reference extends VitalsLogger {

  final implicit
  class FeltReferenceTreeRules(node: FeltNode) extends FeltTreeRules {

    def allReferenceDecls: Array[FeltRefDecl] = {
      node.treeApply[FeltRefDecl] {
        case pe: FeltRefDecl => Array(pe)
        case _ => Array.empty
      }.distinct
    }

    def linkReferences(): Unit = {
      val linker = node.global.linker

      node.global.linker.nominate(node.allReferenceDecls)

      def resolve(path: FeltPathExpr): Unit = {
        // some paths are not meant to be handled by the referencing mechanism
        if (path.isPassive) return
        // don't double resolve a path
        if (path.reference.nonEmpty) return
        linker.lookupReference(path) match {
          case None =>
          case Some(r) =>
            path.absolutePath = r.nameSpace.absoluteName
            r.resolveTypes
            path.reference = Some(r)
            return
        }
        throw FeltException(path.location, s"'${path.fullPath}' did not resolve to a reference")
      }

      node.allFeltPaths foreach (p => resolve(p))
    }

  }

}
