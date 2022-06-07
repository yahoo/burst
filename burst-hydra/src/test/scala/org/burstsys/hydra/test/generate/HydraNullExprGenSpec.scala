/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.generate

import org.burstsys.felt.model.FeltException
import org.burstsys.hydra.test.support.HydraSpecSupport

//@Ignore
class HydraNullExprGenSpec extends HydraSpecSupport {

/*
  it should "base test nullity 1a" in {
    implicit val source: String =
      s"""
         |  true || null // should be true
       """.stripMargin
    val expression = parser printGeneration (_.parseAnalysisDeclaration(source, schema))
    expression.reduceToBoolAtom match {
      case None=>
        fail
      case Some(l) =>
        l.value should equal(true)
    }
  }

  it should "base test nullity 1b" in {
    implicit val source: String =
      s"""
         |  false || null // should be null
       """.stripMargin
    val expression = parser printGeneration (_.parseAnalysisDeclaration(source, schema))
    assert(expression.reduceToNull.isDefined)
  }

  it should "base test nullity 2a" in {
    implicit val source: String =
      s"""
         |  null || true // should be true
       """.stripMargin
    val expression = parser printGeneration (_.parseAnalysisDeclaration(source, schema))
    expression.reduceToBoolAtom match {
      case None=> fail
      case Some(l) => l.value should equal(true)
    }
  }

  it should "base test nullity 2b" in {
    implicit val source: String =
      s"""
         |  null || false // should be null
       """.stripMargin
    val expression = parser printGeneration (_.parseExpression(source, schema))
    assert(expression.reduceToNull.isDefined)
  }

  it should "base test nullity 3" in {
    implicit val source: String =
      s"""
         |  null + 4
       """.stripMargin
    val expression = parser printGeneration (_.parseExpression(source, schema))
    assert(expression.reduceToNull.isDefined)
  }

  it should "base test nullity 4" in {
    implicit val source: String =
      s"""
         |  4 + null
       """.stripMargin
    val expression = parser printGeneration (_.parseExpression(source, schema))
    assert(expression.reduceToNull.isDefined)
  }

  it should "base test nullity 5" in {
    implicit val source: String =
      s"""
         | (5 + 4) >= (4.5 / null)
         |""".stripMargin
    val expression = parser printGeneration (_.parseExpression(source, schema))
    assert(expression.reduceToNull.isDefined)
  }

  it should "base test nullity 6" in {
    implicit val source: String =
      s"""
         | ((null >= 4) || (3000 == null)) && (34 > null)
         |""".stripMargin
    val expression = parser printGeneration (_.parseExpression(source, schema))
    assert(expression.reduceToNull.isDefined)
  }

  it should "base test nullity 7" in {
    implicit val source: String =
      s"""
         |if(user.sessions.id != null) {
         |  ???
         |} """.stripMargin
    val expression = parser printGeneration (_.parseExpression(source, schema))
    expression.reduceToLiteral.isEmpty should be(true)
    expression.reduceToNull.isEmpty should be(true)
  }

  it should "base test nullity 8" in {
    implicit val source: String =
      s"""
         |if(true) {
         |  ???
         |} else if(user.sessions.id != null) {
         | ???
         |}""".stripMargin
    val expression = parser printGeneration (_.parseExpression(source, schema))
    expression.reduceToLiteral.isEmpty should be(true)
    expression.reduceToNull.isEmpty should be(true)
  }
*/


}
