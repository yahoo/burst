/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.bugs

import org.burstsys.alloy.views.AlloySmallDatasets.smallDataset_two_user_three_session
import org.burstsys.hydra.test.support.HydraAlloyTestRunner
import org.scalatest.Ignore

import scala.language.postfixOps

@Ignore
final
class HydraBugSpec09 extends HydraAlloyTestRunner {

  it should "bug 09" in {
    val source =
      s"""|
          |hydra myAnalysis() {
          |   schema 'unity'   {
          |      user <- ext_0
          |   }
          |   frame myCube {
          |      cube user {
          |         limit = 100
          |         aggregates {
          |            'num':sum[long]
          |         }
          |         dimensions {
          |            'id':verbatim[long]
          |         }
          |      }
          |      myRoute.paths.steps user.ext_0 => {
          |         situ => {
          |            myCube.'id' = routeVisitStepTag(myRoute)
          |            myCube.'num' = 1
          |         }
          |      }
          |   }
          |   frame myRoute {
          |      var T2:boolean=false
          |      var T1:boolean=false
          |      route  {
          |         maxPartialPaths = 1
          |         maxSteps = 100
          |         graph {
          |            enter,complete 1 { }
          |         }
          |      }
          |      user.sessions => {
          |         var route_test_control:boolean=false
          |         pre => {
          |            route_test_control=false
          |            routeScopeStart(myRoute)
          |            T2 = (true)
          |            if (!route_test_control) {
          |               if (T2) {
          |                  routeFsmCompletePath(myRoute)
          |                  routeScopeCommit(myRoute)
          |                  routeScopeStart(myRoute)
          |               }
          |            }
          |            routeScopeCommit(myRoute)
          |         }
          |      }
          |      user.sessions.events => {
          |         var route_test_control:boolean=false
          |         pre => {
          |            route_test_control=false
          |            routeScopeStart(myRoute)
          |            T1 = (user.sessions.events.id in (2,1))
          |            if (T1) {
          |               route_test_control = route_test_control || (routeFsmStepAssert(myRoute, 1, 2, user.sessions.events.startTime))
          |            }
          |            if (!route_test_control) {
          |               if (T1) {
          |                  routeFsmCompletePath(myRoute)
          |                  routeScopeCommit(myRoute)
          |                  routeScopeStart(myRoute)
          |               }
          |               if ((!route_test_control) && T1) {
          |                  route_test_control = route_test_control || (routeFsmStepAssert(myRoute, 1, 2, user.sessions.events.startTime))
          |               }
          |            }
          |            routeScopeCommit(myRoute)
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
            case "myCube" =>
              val found = result.rowSet.map {
                row =>
                  (
                    row[Long]("num"),
                    row[Long]("id")
                  )
              } sortBy (_._2) sortBy (_._1)
              val expected =
                Array((2,2))
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // Some(new BC0BC552D3EDC4238A812660C63CF1AF0)
    )


  }

}
