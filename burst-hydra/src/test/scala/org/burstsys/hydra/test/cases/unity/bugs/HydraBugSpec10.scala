/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.bugs

import org.burstsys.alloy.views.AlloySmallDatasets.smallDataset_two_user_three_session
import org.burstsys.hydra.test.support.HydraAlloyTestRunner
import org.scalatest.Ignore

import scala.language.postfixOps

@Ignore
final
class HydraBugSpec10 extends HydraAlloyTestRunner {

  it should "bug 10" in {
    val source =
      s"""|
          |hydra eqlGenerated() {
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
          |            'time':verbatim[long]
          |         }
          |      }
          |      test.paths.steps user.ext_0 => {
          |         situ => {
          |            eqlGenerated.query_test.'id' = routeVisitStepTag(test)
          |            eqlGenerated.query_test.'time' = routeVisitStepTime(test)
          |            eqlGenerated.query_test.'num' = 1
          |         }
          |      }
          |   }
          |   frame test {
          |      var T2:boolean=false
          |      var T1:boolean=false
          |      route  {
          |         maxPartialPaths = 100
          |         maxSteps = 100
          |         graph {
          |            enter,complete 1 {
          |            }
          |         }
          |      }
          |      user.sessions => {
          |         var route_test_control:boolean=false
          |         pre => {
          |            // Lane[9223372036854775807](INIT-RESULT)
          |            route_test_control=false
          |            routeScopeStart(test)
          |            // BasicLane[5](funnel(test)-1)
          |            T2 = (true)
          |            // BasicLane[1](RESULT)
          |            if (!route_test_control) {
          |               if (T2) {
          |                  routeFsmCompletePath(test)
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
          |            // BasicLane[6](funnel(test)-2)
          |            T1 = (user.sessions.events.id in (2,1))
          |            if (T1) {
          |               route_test_control = route_test_control || (routeFsmStepAssert(test, 1, 2, user.sessions.events.startTime))
          |            }
          |            // BasicLane[1](RESULT)
          |            if (!route_test_control) {
          |               if (T1) {
          |                  routeFsmCompletePath(test)
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
      ds = smallDataset_two_user_three_session,
      validate = {
        (name, result) =>
          result.resultName match {
            case "query_test" =>
              val found = result.rowSet.map {
                row =>
                  (
                    row[Long]("num"),
                    row[Long]("time"),
                    row[Long]("id")
                  )
              } sortBy (_._3) sortBy (_._2) sortBy (_._1)
              val expected =
                Array(
                  (2, 10, 2), (2, 20, 2)
                )
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // Some(new BCDF77DCAC2A44084A4F91422156FF558)
    )


  }

}
