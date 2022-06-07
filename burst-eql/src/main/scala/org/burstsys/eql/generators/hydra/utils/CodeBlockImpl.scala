/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.utils

import scala.language.implicitConversions

class CodeBlockImpl extends CodeBlock {
  def add(line: String): CodeBlock = {
    line.split("\n").foreach(append(_))
    this
  }

  def indent: CodeBlock = {
    I.inc
    this.transform(l => s"$I$l").
      I.dec
    this
  }

  protected val I: Indent = Indent()

  override def add(nb: CodeBlock): CodeBlock = {
    this.appendAll(nb)
    this
  }

  override def source()(implicit cb: CodeBlock): CodeBlock = cb.add(this)
}
