/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.parse

import org.burstsys.hydra.test.support.HydraSpecSupport

//@Ignore
class HydraVarParseSpec extends HydraSpecSupport {

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // Examples
  ////////////////////////////////////////////////////////////////////////////////////////////////////


  it should "parse variable 1" in {
    implicit val source: String =
      s"""
         | var foo:double = 2
         |""".stripMargin
    parser printParse (_.parseAnalysis(wrap, schema))
  }


}
