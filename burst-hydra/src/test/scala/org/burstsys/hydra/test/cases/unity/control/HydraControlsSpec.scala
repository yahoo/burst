/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.control

import org.burstsys.alloy.views.AlloySmallDatasets.smallDataset_one_user_one_session
import org.burstsys.alloy.views.unity.UnityUseCaseViews.over_200_200
import org.burstsys.hydra.test.support.HydraAlloyTestRunner

class HydraControlsSpec extends HydraAlloyTestRunner {

  it should "code generate all control verbs" in {
    val source =
      s"""
          |hydra $AnalysisName() {
          | schema unity {
          |   user <- ext1
          | }
          | frame $CubeFrame {
          |   cube user {
          |     limit = 1
          |     aggregates {
          |        a0:sum[long]
          |      }
          |      dimensions {
          |        d0:verbatim[long]
          |      }
          |    }
          |    user.sessions.events => {
          |      pre => {
          |        commitRelation(user.sessions.events)
          |        abortRelation(user.sessions.events)
          |        commitMember(user)
          |        abortMember(user.sessions)
          |      }
          |    }
          | }
          |}""".stripMargin

    test(
      hydra = source,
      ds = over_200_200,
      validate = {
        (name, result) =>
          name match {
            case CubeFrame =>
              val found = result.rowSet.map {
                row =>
                  (
                    row[Long]("a0"),
                    row[Long]("d0")
                  )
              } sortBy (_._2) sortBy (_._1)
              val expected =
                Array(
                )
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // new B25C84BD3115F4BACA862D4F6162FBB17,
    )
  }

  it should "user level abortRelation control verb" in {
    val source =
      s"""
          |hydra $AnalysisName() {
          | schema unity {
          |   user <- ext1
          | }
          | frame $CubeFrame {
          |    cube user {
          |      limit = 1
          |      aggregates {
          |        a0:sum[long]
          |      }
          |      dimensions {
          |        d0:verbatim[long]
          |      }
          |    }
          |    user => {
          |      post => {
          |        $CubeFrame.d0 = 55
          |        $CubeFrame.a0 = 77
          |      }
          |    }
          |    user.sessions => {
          |      pre => {
          |        $CubeFrame.d0 = 11
          |        $CubeFrame.a0 = 33
          |        abortRelation(user)
          |     }
          |   }
          | }
          |}""".stripMargin

    test(
      hydra = source,
      ds = over_200_200,
      validate = {
        (name, result) =>
          name match {
            case CubeFrame =>
              val found = result.rowSet.map {
                row =>
                  (
                    row[Long]("a0"),
                    row[Long]("d0")
                  )
              } sortBy (_._2) sortBy (_._1)
              val expected =
                Array(
                )
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // new B25C84BD3115F4BACA862D4F6162FBB17,
    )
  }

  it should "member level abortRelation control verb" in {
    val source =
      s"""
          |hydra $AnalysisName() {
          | schema unity
          | frame $CubeFrame {
          |    cube user {
          |      limit = 1
          |      aggregates {
          |        a0:sum[long]
          |        a1:sum[long]
          |      }
          |      dimensions {
          |        d0:verbatim[long]
          |        d1:verbatim[long]
          |      }
          |    }
          |    user => {
          |      post => {
          |        $CubeFrame.d0 = 55
          |        $CubeFrame.a0 = 77
          |      }
          |    }
          |    user.sessions => {
          |      pre => {
          |        $CubeFrame.d1 = 11
          |        $CubeFrame.a1 = 33
          |        abortMember(user.sessions)
          |     }
          |   }
          | }
          |}""".stripMargin

    test(
      hydra = source,
      ds = smallDataset_one_user_one_session,
      validate = {
        (name, result) =>
          name match {
            case CubeFrame =>
              val found = result.rowSet.map {
                row =>
                  (
                    row[Long]("a0"),
                    row[Long]("a1"),
                    row[Long]("d0"),
                    row[Long]("d1")
                  )
              } sortBy (_._4) sortBy (_._3) sortBy (_._2) sortBy (_._1)
              val expected =
                Array(
                  (77, 0, 55, 0)
                )
            case _ =>
          }
      },
      staticSweep = None // new B25C84BD3115F4BACA862D4F6162FBB17,
    )
  }

  it should "code generate all control verb for one frame but not the other" in {
    val source =
      s"""
          |hydra eqlGenerated() {
          |   schema 'unity'
          |   frame query_1 {
          |      var T1_summary:boolean=false
          |      var T1:boolean=false
          |      cube user {
          |         limit = 100
          |         aggregates {
          |            '_selected_':sum[long]
          |         }
          |      }
          |      user => {
          |         pre => {
          |            // BasicLane[1](RESULT)
          |            T1 = (user.deviceModelId in (555666))
          |            if (T1) {
          |            } else {
          |               abortRelation(user)
          |            }
          |         }
          |         post => {
          |            // BasicLane[1](RESULT)
          |            if (T1) {
          |               T1_summary = true
          |               eqlGenerated.query_1.'_selected_' = 1
          |            }
          |         }
          |      }
          |   }
          |   frame query_2 {
          |      cube user {
          |         limit = 100
          |         aggregates {
          |            '_total_':sum[long]
          |         }
          |      }
          |      user => {
          |         post => {
          |            // BasicLane[1](RESULT)
          |            eqlGenerated.query_2.'_total_' = 1
          |         }
          |      }
          |   }
          |}""".stripMargin

    test(
      hydra = source,
      ds = over_200_200,
      validate = {
        (name, result) =>
          name match {
            case "query_1" =>
              val found = result.rowSet.map {
                row => row[Long]("_selected_")
              }
              val expected = Array(16 )
              found should equal(expected)
            case "query_2" =>
              val found = result.rowSet.map {
                row => row[Long]("_total_")
              }
              val expected = Array(50)
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // new B25C84BD3115F4BACA862D4F6162FBB17,
    )
  }

}
