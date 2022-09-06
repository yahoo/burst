/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.parse

import org.burstsys.felt.model.FeltException
import org.burstsys.hydra.test.support.HydraSpecSupport

//@Ignore
class HydraExprParseSpec extends HydraSpecSupport {

/*
  it should "parse expression 1" in {
    implicit val source: String =
      s"""|{
          |          (user.sessions + 22) * 2.0
          |}""".stripMargin

    val expr = parser parseExpressionBlock(source, schema)
    expr.reduceToLiteral.nonEmpty should equal(false)
  }

  it should "parse expression 2" in {
    implicit val source: String =
      s"""|{
          |  user.id + 3 // variable/parameter reference
          |}""".stripMargin

    val expr = parser parseExpressionBlock(source, schema)
    expr.reduceToLiteral.nonEmpty should equal(false)
  }

  it should "not parse expression with var declaration not at beginning" in {
    implicit val source: String =
      s"""
         |{
         |    user.id + 3
         |    var i:integer = 1
         |}""".stripMargin

    val caught = intercept[FeltException] {
      parser parseExpressionBlock(source, schema)
    }
  }

  it should "parse expression with var declaration at beginning" in {
    implicit val source: String =
      s"""
         |{
         |    var i:integer = 1
         |}""".stripMargin

    parser parseExpressionBlock(source, schema)
  }

  ignore should "parse expression with plus_eq update" in {
    implicit val source: String =
      s"""
         |{
         |    var i:integer = 1
         |    i += 1
         |}""".stripMargin

    parser parseExpressionBlock(source, schema)
  }

  ignore should "parse expression with minus_eq update" in {
    implicit val source: String =
      s"""
         |{
         |    var i:integer = 1
         |    i -= 1
         |}""".stripMargin

    parser parseExpressionBlock(source, schema)
  }

*/

}
