/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.bugs

import org.burstsys.hydra.test.support.HydraAlloyTestRunner
import org.scalatest.Ignore

import scala.language.postfixOps


/*
T2 = T1 && (user.sessions.startTime > datetime(eqlGenerated.'startDate')) && (user.sessions.startTime < datetime(eqlGenerated.'endDate'))
                                                                ^ no viable alternative at input '&&(user.sessions.startTime>datetime(eqlGenerated'
 */
@Ignore
final
class HydraBugSpec01 extends HydraAlloyTestRunner {

  it should "bug 01" in {
    val source =
      s"""|
          |hydra eqlGenerated('flurryId':string=null,'startDate':string=null,'endDate':string=null) {
          |   schema 'unity'
          |   frame query_1 {
          |      var T2:boolean=false
          |      var T1_summary:boolean=false
          |      var T1:boolean=false
          |      var T2_summary:boolean=false
          |      var T4:boolean=false
          |      var T3:boolean=false
          |      cube user {
          |         limit = 100
          |         dimensions {
          |            'sessionId':verbatim[long]
          |            'osVersion':verbatim[long]
          |            'carrierId':verbatim[long]
          |            'sessionLength':verbatim[long]
          |            'sessionStartTime':verbatim[long]
          |         }
          |         cube user.sessions.appVersion {
          |            dimensions {
          |               'appVersionId':verbatim[long]
          |            }
          |         }
          |         cube user.sessions.events {
          |            aggregates {
          |               'eventCount':sum[long]
          |            }
          |         }
          |      }
          |      user.sessions.appVersion => {
          |         pre => {
          |            // BasicLane[3](RESULT)
          |            T3 = T2
          |            if (T3) {
          |               eqlGenerated.query_1.'appVersionId' = user.sessions.appVersion.id
          |            }
          |         }
          |         post => {
          |            // BasicLane[3](RESULT)
          |            if (T3) {
          |               insert(eqlGenerated.query_1)
          |            }
          |         }
          |      }
          |      user => {
          |         pre => {
          |            // BasicLane[3](RESULT)
          |            T1 = (user.id == eqlGenerated.'flurryId')
          |            T2_summary=false
          |            if (T1) {
          |            } else {
          |               abortRelation(user)
          |            }
          |         }
          |         post => {
          |            // BasicLane[3](RESULT)
          |            if (T2_summary && T1) {
          |               T1_summary = true
          |            }
          |         }
          |      }
          |      user.sessions => {
          |         pre => {
          |            // BasicLane[3](RESULT)
          |            T2 = T1 && (user.sessions.startTime > datetime(eqlGenerated.'startDate')) && (user.sessions.startTime < datetime(eqlGenerated.'endDate'))
          |            if (T2) {
          |               eqlGenerated.query_1.'sessionId' = user.sessions.id
          |               eqlGenerated.query_1.'osVersion' = user.sessions.osVersionId
          |               eqlGenerated.query_1.'carrierId' = user.sessions.carrierId
          |               eqlGenerated.query_1.'sessionLength' = user.sessions.duration
          |               eqlGenerated.query_1.'sessionStartTime' = user.sessions.startTime
          |            }
          |         }
          |         post => {
          |            // BasicLane[3](RESULT)
          |            if (T2) {
          |               T2_summary = true
          |               insert(eqlGenerated.query_1)
          |            }
          |         }
          |      }
          |      user.sessions.events => {
          |         pre => {
          |            // BasicLane[3](RESULT)
          |            T4 = T2
          |         }
          |         post => {
          |            // BasicLane[3](RESULT)
          |            if (T4) {
          |               eqlGenerated.query_1.'eventCount' = 1
          |            }
          |         }
          |      }
          |   }
          |}
          |""".stripMargin

    /*
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
                    Array((2, 2))
                  found should equal(expected)
                case _ =>
              }
          },
          staticSweep = None // Some(new BC0BC552D3EDC4238A812660C63CF1AF0)
        )
    */


  }

}
