/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.bugs

import org.burstsys.alloy.views.AlloySmallDatasets.smallDataset_two_user_three_session
import org.burstsys.alloy.views.unity.UnityUseCaseViews.over_200_200
import org.burstsys.hydra.test.support.HydraAlloyTestRunner
import org.scalatest.Ignore

import scala.language.postfixOps

@Ignore
final
class HydraBugSpec13 extends HydraAlloyTestRunner {

  it should "bug 13" in {
    val source =
      s"""|
          |hydra eqlGenerated() {
          |   schema 'unity'
          |   frame query_1 {
          |      cube user {
          |         limit = 20
          |         aggregates {
          |            'count':sum[long]
          |         }
          |         cube user.application {
          |            cube user.application.lastUse {
          |               dimensions {
          |                  'end':dayGrain[long]
          |               }
          |            }
          |            cube user.application.firstUse {
          |               dimensions {
          |                  'start':dayGrain[long]
          |               }
          |            }
          |         }
          |      }
          |      user => {
          |         post => {
          |            // BasicLane[1](RESULT)
          |            eqlGenerated.query_1.'count' = 1
          |         }
          |      }
          |      user.application.firstUse => {
          |         pre => {
          |            // BasicLane[1](RESULT)
          |            eqlGenerated.query_1.'start' = user.application.firstUse.sessionTime
          |         }
          |         post => {
          |            // BasicLane[1](RESULT)
          |            insert(eqlGenerated.query_1)
          |         }
          |      }
          |      user.application.lastUse => {
          |         pre => {
          |            // BasicLane[1](RESULT)
          |            eqlGenerated.query_1.'end' = user.application.lastUse.sessionTime
          |         }
          |         post => {
          |            // BasicLane[1](RESULT)
          |            insert(eqlGenerated.query_1)
          |         }
          |      }
          |   }
          |}
          |""".stripMargin

    test(
      hydra = source,
      ds = over_200_200,
      validate = {
        (name, result) =>
          result.resultName match {
            case "query_1" =>
              val found = result.rowSet.map {
                row =>
                  (
                    row[Long]("count"),
                    row[Long]("start"),
                    row[Long]("end")
                  )
              }  sortBy (_._3) sortBy (_._2) sortBy (_._1)
              val expected =
                Array(
                  (50,1483257600000L,1485849600000L)
                )
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // Some(new BCDF77DCAC2A44084A4F91422156FF558)
    )


  }

}
