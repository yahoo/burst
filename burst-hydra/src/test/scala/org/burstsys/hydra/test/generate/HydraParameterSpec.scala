/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.generate

import org.burstsys.felt.model.FeltException
import org.burstsys.hydra.test.support.HydraSpecSupport

/**
  * ← ⇒
  */
//@Ignore
class HydraParameterSpec extends HydraSpecSupport {

  it should "generate parameters 1" in {
    implicit val source: String =
      s"""
         |hydra $analysisName (p1:integer = 0) {
         |  schema unity
         |  frame $frameName {
         |
         |    val gv3:long = 0
         |
         |    cube user {
         |      limit = 300
         |      aggregates {
         |        a1:sum[integer]
         |      } // end aggregate
         |    } // end cube
         |
         |    user ⇒ {
         |
         |      pre ⇒ {
         |        var lv4:long = p1
         |        lv4 = p1
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

  it should "generate parameters 2" in {
    implicit val source: String =
      s"""
         |hydra $analysisName (p1:long = 0) {
         |  schema unity
         |  frame $frameName {
         |    val gv3:long = 0
         |    cube user {
         |      limit = 300
         |      aggregates {
         |        a1:sum[integer]
         |      } // end aggregate
         |    } // end cube
         |    user ⇒ {
         |      pre ⇒ {
         |        val lv4:long = p1
         |        p1 = 34
         |      } // end pre
         |    } // end visit
         |  } // end frame
         |} // end analysis
         |""".stripMargin
    val caught = intercept[FeltException] {
      parser printGeneration (_.parseAnalysis(source, schema))
    }
    caught.message should equal("'p1 = 34' assignment not possible to immutable lhs")

  }


}
