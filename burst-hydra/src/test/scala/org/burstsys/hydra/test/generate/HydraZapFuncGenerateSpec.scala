/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.generate

import org.burstsys.hydra.test.support.HydraSpecSupport
import org.scalatest.Ignore

@Ignore
class HydraZapFuncGenerateSpec extends HydraSpecSupport {

  ////////////////////////////////////////////////////////////////////
  // contains()
  ////////////////////////////////////////////////////////////////////

  it should "generate zap insert call 1" in {
    implicit val source: String =
      s"""
         |insert(foo)
       """.stripMargin
    parser printGeneration (_.parseAnalysis(source, schema))
  }

}
