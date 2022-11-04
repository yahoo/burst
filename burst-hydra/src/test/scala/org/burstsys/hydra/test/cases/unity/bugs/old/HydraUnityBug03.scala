/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.bugs.old

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraUnityBug03 extends HydraUseCase(200, 200, "unity") {

  //  override val sweep = new B1801F3BDB5444032803E20CFD4D9A728

  val cubeName: String = frameName

  override val frameSource: String =
    s"""
   frame $frameName {
     cube user {
        limit = 20
        aggregates {
           count:sum[long]
        }
        cube user.application {
           cube user.application.lastUse {
              dimensions {
                 end:dayGrain[long]
              }
           }
           cube user.application.firstUse {
              dimensions {
                 start:dayGrain[long]
              }
           }
        }
     }
     user => {
        post => {
           // Lane[1](RESULT)
           $analysisName.$frameName.count = 1
        }
     }
     user.application.firstUse => {
        pre => {
           // Lane[1](RESULT)
           $analysisName.$frameName.start = user.application.firstUse.sessionTime
        }
        post => {
           // Lane[1](RESULT)
           insert($analysisName.$frameName)
        }
     }
     user.application.lastUse => {
        pre => {
           // Lane[1](RESULT)
           $analysisName.$frameName.end = user.application.lastUse.sessionTime
        }
        post => {
           // Lane[1](RESULT)
           insert($analysisName.$frameName)
        }
     }
   }
  """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

  }


}
