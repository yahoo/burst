/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.reference

import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.FeltGlobal

/**
 * machinery for resolving ''reference''
 */
abstract class FeltRefResolver {

  def resolverName: String

  def global: FeltGlobal

  /**
   * resolve a path '''refName''' into a reference if possible
   * this is done in the local namespace context of the path located
   *
   * @param refName
   * @return
   */
  def resolve(refName: FeltPathExpr): Option[FeltReference]

  /**
   * resolve an absolute name string into a reference if possible.
   *
   * @param name
   * @return
   */
  def resolveAbsolute(name: String): Option[FeltRefDecl]

  /**
   * nominate a declaration to be installed in resolvers
   *
   * @param decl
   */
  def nominate(decl: FeltRefDecl): Unit

  /**
   * the number of stored declaration bindings (some resolvers will not be able to count these)
   *
   * @return
   */
  def bindingCount: Int

}
