/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.generate

import org.burstsys.hydra.test.support.HydraSpecSupport

//@Ignore
class HydraAssignSpec extends HydraSpecSupport {


  it should "generate assign expr 1" in {
    implicit val source: String =
      s"""
         |hydra $analysisName () {
         |  schema unity
         |  frame $frameName {
         |    val v3:long = 0
         |    cube user {
         |      limit = 300
         |      aggregates {
         |        a1:sum[long]
         |      } // end aggregate
         |    } // end cube
         |
         |    user.sessions => {
         |      pre => {
         |        $analysisName.$frameName.a1 = user.sessions.id
         |      } // end pre
         |
         |    } // end visit
         |
         |  } // end frame
         |
         |} // end analysis
         |""".stripMargin
    parser printGeneration (_.parseAnalysis(source, schema))
  }


}
