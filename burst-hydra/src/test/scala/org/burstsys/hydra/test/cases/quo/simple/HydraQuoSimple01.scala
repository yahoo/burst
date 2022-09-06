/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.simple

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase

/**
 * ******* RESULTS ********
 * --------------------------------------------------------------------------------------
 * 84.6 query(s) per sec
 * 11.8 msec per query
 * --------------------------------------------------------------------------------------
 */
object HydraQuoSimple01 extends HydraUseCase(1, 1, "quo" /*, executionCount = 1e6.toInt*/) {

  //  override val sweep = new B16D27254556949BBAEC52B9F8E6E2547

  override val frameSource: String =
    s"""
       |   frame $frameName {
       |     cube user {
       |         limit = 1
       |         aggregates {
       |            userCount:sum[long]
       |            sessionCount:sum[long]
       |            eventCount:sum[long]
       |         }
       |      }
       |
       |      user => {
       |         pre => {
       |            $analysisName.$frameName.userCount = 1
       |         }
       |      }
       |      user.sessions => {
       |         pre => {
       |            $analysisName.$frameName.sessionCount = 1
       |         }
       |      }
       |      user.sessions.events => {
       |         pre => {
       |            $analysisName.$frameName.eventCount = 1
       |         }
       |      }
       |   }
       """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
  }


}
