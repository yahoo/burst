/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.parameters

import org.burstsys.alloy.views.unity.UnityUseCaseViews.over_73_73
import org.burstsys.hydra.test.support.HydraAlloyTestRunner

import scala.language.postfixOps

//@Ignore
final
class HydraParamSpec01 extends HydraAlloyTestRunner {

  it should "successfully execute analysis with a short and a string parameter" in {
    val source =
      s"""
         |hydra myAnalysis(p1:short = 1, p2:string = "pirate") {
         |  schema unity
         |  frame myCube  {
         |    cube user {
         |      limit = 9999
         |      cube user.sessions.events {
         |        aggregates {
         |          count1:sum[long]
         |          count2:sum[long]
         |        }
         |      }
         |    }
         |    user.sessions.events ⇒ {
         |      pre ⇒ {
         |        if(  user.sessions.events.id == p1 ) {
         |          myCube.count1 = 1
         |          insert( myCube )
         |        }
         |        if(  user.sessions.events.eventType == p1 ) {
         |          myCube.count2 = 1
         |          insert( myCube )
         |        }
         |      }
         |    }
         |  }
         |}
     """.stripMargin

    test(
      hydra = source,
      ds = over_73_73,
      validate = {
        (name, result) =>
          result.resultName match {
            case "myCube" =>
              val found = result.rowSet.map {
                row => (row[Long]("count1"), row[Long]("count2"))
              } sortBy (_._2) sortBy (_._1)
              val expected =
                Array((1, 3))
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // Some(new B651053006AC94A5AB86DD3B77177E8EA)
    )

  }

}
