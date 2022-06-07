/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.generate

import org.burstsys.hydra.test.support.HydraSpecSupport

/**
  * ← ⇒
  */
//@Ignore
class HydraExprBlockGenerateSpec extends HydraSpecSupport {


  it should "parse expression block 0" in {
    implicit val source: String =
      s"""
         |  val foo1:string = "hello there"
         |  val foo2:string = "hello there"
       """.stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))

  }

  it should "parse expression block 1" in {
    implicit val source: String =
      s"""
           val bar:double = (0.5 * 12.1) % 3

           user.sessions.id match {
             case "foo" ⇒ {
               val foo:string = "hello there"
               size(user.sessions)
             }
             case 5 ⇒ {
               ???
             }
             case _ ⇒ {
               ???
             }
           }
       """.stripMargin
    val foo = parser printGeneration (_.parseAnalysis(wrap, schema))
    val os = foo.reduceStatics.normalizedSource
    os
  }

  it should "parse expression block 2" in {
    implicit val source: String =
      s"""
           val foo:double = user.sessions.id match {
             case "foo" ⇒ {
               val foo:string = "hello there"
               size(user.sessions)
             }
             case 5 ⇒ {
               ???
             }
           }
       """.stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }


  it should "parse empty block" in {
    implicit val source: String =
      s"""
       """.stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))

  }

  it should "parse expression block with a single variable declaration" in {
    implicit val source: String =
      s"""
         |  val foo:integer = 33
       """.stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))

  }

  it should "parse expression block with a single expression" in {
    implicit val source: String =
      s"""
         |  user.sessions.id + 34
       """.stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }

  it should "parse expression block with a single variable declaration and a single expression" in {
    implicit val source: String =
      s"""
         |  val foo:integer = 33
         |  user.sessions.id + 34
       """.stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }


  it should "parse expression block with a dynamically init local variable" in {
    implicit val source: String =
      s"""
         |  val foo:long = user.sessions.id
       """.stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }

  it should "parse expression block with a statically init local variable" in {
    implicit val source: String =
      s"""
         |  val foo:long = 4
       """.stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }

  it should "parse expression block with quoted identifier" in {
    implicit val source: String =
      s"""
         |  val 'foo':long = 4
       """.stripMargin
   parser printGeneration (_.parseAnalysis(wrap, schema))
  }
}
