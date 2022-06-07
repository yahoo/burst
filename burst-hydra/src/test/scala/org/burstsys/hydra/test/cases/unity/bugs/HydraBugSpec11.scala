/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.bugs

import org.burstsys.alloy.views.AlloySmallDatasets.smallDataset_two_user_three_session
import org.burstsys.hydra.test.support.HydraAlloyTestRunner
import org.scalatest.Ignore

import scala.language.postfixOps

@Ignore
final
class HydraBugSpec11 extends HydraAlloyTestRunner {

  it should "bug 11" in {
    val source =
      s"""|
          |hydra eqlGenerated() {
          |   schema 'unity'
          |   {
          |      user <- ext_0
          |   }
          |   frame query_1 {
          |      var T1_summary:boolean=false
          |      var T2:boolean=false
          |      var T1:boolean=false
          |      var T2_summary:boolean=false
          |      cube user {
          |         limit = 100
          |         aggregates {
          |            'users':unique[long]
          |         }
          |         dimensions {
          |            'measure':verbatim[long]
          |         }
          |      }
          |      measurement.paths.steps user.ext_0 => {
          |         situ => {
          |            T2 = T1 && ((dayGrain(routeVisitStepTime(measurement)) - dayGrain(routeLastStepTime(cohort))) between (-2678400000,2678400000)) && (routeVisitStepIsLast(measurement))
          |            if (T2) {
          |               T2_summary = true
          |               eqlGenerated.query_1.'measure' = (dayGrain(routeVisitStepTime(measurement)) - dayGrain(routeLastStepTime(cohort)))
          |               eqlGenerated.query_1.'users' = 1
          |            }
          |         }
          |      }
          |      user => {
          |         pre => {
          |            // BasicLane[1](RESULT)
          |            T1 = (routeLastPathIsComplete(cohort))
          |            T2_summary=false
          |            if (T1) {
          |            } else {
          |               abortRelation(user)
          |            }
          |         }
          |         post => {
          |            // BasicLane[1](RESULT)
          |            if (T2_summary && T1) {
          |               T1_summary = true
          |            }
          |         }
          |      }
          |   }
          |   frame cohort {
          |      var T3:boolean=false
          |      var T3_summary:boolean=false
          |      var T4:boolean=false
          |      route  {
          |         maxPartialPaths = 1
          |         maxSteps = 100
          |         graph {
          |            enter,complete 1 {
          |            }
          |         }
          |      }
          |      user => {
          |         var route_cohort_control:boolean=false
          |         pre => {
          |            // Lane[9223372036854775807](INIT-RESULT)
          |            route_cohort_control=false
          |            routeScopeStart(cohort)
          |            // BasicLane[3](funnel(cohort)-1)
          |            T4 = (true)
          |            if (T4) {
          |               if (!route_cohort_control) {
          |                  route_cohort_control=routeFsmStepAssert(cohort, 1, 1, user.application.firstUse.sessionTime)
          |               }
          |            }
          |            // BasicLane[1](RESULT)
          |            T3 = (routeLastPathIsComplete(cohort))
          |            if (T3) {
          |               if (!route_cohort_control) {
          |                  if (T4) {
          |                     routeFsmCompletePath(cohort)
          |                     routeScopeCommit(cohort)
          |                     routeScopeStart(cohort)
          |                  }
          |                  if (T4) {
          |                     if (!route_cohort_control) {
          |                        route_cohort_control=routeFsmStepAssert(cohort, 1, 1, user.application.firstUse.sessionTime)
          |                     }
          |                  }
          |               }
          |               routeScopeCommit(cohort)
          |            }
          |         }
          |         post => {
          |            // BasicLane[1](RESULT)
          |            if (T3) {
          |               T3_summary = true
          |            }
          |         }
          |      }
          |   }
          |   frame measurement {
          |      var T6:boolean=false
          |      var T5:boolean=false
          |      var T7:boolean=false
          |      var T5_summary:boolean=false
          |      route  {
          |         maxPartialPaths = 100
          |         maxSteps = 100
          |         graph {
          |            enter,complete 1 {
          |            }
          |         }
          |      }
          |      user => {
          |         pre => {
          |            // BasicLane[1](RESULT)
          |            T5 = (routeLastPathIsComplete(cohort))
          |            if (T5) {
          |            } else {
          |               abortRelation(user)
          |            }
          |         }
          |         post => {
          |            // BasicLane[1](RESULT)
          |            if (T5) {
          |               T5_summary = true
          |            }
          |         }
          |      }
          |      user.sessions => {
          |         var route_measurement_control:boolean=false
          |         pre => {
          |            // Lane[9223372036854775807](INIT-RESULT)
          |            route_measurement_control=false
          |            routeScopeStart(measurement)
          |            // BasicLane[4](funnel(measurement)-2)
          |            T7 = (true)
          |            if (T7) {
          |               if (!route_measurement_control) {
          |                  route_measurement_control=routeFsmStepAssert(measurement, 1, 2, user.sessions.id)
          |               }
          |            }
          |            // BasicLane[1](RESULT)
          |            T6 = T5
          |            if (T6) {
          |               if (!route_measurement_control) {
          |                  if (T7) {
          |                     routeFsmCompletePath(measurement)
          |                     routeScopeCommit(measurement)
          |                     routeScopeStart(measurement)
          |                  }
          |                  if (T7) {
          |                     if (!route_measurement_control) {
          |                        route_measurement_control=routeFsmStepAssert(measurement, 1, 2, user.sessions.id)
          |                     }
          |                  }
          |               }
          |               routeScopeCommit(measurement)
          |            }
          |         }
          |      }
          |   }
          |}
          |""".stripMargin

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
