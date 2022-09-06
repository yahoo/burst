/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.parse

import org.burstsys.hydra.test.support.HydraSpecSupport
import org.scalatest.Ignore

@Ignore
class HydraRouteParseSpec extends HydraSpecSupport {

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // Examples
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  it should "parse route 1" in {
    implicit val source: String =
      s"""
         |route  {
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
    val expr = parser printParse (_.parseAnalysis(source, schema))
  }


  it should "parse route 2" in {
    implicit val source: String =
      s"""
         |route   {
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
    val expr = parser printParse (_.parseAnalysis(source, schema))
  }


  it should "parse route functions" in {
    implicit val source: String =
      s"""
         |user => {
         |  pre => {
         |    routeFsmStepAssert()
         |    routeFsmAssertTime()
         |    routeFsmEndPath()
         |    fullRoutePaths()
         |    routeFsmInPath()
         |    routeFsmInStep()
         |    routeFsmIsEmpty()
         |    routeScopeAbort()
         |    routeScopeCommit()
         |    routeScopeCurrentPath()
         |    routeScopeCurrentStep()
         |    routeScopePathChanged()
         |    routeScopePriorPath()
         |    routeScopePriorStep()
         |    routeScopeStart()
         |    routeScopeStepChanged()
         |    routeVisitPathOrdinal()
         |    routeVisitStepKey()
         |    routeVisitStepTime()
         |  }
         |}
       """.stripMargin

    parser printParse (_.parseAnalysis(source, schema))
  }

}
