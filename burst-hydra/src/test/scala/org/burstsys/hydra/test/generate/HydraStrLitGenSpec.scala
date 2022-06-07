/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.generate

import org.burstsys.hydra.test.support.HydraSpecSupport

//@Ignore
class HydraStrLitGenSpec extends HydraSpecSupport {

  it should "generate string literal 1" in {
    implicit val source: String =
      s"""|
          |   val lv1:string = "foo"
          |
       """.stripMargin
    val e = parser printGeneration (_.parseAnalysis(wrap, schema))
    e
  }


}
