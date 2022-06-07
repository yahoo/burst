/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.maps

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase
import org.burstsys.hydra.test.cases.unity.conditional.HydraUnityCase06.{analysisName, frameName}

object HydraUnityCase14 extends HydraUseCase(200, 200, "unity") {

  //    override val sweep: BurstHydraSweep = new BA9D2F1CEA9824AD5ADABD2B295A93640

  override val frameSource: String =
    s"""
      frame $frameName  {
        cube user {
          limit =77
          cube user.sessions.events.parameters {
            aggregates {
              parameterKeyFrequency:sum[long]
            }
            dimensions {
              eventId:verbatim[long]
              parameterKey:verbatim[string]
            }
          }
        }

        user.sessions.events.parameters ⇒ {
          situ ⇒ {
              $analysisName.$frameName.parameterKey = key(user.sessions.events.parameters)
              $analysisName.$frameName.eventId = user.sessions.events.id
              $analysisName.$frameName.parameterKeyFrequency = 1
          }
        }

      }
    """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

    val found = r.rowSet.map {
      row => (row.cells(0).asLong, row.cells(1).asString, row.cells(2).asLong)
    }.sortBy(_._2).sortBy(_._1)
    found should equal(
      Array(
        (1, "EK1", 810), (1, "EK2", 811), (1, "EK3", 812), (1, "EK4", 812), (1, "EK5", 812), (1, "EK6", 812), (1, "EK7", 811),
        (2, "EK1", 812), (2, "EK2", 813), (2, "EK3", 813), (2, "EK4", 813), (2, "EK5", 812), (2, "EK6", 811), (2, "EK7", 811),
        (3, "EK1", 813), (3, "EK2", 813), (3, "EK3", 812), (3, "EK4", 811), (3, "EK5", 811), (3, "EK6", 812), (3, "EK7", 813),
        (4, "EK1", 812), (4, "EK2", 811), (4, "EK3", 811), (4, "EK4", 812), (4, "EK5", 813), (4, "EK6", 813), (4, "EK7", 813),
        (5, "EK1", 811), (5, "EK2", 812), (5, "EK3", 813), (5, "EK4", 813), (5, "EK5", 813), (5, "EK6", 812), (5, "EK7", 811),
        (6, "EK1", 812), (6, "EK2", 812), (6, "EK3", 812), (6, "EK4", 812), (6, "EK5", 811), (6, "EK6", 810), (6, "EK7", 811),
        (7, "EK1", 812), (7, "EK2", 812), (7, "EK3", 811), (7, "EK4", 810), (7, "EK5", 811), (7, "EK6", 812), (7, "EK7", 812),
        (8, "EK1", 811), (8, "EK2", 810), (8, "EK3", 811), (8, "EK4", 812), (8, "EK5", 812), (8, "EK6", 812), (8, "EK7", 812),
        (9, "EK1", 811), (9, "EK2", 812), (9, "EK3", 812), (9, "EK4", 812), (9, "EK5", 812), (9, "EK6", 811), (9, "EK7", 810),
        (10, "EK1", 812), (10, "EK2", 812), (10, "EK3", 812), (10, "EK4", 811), (10, "EK5", 810), (10, "EK6", 811), (10, "EK7", 812),
        (11, "EK1", 812), (11, "EK2", 811), (11, "EK3", 810), (11, "EK4", 811), (11, "EK5", 812), (11, "EK6", 812), (11, "EK7", 812)
      )
    )

  }


}
