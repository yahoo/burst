/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.generate

import org.burstsys.hydra.test.support.HydraSpecSupport

//@Ignore
class HydraMatchGenerateSpec extends HydraSpecSupport {

  ignore should "generate match 1a" in {
    implicit val source: String =
      s"""
         |user.sessions.startTime match {
         |  case "hello" => {
         |    var i:integer = i + 1
         |    ???
         |  }
         |}
       """.stripMargin
    parser printGeneration (_.parseAnalysis(source, schema))
  }

  ignore should "generate match 1b" in {
    implicit val source: String =
      s"""
         |user.sessions.startTime match {
         |  case "hello" => {
         |    var i:integer = i + 1
         |    ???
         |  }
         |  case 4 + 5 => {
         |    var money:double = 2.00
         |    ???
         |  }
         |}
       """.stripMargin
    parser printGeneration (_.parseAnalysis(source, schema))
  }

  ignore should "generate match 1c" in {
    implicit val source: String =
      s"""
         |user.sessions.startTime match {
         |  case "hello" => {
         |    var i:integer = i + 1
         |    ???
         |  }
         |  case 4 + 5 => {
         |    var money:double = 2.00
         |    ???
         |  }
         |  case _ => {
         |    var money:double = 2.00
         |      ???
         |  }
         |}
       """.stripMargin
    parser printGeneration (_.parseAnalysis(source, schema))
  }

  ignore should "generate match 1d" in {
    implicit val source: String =
      s"""
         |user.sessions.startTime match {
         |  case "hello" => {
         |    var i:integer = i + 1
         |    ???
         |  }
         |  case _ => {
         |    var money:double = 2.00
         |      ???
         |  }
         |}
       """.stripMargin
    parser printGeneration (_.parseAnalysis(source, schema))
  }

  ignore should "generate match 2" in {
    implicit val source: String =
      s"""
         |user.sessions.startTime match {
         |  case "hello" => {
         |    var i:integer = i + 1
         |  }
         |  case 4 + 5 => {
         |    var money:double = 2.00
         |  }
         |}
       """.stripMargin
    parser printGeneration (_.parseAnalysis(source, schema))
  }

  ignore should "generate match 3" in {
    implicit val source: String =
      s"""
         |user.sessions.startTime match {
         |  case "hello" => {
         |    var i:integer = i + 1
         |  }
         |}
       """.stripMargin
    parser printGeneration (_.parseAnalysis(source, schema))
  }

}
