/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.tree

trait FeltFeatures {

  def ctrlVerbs: Boolean

  def ctrlVerbs_=(state: Boolean): Unit

  final def printFeatures: String = s"ctrlVerbs=$ctrlVerbs"

}
