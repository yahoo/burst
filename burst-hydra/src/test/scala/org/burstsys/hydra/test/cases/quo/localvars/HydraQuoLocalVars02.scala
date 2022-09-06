/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.localvars

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.quo.conditionals.HydraQuoConditionals02.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoLocalVars02 extends HydraUseCase(1, 1, "quo") {

  //  override val sweep: HydraSweep = new BC02F274F467E46A59C9739AB8F763240

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
       |         }
       |      }
       |      user.sessions.events => {
       |        pre => {
       |            val lv1:long = user.sessions.events.eventId - 1
       |            if( lv1 == (6049337 - 1) ) { $analysisName.$frameName.event1 = 1 }
       |            if( lv1 == (4498119 - 1) ) { $analysisName.$frameName.event2 = 1 }
       |        }
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
      val v: Long = r(0)[Long]("event1")
      v should equal(279)
    }

    {
      val v: Long = r(0)[Long]("event2")
      v should equal(14555)
    }
  }



}
