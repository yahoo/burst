/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.generate

import org.burstsys.hydra.test.support.HydraSpecSupport
import org.scalatest.Ignore

/**
  * ← ⇒
  */
@Ignore
class HydraRouteGenerateSpec extends HydraSpecSupport {

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // Examples
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  it should "generate route 1" in {
    implicit val source: String =
      s"""
         |route  user {
         |    graph {
         |        enter 1 {
         |          to(2)
         |          to(3, 0, 0)
         |        }
         |        exit 2 {
         |        }
         |        exit 3 {
         |        }
         |    }
         |}
       """.stripMargin
    parser parseAnalysis(source, schema)
  }


  it should "generate route 2" in {
    implicit val source: String =
      s"""
         |route  user {
         |    maxPartialPaths = 1
         |    maxSteps = 30
         |    graph {
         |        enter 1 {
         |          to(2)
         |          to(3, 0, 0)
         |        }
         |        exit 2 {
         |        }
         |        exit 3 {
         |        }
         |    }
         |}
       """.stripMargin
    parser parseAnalysis(source, schema)
  }


}
