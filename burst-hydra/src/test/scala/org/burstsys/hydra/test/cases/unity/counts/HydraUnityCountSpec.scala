/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.counts

import org.burstsys.alloy.views.unity.UnityUseCaseViews.over_9999_9999
import org.burstsys.hydra.test.support.HydraAlloyTestRunner

import scala.language.postfixOps

class HydraUnityCountSpec extends HydraAlloyTestRunner {

  it should "count users, sessions, and events" in {
    val source: String =
      s"""
         |hydra $AnalysisName() {
         |  schema unity
         |
         |  frame $CubeFrame {
         |
         |    cube user {
         |      limit = 1
         |      aggregates {
         |        userCount:sum[long]
         |        sessionCount:sum[long]
         |        eventCount:sum[long]
         |      }
         |    }
         |
         |    user => {
         |      pre => {
         |        $CubeFrame.userCount = 1
         |      }
         |    }
         |    user.sessions => {
         |      pre => {
         |        $CubeFrame.sessionCount = 1
         |      }
         |    }
         |    user.sessions.events => {
         |      pre => {
         |        $CubeFrame.eventCount = 1
         |      }
         |    }
         |
         |  } // end frame
         |} // end analysis""".stripMargin

    test(
      hydra = source,
      ds = over_9999_9999,
      maxExecutions =  1, //  1e6.toInt,
      validate = {
        (name, result) =>
          name match {
            case CubeFrame =>
              val found = result.rowSet.map {
                row =>
                  (
                    row[Long]("userCount"),
                    row[Long]("sessionCount"),
                    row[Long]("eventCount")
                  )
              } sortBy (_._3) sortBy (_._2) sortBy (_._1)
              val expected =
                Array((1,100,300000))
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // Some(new B54400CB98D914FA1AED200C77CAF7FAF)
    )
  }

}
