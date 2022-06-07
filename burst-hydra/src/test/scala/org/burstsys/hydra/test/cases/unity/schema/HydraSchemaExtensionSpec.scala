/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.schema

import org.burstsys.alloy.views.AlloySmallDatasets.smallDataset_2_users_5_sessions
import org.burstsys.alloy.views.unity.UnityUseCaseViews.over_200_200
import org.burstsys.hydra.test.support.HydraAlloyTestRunner

import scala.language.postfixOps

class HydraSchemaExtensionSpec extends HydraAlloyTestRunner {

  it should "successfully define an extended path sub cube" in {
    val source =
      s"""|
          |hydra $AnalysisName() {
          | schema unity {
          |   user <- ext1
          |   user.sessions <- ext2
          | }
          | frame $CubeFrame {
          |   cube user {
          |     limit = 9999
          |     aggregates {
          |       'count':sum[long]
          |     }
          |     cube user.ext1 {
          |       dimensions {
          |         d1:verbatim[long]
          |       }
          |     }
          |   }
          | }
          |}""".stripMargin

    test(
      source, over_200_200, {
        (name, result) =>
      }
    )
  }

  it should "successfully define an extended path sub cube merge" in {
    val source =
      s"""|
          | hydra myAnalysis() {
          |   schema 'unity' {
          |      user <- ext1
          |   }
          |   frame myCube {
          |      cube user {
          |         limit = 100
          |         aggregates {
          |            'count':sum[long]
          |         }
          |         dimensions {
          |           'id':verbatim[long]
          |         }
          |      }
          |      myTablet.members user.ext1 => {
          |         situ => {
          |            myCube.'id' = tabletMemberValue( myTablet )
          |            myCube.'count' = 1
          |         }
          |      }
          |   }
          |   frame myTablet {
          |      tablet[long]
          |      user.sessions => {
          |         pre => {
          |           tabletMemberAdd( myTablet, user.sessions.id )
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
            case "myCube" =>
              val found = result.rowSet.map {
                row => (row[Long]("count"), row[Long]("id"))
              } sortBy (_._2) sortBy (_._1)
              val expected =
                Array((1, 0), (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9))
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None //  Some(new BE8675EC2EA454307A08F5A6BB141733D)
    )
  }

  it should "successfully define an extended path sub cube join" in {
    val source =
      s"""|
          |hydra $AnalysisName() {
          | schema unity {
          |   user <- ext1
          | }
          | frame $CubeFrame {
          |   cube user {
          |     limit = 9999
          |     cube user.ext1 {
          |       aggregates {
          |         'a1':sum[long]
          |       }
          |       dimensions {
          |         d1:verbatim[long]
          |       }
          |     }
          |   }
          |   myTablet.members user.ext1 => {
          |     situ => {
          |       myCube.'d1' = tabletMemberValue(myTablet)
          |       myCube.'a1' = 1
          |     }
          |   }
          | }
          | frame myTablet {
          |   tablet[long]
          |   user.sessions => {
          |     pre => {
          |       tabletMemberAdd(myTablet, user.sessions.id)
          |     }
          |   }
          | }
          |}""".stripMargin

    test(
      hydra = source,
      ds = smallDataset_2_users_5_sessions,
      validate = {
        (name, result) =>
          result.resultName match {
            case "myCube" =>
              val found = result.rowSet.map {
                row => (row[Long]("a1"), row[Long]("d1"))
              } sortBy (_._2) sortBy (_._1)
              val expected =
                Array((1, 0), (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9))
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // Some(new B53F39494BD91407FABA1F0D7057CD468)
    )
  }

}
