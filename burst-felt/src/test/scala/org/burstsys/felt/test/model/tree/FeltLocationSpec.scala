/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.test.model.tree

import org.burstsys.felt.model.tree.{FeltGlobal, FeltLocation, FeltContextualizer}
import org.scalatest.Suite
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class FeltLocationSpec extends AnyFlatSpec with Suite with Matchers {

  it should "contextualize from an unknown location" in {
    val loc = FeltLocation()
    val msg = loc.contextualizedErrorMsg("Boom goes the dynamite")
    msg should equal("Boom goes the dynamite: UNKNOWN_LOCATION ")
  }

  it should "contextualize in a known location" in {
    val source =       ("line\n" * 50).split("\n").zipWithIndex.map(e => s"${e._1} ${e._2 + 1}").mkString("\n")
    val global = FeltGlobal(source)
    val loc = FeltLocation(global, 25, 0)
    val msg = loc.contextualizedErrorMsg("should be capitalized")
    msg should equal(
      """[24] line 24
        |[25] line 25
        |     ^ should be capitalized
        |[26] line 26""".stripMargin)
  }

  it should "not recontextualize a location" in {
    val singleLine = FeltLocation(FeltGlobal("single line"), 1, 0)
    val msg1 = singleLine.contextualizedErrorMsg("should be capitalized")
    val msg2 = singleLine.contextualizedErrorMsg(msg1)

    // since the message is already in context, we just pass it right back
    msg1 should equal(msg2)
  }
}
