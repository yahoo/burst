/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.conditional

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase
import org.burstsys.hydra.test.cases.unity.bugs.old.HydraUnityBug05.{analysisName, frameName}

object HydraUnityCase05 extends HydraUseCase(100, 100, "unity") {

  //  override val sweep: BurstHydraSweep = new B90197A0BF6974C9B88A832ABC56F379D

  override val frameSource: String =
    s"""
       frame $frameName {
          cube user {
           limit = 4
            aggregates {
              userCount:sum[long]
              nullCount:sum[long]
            }
          }

          user => {
            pre => {
              $analysisName.$frameName.userCount = if(true) {
                1
              } else {
                  2
              } elseNull {
                  3
              }

            }
          }
       }
     """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

  }


}
