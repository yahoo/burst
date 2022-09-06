/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.empty

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.status.FabricNoDataResultStatus
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoEmpty00 extends HydraUseCase(-1, -1, "quo"
  //  , executionCount = 1e6.toInt
) {

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
       |      user.sessions => {
       |         post => {
       |            $analysisName.$frameName.sessionCount = 1
       |         }
       |      }
       |   }
       """.stripMargin

  override def expectsException: Boolean = true

  override def validate(implicit result: FabricResultGroup): Unit = {
    result.resultStatus === FabricNoDataResultStatus
  }

}
