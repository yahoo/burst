/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.bugs

import org.burstsys.alloy.views.AlloySmallDatasets.smallDataset_two_user_three_session
import org.burstsys.hydra.test.support.HydraAlloyTestRunner
import org.scalatest.Ignore

import scala.language.postfixOps

@Ignore
final
class HydraBugSpec12 extends HydraAlloyTestRunner {

  it should "bug 12" in {
    val source =
      s"""|
          |hydra eqlGenerated('sessionId':long=0,'flurryId':string=null,'eventId':long=0,'startTime':long=0) {
          |   schema 'unity'
          |   frame query_1 {
          |      var T2:boolean=false
          |      var T1_summary:boolean=false
          |      var T1:boolean=false
          |      var T2_summary:boolean=false
          |      var T3_summary:boolean=false
          |      var T4:boolean=false
          |      var T3:boolean=false
          |      cube user {
          |         limit = 250
          |         dimensions {
          |            'k':verbatim[string]
          |            'v':verbatim[string]
          |         }
          |      }
          |      user.sessions.events.parameters => {
          |         situ => {
          |            T4 = T3
          |            if (T4) {
          |               eqlGenerated.query_1.'k' = key(user.sessions.events.parameters)
          |               eqlGenerated.query_1.'v' = value(user.sessions.events.parameters)
          |               insert(eqlGenerated.query_1)
          |            }
          |         }
          |      }
          |      user => {
          |         pre => {
          |            // BasicLane[1](RESULT)
          |            T1 = (user.id == eqlGenerated.'flurryId')
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
          |      user.sessions => {
          |         pre => {
          |            // BasicLane[1](RESULT)
          |            T2 = T1 && (user.sessions.id == eqlGenerated.'sessionId')
          |            T3_summary=false
          |         }
          |         post => {
          |            // BasicLane[1](RESULT)
          |            if (T3_summary && T2) {
          |               T2_summary = true
          |            }
          |         }
          |      }
          |      user.sessions.events => {
          |         pre => {
          |            // BasicLane[1](RESULT)
          |            T3 = T2 && (user.sessions.events.id == eqlGenerated.'eventId') && (user.sessions.events.startTime == eqlGenerated.'startTime')
          |         }
          |         post => {
          |            // BasicLane[1](RESULT)
          |            if (T3) {
          |               T3_summary = true
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
            case "query_1" =>
              val found = result.rowSet.map {
                row =>
                  (
                    row[Long]("k"),
                    row[Long]("v")
                  )
              }  sortBy (_._2) sortBy (_._1)
              val expected =
                Array(
                )
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // Some(new BCDF77DCAC2A44084A4F91422156FF558)
    )


  }

}
