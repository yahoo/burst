/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.refscalar

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase
import org.burstsys.hydra.test.cases.unity.conditional.HydraUnityCase06.{analysisName, frameName}

object HydraUnityCase11 extends HydraUseCase(200, 200, "unity") {

  //    override val sweep: BurstHydraSweep = new BA9D2F1CEA9824AD5ADABD2B295A93640

  override val frameSource: String =
    s"""
       frame $frameName {
         cube user {
          limit = 4
            aggregates {
               frequency:sum[long]
            }
            dimensions {
              originMethodTypeId:verbatim[long]
            }
         }
         user.sessions => {
           pre => {
              $analysisName.$frameName.originMethodTypeId = user.sessions.originMethodTypeId
              $analysisName.$frameName.frequency = 1
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
    found should equal(Array((12, 156), (13, 157), (14, 157), (15, 156), (16, 156), (17, 156), (18, 156), (19, 156)))

  }


}
