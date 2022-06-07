/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.utils

object Indent {
  def apply(): Indent = new Indent(3)

  def apply(size: Int) = new Indent(size)
}

final class Indent(private val count: Int = 1, private var level: Int = 0) {
  def inc: Int = {
    level = (level + 1).min(Int.MaxValue - 1); level
  }

  def dec: Int = {
    level = (level - 1).max(0); level
  }

  def currentLevel: Int = level

  override def toString: String = {
    val f = " " * (level * count)
    f
  }
}
