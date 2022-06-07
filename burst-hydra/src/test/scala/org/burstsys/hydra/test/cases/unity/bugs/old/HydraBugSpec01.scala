/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.bugs.old

import org.burstsys.alloy.views.AlloySmallDatasets.smallDataset_2_users_5_sessions
import org.burstsys.hydra.test.support.HydraAlloyTestRunner

import scala.language.postfixOps

final
class HydraBugSpec01 extends HydraAlloyTestRunner {

  it should "parse" in {
    val source =
      s"""hydra eqlGenerated() {
         |   schema 'unity'
         |   {
         |      user <- ext_0
         |   }
         |   frame query_test {
         |      cube user {
         |         limit = 100
         |         aggregates {
         |            'num':sum[long]
         |         }
         |         dimensions {
         |            'id':verbatim[long]
         |         }
         |      }
         |      test.paths.steps user.ext_0 => {
         |         situ => {
         |            eqlGenerated.query_test.'id' = routeVisitStepTag(test)
         |            eqlGenerated.query_test.'num' = 1
         |         }
         |      }
         |   }
         |   frame test {
         |      var T2:boolean=false
         |      var T1:boolean=false
         |      route  {
         |         maxPartialPaths = 1
         |         maxSteps = 100
         |         graph {
         |            enter 1 {
         |               to (2, 0, 0)
         |            }
         |            exit 2 {
         |            }
         |         }
         |      }
         |      user.sessions => {
         |         var route_test_control:boolean=false
         |         pre => {
         |            // Lane[9223372036854775807](INIT-RESULT)
         |            route_test_control=false
         |            routeScopeStart(test)
         |            // BasicLane[3](funnel(test)-1)
         |            T2 = (true)
         |            // BasicLane[1](RESULT)
         |            if (!route_test_control) {
         |               if (T2) {
         |                  if (routeFsmInStep(test, 1)) {
         |                     routeFsmStepAssert(test, 2, 0, 0)
         |                  } else {
         |                     routeFsmEndPath(test)
         |                  }
         |                  routeScopeCommit(test)
         |                  routeScopeStart(test)
         |               }
         |            }
         |            routeScopeCommit(test)
         |         }
         |      }
         |      user.sessions.events => {
         |         var route_test_control:boolean=false
         |         pre => {
         |            // Lane[9223372036854775807](INIT-RESULT)
         |            route_test_control=false
         |            routeScopeStart(test)
         |            // BasicLane[4](funnel(test)-2)
         |            T1 = (user.sessions.events.id in (2,1))
         |            if (T1) {
         |               route_test_control = route_test_control || (routeFsmStepAssert(test, 1, 2, user.sessions.events.startTime))
         |            }
         |            // BasicLane[1](RESULT)
         |            if (!route_test_control) {
         |               if (T1) {
         |                  if (routeFsmInStep(test, 1)) {
         |                     routeFsmStepAssert(test, 2, 0, 0)
         |                  } else {
         |                     routeFsmEndPath(test)
         |                  }
         |                  routeScopeCommit(test)
         |                  routeScopeStart(test)
         |               }
         |               if ((!route_test_control) && T1) {
         |                  route_test_control = route_test_control || (routeFsmStepAssert(test, 1, 2, user.sessions.events.startTime))
         |               }
         |            }
         |            routeScopeCommit(test)
         |         }
         |      }
         |   }
         |}""".stripMargin

    test(
      hydra = source,
      ds = smallDataset_2_users_5_sessions,
      validate = {
        (name, result) =>
          result.resultName match {
            case "query_test" =>
              val found = result.rowSet.map {
                row => (row[Long]("num"), row[Long]("id"))
              } sortBy (_._2) sortBy (_._1)
              val expected =
                Array()
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // Some(new B53F39494BD91407FABA1F0D7057CD468)
    )
  }

}
