/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.bugs.old

import org.burstsys.alloy.views.AlloySmallDatasets.smallDataset_one_user_one_session
import org.burstsys.hydra.test.support.HydraAlloyTestRunner
import org.scalatest.Ignore

import scala.language.postfixOps

@Ignore
final
class HydraUnityBug09Spec extends HydraAlloyTestRunner {

  it should "successfully define an extended path sub cube" in {
    val source =
      s"""
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
         |      var T4_summary:boolean=false
         |      var T5:boolean=false
         |      var T3_summary:boolean=false
         |      var T4:boolean=false
         |      var T3:boolean=false
         |      route  {
         |         maxPartialPaths = 1
         |         maxSteps = 100
         |         graph {
         |            enter,complete 1 {
         |            }
         |         }
         |      }
         |      measurement.paths.steps user.ext_0 => {
         |         situ => {
         |            T4 = T3 && ((dayGrain(routeVisitStepTime(measurement)) - dayGrain(routeLastStepTime(cohort))) between (-2678400000,2678400000)) && (routeVisitStepIsLast(measurement))
         |            if (T4) {
         |               T4_summary = true
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
         |            T5 = (true)
         |            if (T5) {
         |               if (!route_cohort_control) {
         |                  route_cohort_control=routeFsmStepAssert(cohort, 1, 1, user.application.firstUse.sessionTime)
         |               }
         |            }
         |            // BasicLane[1](RESULT)
         |            T3 = (routeLastPathIsComplete(cohort))
         |            T4_summary=false
         |            if (T3) {
         |               if (!route_cohort_control) {
         |                  if (T5) {
         |                     routeFsmCompletePath(cohort)
         |                     routeScopeCommit(cohort)
         |                     routeScopeStart(cohort)
         |                  }
         |                  if (T5) {
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
         |            if (T4_summary && T3) {
         |               T3_summary = true
         |            }
         |         }
         |      }
         |   }
         |   frame measurement {
         |      var T6:boolean=false
         |      var T8:boolean=false
         |      var T6_summary:boolean=false
         |      var T7:boolean=false
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
         |            T6 = (routeLastPathIsComplete(cohort))
         |            if (T6) {
         |            } else {
         |               abortRelation(user)
         |            }
         |         }
         |         post => {
         |            // BasicLane[1](RESULT)
         |            if (T6) {
         |               T6_summary = true
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
         |            T8 = (true)
         |            if (T8) {
         |               if (!route_measurement_control) {
         |                  route_measurement_control=routeFsmStepAssert(measurement, 1, 2, user.sessions.id)
         |               }
         |            }
         |            // BasicLane[1](RESULT)
         |            T7 = T6
         |            if (T7) {
         |               if (!route_measurement_control) {
         |                  if (T8) {
         |                     routeFsmCompletePath(measurement)
         |                     routeScopeCommit(measurement)
         |                     routeScopeStart(measurement)
         |                  }
         |                  if (T8) {
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
         |}  """.stripMargin

    test(
      hydra = source,
      ds = smallDataset_one_user_one_session,
      validate = {
        (name, result) =>
          result.resultName match {
            case "query_1" =>
              val found = result.rowSet.map {
                row =>
                  (
                    row[Long]("users"),
                    row[Long]("measure")
                  )
              }   sortBy (_._2) sortBy (_._1)
              val expected =
                Array(
                )
            case _ =>
          }
      },
      staticSweep = None // Some(new B77698C6CC9B34879BDC763E131281609)
    )
  }

}
