/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.parse

import org.burstsys.hydra.test.support.HydraSpecSupport
import org.scalatest.Ignore

@Ignore
class HydraTabletParseSpec extends HydraSpecSupport {

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // Examples
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  it should "parse tablet 1" in {
    implicit val source: String =
      s"""
         tablet t1[string] : user
       """.stripMargin

    parser printParse (_.parseAnalysis(source, schema))
  }


}
