/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.parse

import org.burstsys.hydra.test.support.HydraSpecSupport

/**
  * ← ⇒
  */
//@Ignore
class HydraStatScopeParseSpec extends HydraSpecSupport {

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // Examples
  ////////////////////////////////////////////////////////////////////////////////////////////////////


  it should "parse statement scope 1" in {
    implicit val source: String =
      s"""
         |if ( true ) { ??? }
         |
         |if ( false ) {  } else {  } //4
         |
         |if ( true ) {  } else if ( false ) {  } // 6
         |
         |if ( false ) {  } else if ( true)  {  } else {  } // 8
       """.stripMargin

    val expr = parser printParse (_.parseAnalysis(wrap, schema))
  }

  it should "parse statement scope 2" in {
    implicit val source: String =
      s"""
         | // empty is ok...
       """.stripMargin

    val expr = parser printParse (_.parseAnalysis(wrap, schema))
  }

  it should "parse statement scope 3" in {
    implicit val source: String =
      s"""
         |   if ( true ) { ??? }
       """.stripMargin
    val expr = parser printParse (_.parseAnalysis(wrap, schema))
  }

  it should "parse statement scope 4" in {
    implicit val source: String =
      s"""
         |   user.sessions.id  match {
         |      case "foo" ⇒ {
         |         // statements...
         |      }
         |
         |      case 1 ⇒ {
         |          if ( true ) { ??? }
         |      }
         |   }
       """.stripMargin
    val expr = parser printParse (_.parseAnalysis(wrap, schema))
  }


}
