/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra

import scala.collection.mutable.ArrayBuffer
import scala.language.implicitConversions

package object utils {
  implicit class CodeBlockString(s: String) {
    def source()(implicit cb: CodeBlock): CodeBlock = cb.add(s)

    def indentSource()(implicit cb: CodeBlock): CodeBlock = cb.add(CodeBlock(s).indent)
  }

  object CodeBlock {
    def apply(body: CodeBlock => Unit): CodeBlock = {
      val b = new CodeBlockImpl()
      body(b)
      b
    }

    def apply(): CodeBlock = {
      new CodeBlockImpl()
    }

    def apply(s: String): CodeBlock = {
      val cb = new CodeBlockImpl()
      cb.add(s)
      cb
    }

    implicit def stringToCodeBlock(s: String): CodeBlock = {
      CodeBlock(s)
    }

    def Empty: CodeBlock = CodeBlock()
  }

  trait CodeBlock extends ArrayBuffer[String] {
    def add(line: String): CodeBlock

    def add(nb: CodeBlock): CodeBlock

    def indent: CodeBlock

    def source()(implicit cb: CodeBlock): CodeBlock
  }

}
