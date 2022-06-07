/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.refscalar

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase
import org.burstsys.hydra.test.cases.unity.conditional.HydraUnityCase06.{analysisName, frameName}

object HydraUnityCase06 extends HydraUseCase(200, 200, "unity") {

  //    override val sweep: BurstHydraSweep = new BA9D2F1CEA9824AD5ADABD2B295A93640

  override val frameSource: String =
    s"""
       frame frame06 {
         cube user {
           limit = 5
           dimensions {
               languageId:verbatim[long]
           }
         }
         user.application.firstUse ⇒ {
           pre ⇒ {
              $analysisName.frame06.languageId = user.application.firstUse.languageId
              insert($analysisName.frame06)
           }
         }
       }
     """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames("frame06"))
    assertLimits(r)

    val found = r.rowSet.map {
      row => row.cells(0).asLong
    }.sorted
    found should equal(Array(111222L, 333444L, 555666L, 777888L, 888999L))

  }


}
