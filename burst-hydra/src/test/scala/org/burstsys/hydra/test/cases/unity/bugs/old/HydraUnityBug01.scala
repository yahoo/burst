/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.bugs.old

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraUnityBug01 extends HydraUseCase(200, 200, "unity") {

  //  override val sweep = new BCE228332C97A44169647712DD6C1C522

  val cubeName: String = frameName

  override val frameSource: String =
    s"""
   frame $frameName {
      var T2:boolean=false
      var T1:byte=0
      cube user {
         limit = 100
         aggregates {
            one:sum[byte]
         }
         cube user.sessions {
            dimensions {
               two:verbatim[byte]
            }
         }
      }
      user => {
         post => {
            // LANE: RESULT
            $analysisName.$frameName.one = 2
         }
      }
      user.sessions => {
         pre => {
            // LANE: INIT T1-(SUM((1 + 2)) SCOPE user.sessions WHERE ((user.sessions.id % 2) == 0))
            T1 = null
            // LANE: T1-(SUM((1 + 2)) SCOPE user.sessions WHERE ((user.sessions.id % 2) == 0))
            T2 = (user.sessions.id % 2 == 0)
         }
         post => {
            // LANE: RESULT
            $analysisName.$frameName.two = T1
            insert($analysisName.$frameName)
            // LANE: T1-(SUM((1 + 2)) SCOPE user.sessions WHERE ((user.sessions.id % 2) == 0))
            if (T2) {
               T1 = T1 + 1 + 2
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
