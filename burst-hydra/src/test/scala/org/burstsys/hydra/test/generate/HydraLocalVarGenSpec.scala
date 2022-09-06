/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.generate

import org.burstsys.hydra.test.support.HydraSpecSupport

//@Ignore
class HydraLocalVarGenSpec extends HydraSpecSupport {

  it should "generate variable 0" in {
    implicit val source: String =
      s"""
         |  val foo:string = "hello there"
         |""".stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }

  it should "generate variable 1" in {
    implicit val source: String =
      s"""
         |  val foo:double = 5
         |""".stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }

  it should "generate variable 2" in {
    implicit val source: String =
      s"""
         |  val foo:map[string, string] = map("foo" -> "bar")
         |""".stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }

  it should "generate variable 3" in {
    implicit val source: String =
      s"""
         |  val foo:set[double] = set(2.0, 2.4, 45, 6 * 7)
         |""".stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }

  it should "generate variable 4" in {
    implicit val source: String =
      s"""
         |  val foo:double = 5 * 4.5
         |""".stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }

  it should "generate variable 5" in {
    implicit val source: String =
      s"""
         |  val foo:boolean = true
         |""".stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }

  it should "generate variable 6" in {
    implicit val source: String =
      s"""
         |  val foo:boolean = true && false
         |""".stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }

  it should "generate variable 7" in {
    implicit val source: String =
      s"""
         |  val foo:boolean = ((true && false) && user.sessions.id)
         |""".stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }


  it should "generate variable 8" in {
    implicit val source: String =
      s"""
         |  val foo:integer = -(4 * 5)
         |""".stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }

  it should "generate variable 9" in {
    implicit val source: String =
      s"""
         |  val foo:boolean = (true && (3== 3)) && 45 >= 34
         |""".stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }

  it should "generate variable 10" in {
    implicit val source: String =
      s"""
         |  val foo:string = user.sessions.parameters["goody"]
         |""".stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }

  it should "generate variable 11" in {
    implicit val source: String =
      s"""
         |  val foo:string = user.sessions.parameters[4 * (5 + 23)]
         |""".stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }

  it should "generate variable 12" in {
    implicit val source: String =
      s"""
         |  val foo:string = user.sessions.parameters[4 * (5 + 23)]
         |""".stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }


}
