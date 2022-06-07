/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.parse

import org.burstsys.hydra.test.support.HydraSpecSupport
import org.scalatest.Ignore

/**
  * ← ⇒
  */
//@Ignore
class HydraMatchParseSpec extends HydraSpecSupport {

  it should "parse match clause 1" in {

    implicit val source: String =
      s"""
         |user.sessions.id match {
         |    case "foo" ⇒ {
         |    }
         |    case 5 ⇒ {
         |    }
         |    case _ ⇒ {
         |    }
         |}
       """.stripMargin
    val expr = parser printGeneration (_.parseAnalysis(wrap, schema))
  }

  it should "parse match clause 2" in {
    implicit val source: String =
      s"""
         |user.sessions.id match {
         |    case "foo" ⇒ {
         |      ???
         |    }
         |    case user.id ⇒ {
         |      ???
         |    }
         |    case _ ⇒ {
         |      var i:double = 2.0
         |      ???
         |    }
         |}
       """.stripMargin
    val expr = parser printGeneration (_.parseAnalysis(wrap, schema))
  }

}
