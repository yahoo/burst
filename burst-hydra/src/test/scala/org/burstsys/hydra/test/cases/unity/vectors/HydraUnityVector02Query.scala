/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.vectors

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase
import org.burstsys.hydra.test.cases.unity.splits.HydraUnitySplitCase00.{analysisName, frameName}

object HydraUnityVector02Query extends HydraUseCase(210, 210, "unity") {

  /*
    override val sweep = new B0829C3CB6AC14F3798929C8CC1BC2AE8
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
        |      user.sessions => {
        |         before => {
        |               $analysisName.$frameName.beforeCount = 1
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
      Array((7,0))
    )

  }


}
