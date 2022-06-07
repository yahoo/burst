/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.conditionals

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoConditionals02 extends HydraUseCase(1, 1, "quo") {

  //  override val sweep: HydraSweep = new B537BE28A89DE413788734B67B4A149A6

  override def analysisSource: String =
    s"""
       |hydra $analysisName() {
       |   schema 'quo'
       |   frame $frameName {
       |     cube user {
       |         limit = 1
       |         aggregates {
       |            event1:sum[long]
       |            event2:sum[long]
       |            other:sum[long]
       |         }
       |      }
       |      user.sessions.events ⇒ {
       |         pre ⇒ {
       |            if( user.sessions.sessionId == 1420795664411 ) {
       |              $analysisName.$frameName.event1 = 1
       |            } else if( user.sessions.sessionId == 1419899489335) {
       |              $analysisName.$frameName.event2 = 1
       |            } else {
       |              $analysisName.$frameName.other = 1
       |            }
       |            insert($analysisName.$frameName)
       |         }
       |      }
       |   }
       |}
       """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

    {
      val v: Long = r(0)[Long]("event1")
      v should equal(1)
    }

    {
      val v: Long = r(0)[Long]("event2")
      v should equal(99)
    }

    {
      val v: Long = r(0)[Long]("other")
      v should equal(164382)
    }
  }


}
