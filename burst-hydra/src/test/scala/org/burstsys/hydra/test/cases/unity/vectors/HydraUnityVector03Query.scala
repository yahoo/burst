/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.vectors

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase
import org.burstsys.hydra.test.cases.unity.splits.HydraUnitySplitCase00.{analysisName, frameName}

object HydraUnityVector03Query extends HydraUseCase(210, 210, "unity") {

  /*
    override val sweep = new BA148F62A4ABF4E74AA03CA26B7EB136F
    override val serializeTraversal = true
  */

  override val frameSource: String =
    s"""|
        |   frame $frameName {
        |      cube user {
        |         limit = 10
        |         cube user.sessions {
        |            aggregates {
        |               beforeCount:sum[long]
        |               afterCount:sum[long]
        |            }
        |         }
        |      }
        |      user.sessions.events => {
        |         after => {
        |               $analysisName.$frameName.afterCount = 1
        |               insert($analysisName.$frameName)
        |         }
        |      }
        |   }""".stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)
    val found = r.rowSet.map {
      row => (row.cells(0).asLong, row.cells(1).asLong)
    }.sortBy(_._1)
    found should equal(
      Array((0, 77))
    )

  }

}
