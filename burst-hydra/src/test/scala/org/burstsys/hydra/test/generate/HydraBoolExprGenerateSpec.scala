/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.generate

import org.burstsys.hydra.test.support.HydraSpecSupport

/**
  * ← ⇒
  */
//@it
class HydraBoolExprGenerateSpec extends HydraSpecSupport {


  it should "parse boolean expression 1" in {
    implicit val source: String =
      s"""
          true || false
       """.stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))

  }

  it should "parse boolean expression 2" in {
    implicit val source: String =
      s"""
          !((true || false) && (4 == 5))
       """.stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }

  it should "parse boolean expression 3" in {
    implicit val source: String =
      s"""
          !((true || false) && (4 == (user.sessions.id == 5)))
       """.stripMargin
    val e = parser printGeneration (_.parseAnalysis(wrap, schema))
    e
  }


  it should "parse boolean expression 4" in {
    implicit val source: String =
      s"""
          user.sessions.id == 5
       """.stripMargin
    val e = parser printGeneration (_.parseAnalysis(wrap, schema))
    e
  }

  it should "parse boolean expression 5" in {
    implicit val source: String =
      s"""
          !(user.sessions.id == 5)
       """.stripMargin
    val e = parser printGeneration (_.parseAnalysis(wrap, schema))
    e
  }


  it should "parse boolean expression 6" in {
    implicit val source: String =
      s"""
           4 != (user.sessions.id == 5)
       """.stripMargin
    val e = parser printGeneration (_.parseAnalysis(wrap, schema))
    e
  }

  it should "parse boolean expression 7" in {
    implicit val source: String =
      s"""
           (user.sessions.id == 5) != false
       """.stripMargin
    val e = parser printGeneration (_.parseAnalysis(wrap, schema))
    e
  }


}
