/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.test.model.tree

import org.burstsys.felt.model.tree.FeltContextualizer
import org.scalatest.{Ignore, Suite}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

private case class MockLocationContextualizer(source: String) extends FeltContextualizer

/**
 * (clay) not really sure what this was intended to do but it seemed to break last push I made
 * I do not know why - don't think I changed anything that should impact error messages.
 */
//@Ignore
class FeltContextSpec extends AnyFlatSpec with Suite with Matchers {

  it should "contextualize in a single line source" in {
    val singleLine = MockLocationContextualizer("single line")
    val msg1 = singleLine.contextualize(1, 0, "should be capitalized")
    msg1 should equal(
      """[1] single line
        |    ^ should be capitalized""".stripMargin)
    singleLine.isContextualized(msg1) should equal(true)
  }

  it should "contextualize in multi-line source" in {
    val manyLines = MockLocationContextualizer(
      ("line\n" * 50).split("\n").zipWithIndex.map(e => s"${e._1} ${e._2}").mkString("\n")
    )

    // no previous line
    val msg1 = manyLines.contextualize(1, 5, "Not using 1-based line numbers")
    msg1 should equal(
      """[ 1] line 0
        |          ^ Not using 1-based line numbers
        |[ 2] line 1""".stripMargin)
    manyLines.isContextualized(msg1) should equal(true)

    // previous and subsequent lines
    val msg2 = manyLines.contextualize(25, 5, "Not using 1-based line numbers")
    msg2 should equal(
      """[24] line 23
        |[25] line 24
        |          ^ Not using 1-based line numbers
        |[26] line 25""".stripMargin)
    manyLines.isContextualized(msg2) should equal(true)

    // no subsequent line
    val msg3 = manyLines.contextualize(50, 5, "Not using 1-based line numbers")
    msg3 should equal(
      """[49] line 48
        |[50] line 49
        |          ^ Not using 1-based line numbers""".stripMargin)
    manyLines.isContextualized(msg3) should equal(true)
  }
}
