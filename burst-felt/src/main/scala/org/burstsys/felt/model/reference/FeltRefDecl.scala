/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.reference

import org.burstsys.felt.model.FeltDeclaration
import org.burstsys.felt.model.reference.path.FeltPathExpr

/**
 * a special declaration that can be referred to by a [[FeltPathExpr]] anywhere in the AST and be connected
 * by a [[FeltReference]]
 */
trait FeltRefDecl extends FeltDeclaration {

  /**
   * the ''name'' (path) specified here for later referencing (elsewhere in expressions) to this declaration
   *
   * @return
   */
  def refName: FeltPathExpr

}
