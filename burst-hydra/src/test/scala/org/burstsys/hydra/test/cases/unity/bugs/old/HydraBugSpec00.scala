/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.bugs.old

import org.burstsys.alloy.views.unity.UnityUseCaseViews.over_200_200
import org.burstsys.hydra.test.support.HydraAlloyTestRunner
import org.scalatest.Ignore

import scala.language.postfixOps

@Ignore
final
class HydraBugSpec00 extends HydraAlloyTestRunner {

  //  doSerializeTraversal = false
  //  useStaticSweep = null

  it should "test for inappropriate cube names that shadow schema paths" in {
    val source =
      s"""|hydra eqlGenerated() {
          |   schema 'unity'
          |   frame query_1 {
          |      cube user {
          |         limit = 100
          |         aggregates {
          |            'events':sum[long]
          |            'sessions':sum[long]
          |            'user':sum[long]
          |         }
          |      }
          |      /* should cause error */ user => {
          |         post => {
          |            // BasicLane[1](RESULT)
          |            eqlGenerated.query_1.'user' = 1
          |         }
          |      }
          |      user.sessions => {
          |         post => {
          |            // BasicLane[1](RESULT)
          |            eqlGenerated.query_1.'sessions' = 1
          |         }
          |      }
          |      user.sessions.events => {
          |         post => {
          |            // BasicLane[1](RESULT)
          |            eqlGenerated.query_1.'events' = 1
          |         }
          |      }
          |   }
          |}""".stripMargin

    test(
      source, over_200_200, {
        (name, result) =>
      }
    )
  }

}
