/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.bugs

import org.burstsys.alloy.views.unity.UnityUseCaseViews.over_200_200
import org.burstsys.hydra.test.support.GeneratedUnitySchema.{UnityTraveler_lexicon, UnityTraveler_lexicon_runtime}
import org.burstsys.hydra.test.support.HydraAlloyTestRunner
import org.scalatest.Ignore

import scala.language.postfixOps

// @Ignore
final
class HydraBugSpec16 extends HydraAlloyTestRunner {

  it should "successfully do put a user in a tablet" in {
    val source =
      s"""|
          |hydra $AnalysisName() {
          |   schema 'unity'
          |   {
          |      user <- ext_0
          |   }
          |   var T3:boolean=false
          |   frame $CubeFrame {
          |      var T1_summary:boolean=false
          |      var T2:boolean=false
          |      var T1:boolean=false
          |      var T2_summary:boolean=false
          |      cube user {
          |         limit = 1
          |         aggregates {
          |            '_a1_':sum[long]
          |         }
          |      }
          |      user => {
          |         pre => {
          |            // BasicLane[3](RESULT)
          |            T1_summary=false
          |         }
          |         post => {
          |            // BasicLane[3](RESULT)
          |            if (T1_summary) {
          |               T2_summary = true
          |               $AnalysisName.$CubeFrame.'_a1_' = 1
          |            }
          |         }
          |      }
          |      sgmnt.members user.ext_0 => {
          |         before => {
          |            // BasicLane[6066](segment(sgmnt)-596517)
          |            if (T3) {
          |             tabletMemberAdd(sgmnt, 596517)
          |            }
          |         }
          |
          |         situ => {
          |            T1 = (tabletMemberValue(sgmnt) in (596517))
          |            if (T1) {
          |               T1_summary = true
          |            }
          |         }
          |      }
          |   }
          |   frame sgmnt {
          |      tablet[long]
          |      user => {
          |         pre => {
          |            // BasicLane[6066](segment(sgmnt)-596517)
          |            T3 = (user.deviceModelId > 0) && (user.application.firstUse.osVersionId in (454545))
          |         }
          |      }
          |   }
          |}
          |""".stripMargin

    // I wished this worked.

    val sourcex =
      s"""|
          |hydra $AnalysisName() {
          |   schema 'unity'
          |   {
          |      user <- ext_0
          |   }
          |   frame $CubeFrame {
          |      var T1_summary:boolean=false
          |      var T2:boolean=false
          |      var T1:boolean=false
          |      var T2_summary:boolean=false
          |      cube user {
          |         limit = 1
          |         aggregates {
          |            '_a1_':sum[long]
          |         }
          |      }
          |      user => {
          |         pre => {
          |            // BasicLane[3](RESULT)
          |            T1_summary=false
          |         }
          |         post => {
          |            // BasicLane[3](RESULT)
          |            if (T1_summary) {
          |               T2_summary = true
          |               $AnalysisName.$CubeFrame.'_a1_' = 1
          |            }
          |         }
          |      }
          |
          |      sgmnt.members user.ext_0 => {
          |        situ => {
          |            T1 = (tabletMemberValue(sgmnt) in (596517))
          |            if (T1) {
          |               T1_summary = true
          |            }
          |        }
          |      }
          |   }
          |   frame sgmnt {
          |      var T3:boolean=false
          |      tablet[long]
          |      user => {
          |         pre => {
          |            // BasicLane[6066](segment(sgmnt)-596517)
          |            T3 = (user.deviceModelId > 0) && (user.application.firstUse.osVersionId in (454545))
          |         }
          |      }
          |      sgmnt.members user.ext_0 => {
          |            before => {
          |               // BasicLane[6066](segment(sgmnt)-596517)
          |               if (T3) {
          |                tabletMemberAdd(sgmnt, 596517)
          |               }
          |            }
          |      }
          |   }
          |}
          |""".stripMargin

    test(source, over_200_200, {
      (name, result) =>
        name match {
          case CubeFrame =>
            val found = result.rowSet.map {
              row => row.cells(0) asLong
            } sortBy (r => r)
            val expected = Array(25)
            found should equal(expected)
          case _ =>
        }
    },
    staticSweep = None // Some(new B6EA6710919C840979929D66C6E6B3A59)
    )
  }


}
