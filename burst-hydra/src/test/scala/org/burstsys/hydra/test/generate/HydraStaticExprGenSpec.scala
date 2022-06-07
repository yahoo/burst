/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.generate

import org.burstsys.brio.types.BrioTypes
import org.burstsys.hydra.test.support.HydraSpecSupport

//@Ignore
class HydraStaticExprGenSpec extends HydraSpecSupport {

/*
  it should "base expression 1" in {
    implicit val source: String =
      s"""
         |   3 + 5 * ( 1 / 4)
       """.stripMargin
    val expression = parser printGeneration (_.parseAnalysisDeclaration(source, schema))
    expression.feltType.valueType should equal(BrioTypes.BrioByteKey)

    val value = 3 + 5 * (1 / 4)

    expression.reduceToFixAtomOrThrow.value should equal(value)

  }

  it should "base expression 2" in {
    implicit val source: String =
      s"""
         |   (3 + 5 * 1) / 4
       """.stripMargin
    val expression = parser printGeneration (_.parseExpression(source, schema))
    expression.feltType.valueType should equal(BrioTypes.BrioByteKey)

    val value = (3 + 5 * 1) / 4

    expression.reduceToFixAtomOrThrow.value should equal(value)

  }

  it should "base expression 3" in {
    implicit val source: String =
      s"""
         |   (2 * 5) / (3 - 1)
       """.stripMargin
    val expression = parser printGeneration (_.parseExpression(source, schema))
    expression.feltType.valueType should equal(BrioTypes.BrioByteKey)

    val value = (2 * 5) / (3 - 1)

    expression.reduceToFixAtomOrThrow.value should equal(value)

  }

  it should "base expression 4" in {
    implicit val source: String =
      s"""
         |   2 + 5 * 4
       """.stripMargin
    val expression = parser printGeneration (_.parseExpression(source, schema))
    expression.feltType.valueType should equal(BrioTypes.BrioByteKey)

    val value = 2 + 5 * 4

    expression.reduceToFixAtomOrThrow.value should equal(value)

  }
*/

}
