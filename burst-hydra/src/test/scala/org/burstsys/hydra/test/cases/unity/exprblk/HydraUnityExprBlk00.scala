/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.exprblk

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraUnityExprBlk00 extends HydraUseCase(200, 200, "unity") {

  //    override val sweep = new B08926B0E02714922A59E3C5A7EF580F6

  override val frameSource: String =
    s"""
       |frame $frameName {
       |  cube user {
       |    limit = 100
       |    aggregates {
       |      count:sum[long]
       |    }
       |  }
       |  user.sessions.events => {
       |    pre => {
       |        $analysisName.$frameName.count = 1
       |        5 + 4
       |    }
       |  }
       |}""".stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    result.resultSets(result.resultSetNames(frameName))
  }


}
