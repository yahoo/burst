/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.bugs.old

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase

/**
 * https://git.ouroath.com/burst/burst/issues/1722
 */
object HydraUnityBug02 extends HydraUseCase(200, 200, "unity") {

  //  override val sweep = new B12ADE3BB29A04126ABFBC3209D8B8218

  val cubeName: String = frameName

  override val frameSource: String =
    s"""
   frame $frameName {
   var T2:boolean=false
   var T1:long=0
   cube user {
      limit = 100
      cube user.sessions {
         dimensions {
            two:verbatim[long]
         }
      }
   }
   user.sessions => {
      pre => {
         T1 = null
         T2 = true // (user.sessions.id % 2 == 0)
      }
      post => {
         // Lane[3](T1-(SUM(3) SCOPE user.sessions WHERE ((user.sessions.id % 2) == 0)))
         if (T2) {
            T1 = 3
         }
         // Lane[1](RESULT)
         $analysisName.$frameName.two = T1
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
