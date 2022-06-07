/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.generate

import org.burstsys.hydra.test.support.HydraSpecSupport
import org.scalatest.Ignore

//@Ignore
class HydraDynExprGenSpec extends HydraSpecSupport {

  it should "dynamic expression 1" in {
    implicit val source: String =
      s"""
         |   user.sessions.id + 3 // variable/parameter reference
       """.stripMargin
    val e = parser printGeneration (_.parseAnalysis(wrap, schema))
    e
  }

  it should "dynamic expression 2" in {
    implicit val source: String =
      s"""
          user.sessions.id + 22 * (user.sessions.id / 34)
       """.stripMargin
    val foo = parser printGeneration (_.parseAnalysis(wrap, schema))
  }

  it should "dynamic expression 3" in {
    implicit val source: String =
      s"""
          user.sessions.id + 22 * 2.0 + 3
       """.stripMargin
    val foo = parser printGeneration (_.parseAnalysis(wrap, schema))
/*
    val foo_optimized = foo.reduceStatics
    // TODO we do not reduce 44 + 3 yet
    foo_optimized.normalizedSource should equal("user.sessions.id + 44.0 + 3")
*/
  }

  it should "dynamic expression 4" in {
    implicit val source: String =
      s"""
         |   user.sessions.id
       """.stripMargin
    val e = parser printGeneration (_.parseAnalysis(wrap, schema))
    e
  }


}
