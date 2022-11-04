/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.conditionals

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.sweep.HydraSweep
import org.burstsys.hydra.test.cases.quo.conditionals.HydraQuoConditionals02.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoConditionals05 extends HydraUseCase(1, 1, "quo") {

//    override lazy val sweep: HydraSweep = new B6951C4DB149F4FF1A2AA87AD8DB6B8AB

  override def analysisSource: String =
    s"""
       |hydra $analysisName() {
       |   schema 'quo'
       |   frame $frameName {
       |     cube user {
       |         limit = 1
       |         aggregates {
       |            count1:sum[long]
       |         }
       |      }
       |      user.sessions.events => {
       |         pre => {
       |            if(user.sessions.events.eventId == 6049337 || user.sessions.events.eventId == 4498119) {
       |                $analysisName.$frameName.count1 = 1
       |            }
       |         }
       |      }
       |   }
       |}
       """.stripMargin

  // 4866819
  // 48668190

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

    {
      val v: Long = r(0)[Long]("count1")
      v should equal(14834)
    }

  }



}
