/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.bugs

import org.burstsys.alloy.views.unity.UnityUseCaseViews.over_200_200
import org.burstsys.hydra.test.support.HydraAlloyTestRunner
import org.scalatest.Ignore

import scala.language.postfixOps

@Ignore
final
class HydraBugSpec14 extends HydraAlloyTestRunner {

  // This will result type mismatch at compilation
  ignore should "successfully do two routes with tablet" in {
    val source =
      s"""|
          |hydra $AnalysisName() {
          |   schema 'unity'
          |   {
          |      user <- ext_1
          |      user <- ext_0
          |   }
          |   frame $CubeFrame {
          |      var T2:boolean=false
          |      var T1_summary:boolean=false
          |      var T1:boolean=false
          |      var T2_summary:boolean=false
          |      var T3_summary:boolean=false
          |      var T3:boolean=false
          |      cube user {
          |         limit = 131072
          |         dimensions {
          |            'timestamp':dayGrain[long]
          |         }
          |         cube user.ext_0 {
          |            aggregates {
          |               'count':unique[long]
          |            }
          |            dimensions {
          |               'timeSince':verbatim[long]
          |            }
          |         }
          |      }
          |      tf.paths.steps user.ext_0 => {
          |         situ => {
          |            T2 = T1 && (routeVisitStepTag(tf) == 1) && ((dayGrain(routeVisitStepTime(tf)) - dayGrain(routeLastStepTime(cf))) > 0) && ((dayGrain(routeVisitStepTime(tf)) - (dayGrain(routeLastStepTime(cf)) + 3600000)) < 2678400000)
          |            if (T2) {
          |               T2_summary = true
          |               $AnalysisName.$CubeFrame.'timeSince' = ((dayGrain(routeVisitStepTime(tf)) - (dayGrain(routeLastStepTime(cf)) + 3600000)) / 86400000)
          |               $AnalysisName.$CubeFrame.'count' = 1
          |            }
          |         }
          |      }
          |      user => {
          |         pre => {
          |            // BasicLane[3](RESULT)
          |            T1 = (user.application.firstUse.sessionTime >= dayGrain((now() - 2332800000)))
          |            T2_summary=false
          |            T3_summary=false
          |            if (T1) {
          |            } else {
          |               abortRelation(user)
          |            }
          |         }
          |         post => {
          |            // BasicLane[3](RESULT)
          |            T1 = (routeLastPathIsComplete(cf))
          |            if (T2_summary && T3_summary && T1) {
          |               T1_summary = true
          |               $AnalysisName.$CubeFrame.'timestamp' = routeLastStepTime(cf)
          |               insert($AnalysisName.$CubeFrame)
          |            }
          |         }
          |      }
          |      sgmnt.members user.ext_1 => {
          |         situ => {
          |            T3 = T1 && (tabletMemberValue(sgmnt) in (615684))
          |            if (T3) {
          |               T3_summary = true
          |            }
          |         }
          |      }
          |   }
          |   frame cf {
          |      var T7:boolean=false
          |      var T4_summary:boolean=false
          |      var T5:boolean=false
          |      var T4:boolean=false
          |      var T6:boolean=false
          |      var T7_summary:boolean=false
          |      route  {
          |         maxCompletePaths = 1
          |         maxSteps = 100000
          |         graph {
          |            enter,complete 1 {
          |            }
          |         }
          |      }
          |      user => {
          |         pre => {
          |            // BasicLane[3](RESULT)
          |            T4 = (user.application.firstUse.sessionTime >= dayGrain((now() - 2332800000)))
          |            if (T4) {
          |            } else {
          |               abortRelation(user)
          |            }
          |         }
          |         post => {
          |            // BasicLane[3](RESULT)
          |            if (T4) {
          |               T4_summary = true
          |            }
          |         }
          |      }
          |      user.sessions => {
          |         pre => {
          |            // BasicLane[5](funnel(cf)-1)
          |            T6 = (user.sessions.sessionType == 1)
          |            T7_summary=false
          |         }
          |      }
          |      user.sessions.events => {
          |         var route_cf_control:boolean=false
          |         var route_cf_control_processed:boolean=false
          |         pre => {
          |            // Lane[9223372036854775807](INIT-RESULT)
          |            route_cf_control=false
          |            route_cf_control_processed=false
          |            routeScopeStart(cf)
          |            // BasicLane[5](funnel(cf)-1)
          |            T7 = T6 && (user.sessions.events.id == 24123599)
          |            if (T7) {
          |               if (!route_cf_control) {
          |                  route_cf_control=routeFsmStepAssert(cf, 1, 1, user.sessions.events.startTime)
          |               }
          |            }
          |            // BasicLane[3](RESULT)
          |            T5 = T4
          |            if (T5) {
          |               route_cf_control_processed=true
          |               if (!route_cf_control) {
          |                  if (T7) {
          |                     if (!route_cf_control) {
          |                        route_cf_control=routeFsmStepAssert(cf, 1, 1, user.sessions.events.startTime)
          |                     }
          |                  }
          |               }
          |               routeScopeCommit(cf)
          |            }
          |            // BasicLane[2](CLEANUP)
          |            if ((!route_cf_control_processed) && route_cf_control) {
          |               routeScopeAbort(cf)
          |            } else {
          |               routeScopeCommit(cf)
          |            }
          |         }
          |         post => {
          |            // BasicLane[5](funnel(cf)-1)
          |            if (T7) {
          |               T7_summary = true
          |            }
          |         }
          |      }
          |   }
          |   frame tf {
          |      var T9:boolean=false
          |      var T10:boolean=false
          |      var T8:boolean=false
          |      var T8_summary:boolean=false
          |      route  {
          |         maxCompletePaths = 100000
          |         maxSteps = 100000
          |         graph {
          |            enter,complete 1 {
          |            }
          |         }
          |      }
          |      user => {
          |         pre => {
          |            // BasicLane[3](RESULT)
          |            T8 = (user.application.firstUse.sessionTime >= dayGrain((now() - 2332800000)))
          |            if (T8) {
          |            } else {
          |               abortRelation(user)
          |            }
          |         }
          |         post => {
          |            // BasicLane[3](RESULT)
          |            if (T8) {
          |               T8_summary = true
          |            }
          |         }
          |      }
          |      user.sessions => {
          |         var route_tf_control:boolean=false
          |         var route_tf_control_processed:boolean=false
          |         pre => {
          |            // Lane[9223372036854775807](INIT-RESULT)
          |            route_tf_control=false
          |            route_tf_control_processed=false
          |            routeScopeStart(tf)
          |            // BasicLane[6](funnel(tf)-1)
          |            T10 = (user.sessions.startTime > 0) && (user.sessions.sessionType == 1)
          |            if (T10) {
          |               if (!route_tf_control) {
          |                  route_tf_control=routeFsmStepAssert(tf, 1, 1, user.sessions.startTime)
          |               }
          |            }
          |            // BasicLane[3](RESULT)
          |            T9 = T8
          |            if (T9) {
          |               route_tf_control_processed=true
          |               if (!route_tf_control) {
          |                  if (T10) {
          |                     if (!route_tf_control) {
          |                        route_tf_control=routeFsmStepAssert(tf, 1, 1, user.sessions.startTime)
          |                     }
          |                  }
          |               }
          |               routeScopeCommit(tf)
          |            }
          |            // BasicLane[2](CLEANUP)
          |            if ((!route_tf_control_processed) && route_tf_control) {
          |               routeScopeAbort(tf)
          |            } else {
          |               routeScopeCommit(tf)
          |            }
          |         }
          |      }
          |   }
          |   frame sgmnt {
          |      var T11:boolean=false
          |      tablet[long]
          |      user => {
          |         pre => {
          |            // BasicLane[7](segment(sgmnt)-615684)
          |            T11 = (user.application.lastUse.osVersionId in (261,4433,11141,33174,39603,39842,40178,48573,51976,57193,58794,60306,63823,63964,64218,64362,64798,66232,77094,105767,117378,192420,204642,243173,243753,245061,249131,256525,259676,264392,264797,266624,270099,272976,268305,275155,278242,279437,280255,292986,294033,294386,297492,299970,302028,305234,306352,306722,306745,307134,307137,307465,308312,309875,311372,312338,313458,313490,281905,282708,282744,282825,284998,285159,286315,286742,286915,287322,288312,288360,288420,288428,291871,291947,287910,292352,292487,292489,292978,319245,319479,324079,324554,324579,316773,316770,317698,318192,318317,315718,314712,315002,315686,314319,314260,362825,362826,354723,351927,351926,349995,347318,342554,325425)) && (user.application.firstUse.sessionTime >= dayGrain((now() - 2332800000)))
          |         }
          |         post => {
          |            // BasicLane[7](segment(sgmnt)-615684)
          |            if (T11) {
          |               tabletMemberAdd(sgmnt, 615684)
          |            }
          |         }
          |      }
          |   }
          |}
          |""".stripMargin

    test(source, over_200_200, {
      (name, result) =>
        name match {
          case CubeFrame =>
            val found = result.rowSet.map {
              row => (row.cells(0) asLong, row.cells(1) asLong, row.cells(2) asLong)
            } sortBy (_._1) sortBy (_._2) sortBy (_._3)
            /*
            val expected = Array(
              (1,1,101,1111), (2,1,101,1111), (3,1,101,1111), (4,1,101,1111), (5,1,101,1111), (6,1,101,1111), (7,1,101,1111), (8,1,101,1111), (9,1,101,1111), (10,1,101,1111), (11,1,101,1111), (12,1,101,1111), (13,1,101,1111), (14,1,101,1111), (15,1,101,1111), (16,1,101,1111), (17,1,101,1111), (18,1,101,1111), (19,1,101,1111), (20,1,101,1111), (21,1,101,1111), (22,1,101,1111), (23,1,101,1111), (24,1,101,1111), (25,1,101,1111)
            )
            found should equal(expected)
            */
          case _ =>
        }
    }
    )
  }

}
