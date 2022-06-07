/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.conditionals

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.quo.calordinals.HydraQuoCalDayOfYearOrdinal.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoConditionals01 extends HydraUseCase(1, 1, "quo") {

  //  override val sweep: HydraSweep = new B7225A805B1AF476D84A642D5938FE89B

  override def analysisSource: String =
    s"""
       |hydra $analysisName() {
       |   schema 'quo'
       |   frame $frameName {
       |     cube user {
       |         limit = 1
       |         aggregates {
       |            count1:sum[long]
       |            count2:sum[long]
       |         }
       |      }
       |      user.sessions.events ⇒ {
       |         pre ⇒ {
       |            if(false) { $analysisName.$frameName.count1 = 3 } else { $analysisName.$frameName.count1 = 30 }
       |            if(true) { $analysisName.$frameName.count2 = 3 } else { $analysisName.$frameName.count2 = 30 }
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
      v should equal(4934460)
    }

    {
      val v: Long = r(0)[Long]("count2")
      v should equal(493446)
    }
  }


}
