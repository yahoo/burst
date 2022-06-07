/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.tree

import org.burstsys.vitals.strings.spaces

import scala.collection.immutable.StringOps

trait FeltContextualizer {

  def source: String

  private case class Loc(prefix: String, line: String)

  private lazy val lines: Array[Loc] = {
    val tmpl = s"[%${s"${(source: StringOps).lines.length}".length}d]"
    (source: StringOps).lines.zipWithIndex.map(l => Loc(tmpl.format(l._2 + 1), l._1)).toArray
  }

  /**
   * print the message in the context of the source that caused it
   *
   * @param line    the line number on which the error occurred (1-based)
   * @param col     the column index on which the error occurred (0-based)
   * @param message a message describing the error
   * @return the message printed in the context of its encosing source
   */
  final
  def contextualize(line: Int, col: Int, message: String): String = {
    val loc = lines(line - 1)
    val offset = loc.prefix.length + 1 + col
    val ptr = s"${spaces(offset)}^"
    val errorLine =
      s"""|${loc.prefix} ${loc.line}
          |$ptr $message""".stripMargin
    val previousLine = if (line > 1 && lines.length > 1) {
      val prevLoc = lines(line - 2)
      s"${prevLoc.prefix} ${prevLoc.line}\n"
    } else ""

    val nextLine = if (line != lines.length) {
      val nextLoc = lines(line)
      s"\n${nextLoc.prefix} ${nextLoc.line}"
    } else ""

    s"$previousLine$errorLine$nextLine"
  }

  final
  def isContextualized(message: String): Boolean = message.matches("""(?s)^\[\s*\d+].+""")
}
