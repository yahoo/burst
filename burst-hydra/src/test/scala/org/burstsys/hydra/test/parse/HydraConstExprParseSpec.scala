/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.parse

import org.burstsys.felt.model.literals.primitive.FeltPrimitive
import org.burstsys.hydra.test.support.HydraSpecSupport
import org.scalatest.Ignore

@Ignore
class HydraConstExprParseSpec extends HydraSpecSupport {

/*
  it should "parse value expression 3" in {
    implicit val source: String =
      s"""
          (4/5)
       """.stripMargin

    val expr = parser printParse (_.parseAnalysisDeclaration(source, schema))
    expr.reduceToLiteral match {
      case Some(c) => c.asInstanceOf[FeltAtom].value should equal(4 / 5)
      case _ => ???
    }
  }

  it should "parse value expression 4" in {
    implicit val source: String =
      s"""
          (5/4)
       """.stripMargin

    val expr = parser printParse (_.parseAnalysisDeclaration(source, schema))
    expr.reduceToLiteral.nonEmpty should equal(true)
    expr.reduceToLiteral match {
      case Some(c) => c.asInstanceOf[FeltAtom].value should equal(5 / 4)
      case _ => ???
    }
  }
  it should "parse value expression 5" in {
    implicit val source: String =
      s"""
          (20%7)
       """.stripMargin

    val expr = parser printParse (_.parseExpression(source, schema))
    expr.reduceToLiteral.nonEmpty should equal(true)
    expr.reduceToLiteral match {
      case Some(c) => c.asInstanceOf[FeltAtom].value should equal(20 % 7)
      case _ => ???
    }
  }

  it should "parse value expression 6" in {
    implicit val source: String =
      s"""
          ((1 * 2) + (4/5) % 4.0)
       """.stripMargin

    val expr = parser printParse (_.parseExpression(source, schema))
    expr.reduceToLiteral.nonEmpty should equal(true)
    expr.reduceToLiteral match {
      case Some(c) => c.asInstanceOf[FeltAtom].value should equal((1 * 2) + (4 / 5) % 4.0)
      case _ => ???
    }
  }

  it should "parse value expression 7" in {
    implicit val source: String =
      s"""
          ((1.0 * 2.0) + (4.0/5.0) % 4.0)
       """.stripMargin

    val expr = parser printParse (_.parseExpression(source, schema))
    expr.reduceToLiteral.nonEmpty should equal(true)
    expr.reduceToLiteral match {
      case Some(c) => c.asInstanceOf[FeltAtom].value should equal((1.0 * 2.0) + (4.0 / 5.0) % 4.0)
      case _ => ???
    }
  }

*/

}
