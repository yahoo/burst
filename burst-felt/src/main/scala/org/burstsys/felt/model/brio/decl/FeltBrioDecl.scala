/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.brio.decl

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.felt.model.reference.FeltRefDecl
import org.burstsys.felt.model.reference.names.FeltNamedNode
import org.burstsys.felt.model.tree.code.FeltNoCode

/**
 * A decl placed in the namespace based on relation in the brio schema
 */
trait FeltBrioDecl extends FeltRefDecl with FeltNamedNode {

  /**
   * the brio schema tree node
   *
   * @return
   */
  def brioNode: BrioNode

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String = FeltNoCode

}
