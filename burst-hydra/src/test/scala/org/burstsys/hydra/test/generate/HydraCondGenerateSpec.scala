/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.generate

import org.burstsys.felt.model.FeltException
import org.burstsys.hydra.test.support.HydraSpecSupport

//@it
class HydraCondGenerateSpec extends HydraSpecSupport {

  ignore should "generate conditional 1a" in {
    implicit val source: String =
      s"""
         |if("foo" == 45) {
         |  var i:integer = 0
         |  i = i + 1
         |  ???
         |}
       """.stripMargin
    val caught = intercept[FeltException] {
      parser printGeneration (_.parseAnalysis(wrap, schema))
    }
    caught.message should equal("operation [ string '==' byte ] not supported")
  }

  it should "generate conditional 1b" in {
    implicit val source: String =
      s"""
         |if(true) {
         | var i:integer = 0
         |  i = i + 1
         |  ???
         |}
       """.stripMargin
    val foo = parser printGeneration (_.parseAnalysis(wrap, schema))
    foo
  }

  it should "generate conditional 1c" in {
    implicit val source: String =
      s"""
         |if(true) {
         | var i:integer = 0
         |  i = i + 1
         |  ???
         |} else {
         | var i:integer = 0
         |  ???
         |}
       """.stripMargin
    val foo = parser printGeneration (_.parseAnalysis(wrap, schema))
    foo
  }

  it should "generate conditional 1d" in {
    implicit val source: String =
      s"""
         |if(true) {
         |  var i:integer = 0
         |  ???
         |} else if ("foo" == "foo") {
         |  var j:integer = 0
         |  ???
         |} else if (45==4) {
         |  var k:integer = 0
         |  ???
         |} else {
         |  var p:integer = 0
         |  ???
         |}
       """.stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }

  it should "generate conditional 3" in {
    implicit val source: String =
      s"""
         |if(true) {
         | var i:integer = 0
         |  ???
         |} else if(size(user.sessions) > 4) {
         |  contains(user.sessions.parameters, "foo")
         |} else if (45==4) {
         |  ???
         |} else {
         |  ???
         |}
       """.stripMargin
    parser printGeneration (_.parseAnalysis(wrap, schema))
  }


}
