/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.parse

import org.burstsys.hydra.test.support.HydraSpecSupport
import org.scalatest.Ignore

/**
  * ← ⇒
  */
@Ignore
class HydraMethodParseSpec extends HydraSpecSupport {


  it should "parse method 1" in {
    implicit val source: String =
      s"""
         |def method1():boolean = {
         |  ???
         |  true
         |}
       """.stripMargin

    val expr = parser printParse (_.parseAnalysis(wrap, schema))
  }

  it should "parse method 2" in {
    implicit val source: String =
      s"""
         |def method2():array[double] = {
         |  ???
         |  return true
         |}
       """.stripMargin

    val expr = parser printParse (_.parseAnalysis(wrap, schema))
  }

}
