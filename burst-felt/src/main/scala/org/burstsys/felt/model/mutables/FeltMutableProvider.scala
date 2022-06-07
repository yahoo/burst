/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.mutables

import org.burstsys.felt.model.tree.code.cleanClassName

/**
 * FELT delegates the implementation of mutables to the top level language binding agent
 * (currently only the `hydra` concrete language binding)
 */
trait FeltMutableProvider[M <: FeltMutable, B <: FeltMutableBuilder] extends Any {

  def newBuilder: B

  def builderClassName: String

  def mutableClass: Class[_ <: M]

  final def mutableClassName: String = cleanClassName(mutableClass)

  /**
   *
   * @param builder
   * @return
   */
  def grabMutable(builder: B): M

  /**
   *
   * @param mutable
   */
  def releaseMutable(mutable: M): Unit

}

