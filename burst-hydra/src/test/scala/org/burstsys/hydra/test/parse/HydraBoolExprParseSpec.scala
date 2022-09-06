/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.parse


import org.burstsys.hydra.test.support.HydraSpecSupport

//@Ignore
class HydraBoolExprParseSpec extends HydraSpecSupport {

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // Examples
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  it should "parse boolean expression 1" in {
    implicit val source: String =
      s"""
          true || false
       """.stripMargin
    parser printParse (_.parseAnalysis(wrap, schema))
  }

  it should "parse boolean expression 2" in {
    implicit val source: String =
      s"""
          !((true || false) && (4 == 5))
       """.stripMargin
    parser printParse (_.parseAnalysis(wrap, schema))

  }

  it should "parse boolean expression 3" in {
    implicit val source: String =
      s"""
          user.sessions.id == 4
       """.stripMargin
    parser printParse (_.parseAnalysis(wrap, schema))

  }

  it should "parse boolean expression 4" in {
    implicit val source: String =
      s"""
          if(user.sessions.id == 4) {
            true
          } else {
            false
          }
       """.stripMargin
    parser printParse (_.parseAnalysis(wrap, schema))

  }

}
