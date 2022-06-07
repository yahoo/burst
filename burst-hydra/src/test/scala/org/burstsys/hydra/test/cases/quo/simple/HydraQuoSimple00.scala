/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.simple

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.quo.parameters.HydraQuoParameters01.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

/**
  * ******* RESULTS ********
  * --------------------------------------------------------------------------------------
  * 84.6 query(s) per sec
  * 11.8 msec per query
  * --------------------------------------------------------------------------------------
  */
object HydraQuoSimple00 extends HydraUseCase(10, 10, "quo") {

  //  override val sweep: HydraSweep = new B78ACF5280964474D87A8A310DBE53C16

  override val frameSource: String =
    s"""
       |   frame $frameName {
       |     cube user {
       |         limit = 1
       |         aggregates {
       |            sessionCount:sum[long]
       |         }
       |      }
       |
       |      user.sessions ⇒ {
       |         post ⇒ {
       |            $analysisName.$frameName.sessionCount = 1
       |         }
       |      }
       |   }
       """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {

  }

}
