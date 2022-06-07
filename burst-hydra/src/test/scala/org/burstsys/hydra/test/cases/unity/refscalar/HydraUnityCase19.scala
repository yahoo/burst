/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.refscalar

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase
import org.burstsys.hydra.test.cases.unity.conditional.HydraUnityCase06.{analysisName, frameName}

object HydraUnityCase19 extends HydraUseCase(200, 200, "unity") {

  //  override val sweep: BurstHydraSweep = new B9DED0948517041B59E8B8B83F1EF7E88

  override val frameSource: String =
    s"""
      frame $frameName  {
        cube user {
          limit =77
          aggregates {
            userCount:sum[long]
          }
          cube user.application.lastUse {
            dimensions {
              localeCountryId:verbatim[long]
            }
          }
        }

        user ⇒ {
           pre ⇒ {
              $analysisName.$frameName.userCount = 1
           }
        }

        user.application.channels ⇒ {
           pre ⇒ {
              $analysisName.$frameName.localeCountryId = user.application.lastUse.localeCountryId
              insert($analysisName.$frameName)
           }
        }

      }
    """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

    val found = r.rowSet.map {
      row => (row.cells(0).asLong, row.cells(1).asLong)
    }.sortBy(_._1)

    found should equal(
      Array(
      )
    )

  }


}
