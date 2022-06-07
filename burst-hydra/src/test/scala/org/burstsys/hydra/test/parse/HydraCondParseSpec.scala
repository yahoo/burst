/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.parse

import org.burstsys.hydra.test.support.HydraSpecSupport
import org.scalatest.Ignore

/**
  * ← ⇒
  */
//@Ignore
class HydraCondParseSpec extends HydraSpecSupport {


  it should "parse if 1" in {

    implicit val source: String =
      s"""
         |var v1:integer = 0
         |var v2:string = 1
         |var v3:integer = 2
         |var v4:integer = 3
         |if( true && false ) {
         |  v1 = 8
         |} else if(5 == 1) {
         |  v2 = "hello"
         |} else {
         |  v4 = null
         |}
       """.stripMargin


    val expr = parser printParse (_.parseAnalysis(wrap, schema))
  }

  it should "parse if 2" in {

    implicit val source: String =
      s"""
         |var v1:integer = 0
         |var v2:integer = 1
         |var v3:integer = 2
         |var v4:integer = 3
         |if( true && false ) {
         |  v1 = 8
         |}
       """.stripMargin


    val expr = parser printParse (_.parseAnalysis(wrap, schema))
  }

  it should "parse if 3" in {

    implicit val source: String =
      s"""
         |var v1:integer = 0
         |var v2:integer = 1
         |var v3:integer = 2
         |var v4:integer = 3
         |if( true && false ) {
         |  v1 = 8
         |} else {
         |  v4 = null
         |}
       """.stripMargin

    val expr = parser printParse (_.parseAnalysis(wrap, schema))
  }


  it should "parse if 4" in {

    implicit val source: String =
      s"""
         |var v1:integer = 0
         |var v2:string = 1
         |var v3:string = 2
         |var v4:integer = 3
         |if( true && false ) {
         |  v1 = 8
         |} else if(5 == 1) {
         |  v2 = "hello"
         |} else if(true || (false && true)) {
         |  v3 = "goodbye"
         |} else {
         |  v4 = null
         |}
       """.stripMargin

    val expr = parser printParse (_.parseAnalysis(wrap, schema))
  }

}
