/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.refscalar

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase
import org.burstsys.hydra.test.cases.unity.conditional.HydraUnityCase06.{analysisName, frameName}

object HydraUnityCase07 extends HydraUseCase(200, 200, "unity") {

  //    override val sweep: BurstHydraSweep = new BA9D2F1CEA9824AD5ADABD2B295A93640

  override val frameSource: String =
    s"""
       frame frame07 {
         cube user {
           limit = 6
            aggregates {
               frequency:sum[long]
            }
            dimensions {
              appVersion:verbatim[long]
            }
         }
         user.sessions => {
           pre => {
              $analysisName.frame07.appVersion = user.sessions.appVersion.id
              $analysisName.frame07.frequency = 1
           }
         }
       }
     """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames("frame07"))
    assertLimits(r)

    val found = r.rowSet.map {
      row => (row.cells(0).asLong, row.cells(1).asLong)
    }.sortBy(_._1)
    found should equal(Array((12121212, 200), (13131313, 217), (101010101, 216), (1414141414, 200), (1515151515, 217), (1616161616, 200)))

  }


}
