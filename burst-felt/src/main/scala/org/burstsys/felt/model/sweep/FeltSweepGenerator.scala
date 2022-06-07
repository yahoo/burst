/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.sweep

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.felt.binding.FeltBinding
import org.burstsys.felt.model.tree.FeltGlobal

/**
 * Superclass for all sweep generators
 */
trait FeltSweepGenerator extends AnyRef {

  /**
   * convenience access to the global
   *
   * @return
   */
  def global: FeltGlobal

  /**
   * current brio schema
   *
   * @return
   */
  def brioSchema: BrioSchema = global.feltSchema

  /**
   * current felt implementation bindings
   *
   * @return
   */
  def binding: FeltBinding = global.binding

}

