/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.maps

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase
import org.burstsys.hydra.test.cases.unity.conditional.HydraUnityCase06.{analysisName, frameName}

object HydraUnityCase12 extends HydraUseCase(200, 200, "unity") {

  //    override val sweep: BurstHydraSweep = new BA9D2F1CEA9824AD5ADABD2B295A93640

  override val frameSource: String =
    s"""
      frame $frameName {
          cube user {
            limit = 7
            cube user.sessions {
              aggregates {
                parameterFrequency:sum[long]
              }
              dimensions {
                parameterKey:verbatim[string]
              }
            }
          }

          user.sessions.parameters ⇒ {
            situ ⇒ {
              $analysisName.$frameName.parameterKey = key(user.sessions.parameters)
              $analysisName.$frameName.parameterFrequency = 1
            }
          }

      }
    """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

    val found = r.rowSet.map {
      row => (row.cells(0).asString, row.cells(1).asLong)
    }.sortBy(_._1)
    found should equal(Array(("SK1", 892), ("SK2", 893), ("SK3", 893), ("SK4", 893), ("SK5", 893), ("SK6", 893), ("SK7", 893)))

  }


}
