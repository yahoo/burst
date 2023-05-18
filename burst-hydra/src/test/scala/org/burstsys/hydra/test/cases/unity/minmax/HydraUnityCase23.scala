/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.minmax

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase
import org.burstsys.hydra.test.cases.unity.conditional.HydraUnityCase06.{analysisName, frameName}

object HydraUnityCase23 extends HydraUseCase(100, 200, "unity") {

  /*
    override val sweep: HydraSweep = new B657E92DF271B48A283ABE27C5998A87A

    override def serializeTraversal: Boolean = true
  */

  override val frameSource: String =
    s"""
       | frame $frameName {
       |      cube user {
       |         limit = 100
       |         aggregates {
       |            userCount:sum[long]
       |            sessionCount:sum[long]
       |            eventCount:sum[long]
       |            minSessionTime:min[long]
       |            maxSessionTime:max[long]
       |         }
       |      }
       |      user => {
       |         post => {
       |            $analysisName.$frameName.userCount = 1
       |         }
       |      }
       |      user.sessions => {
       |         post => {
       |            $analysisName.$frameName.sessionCount = 1
       |            $analysisName.$frameName.minSessionTime = user.sessions.startTime
       |            $analysisName.$frameName.maxSessionTime = user.sessions.startTime
       |         }
       |      }
       |      user.sessions.events => {
       |         post => {
       |            $analysisName.$frameName.eventCount = 1
       |         }
       |      }
       |   }
        """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

    val found = r.rowSet.map {
      row =>
        (
          row.cells(0).asLong
          , row.cells(1).asLong
          , row.cells(2).asLong
          , row.cells(3).asLong
          , row.cells(4).asLong
        )
    }
    //    found should equal(Array(1514797260001L))
    val userCount = 50
    val sessionCount = 1250
    val eventCount = 12500
    val min = 1514797260001L
    val max = 1514797273740L
    found should equal(Array((
      userCount
      , sessionCount
      , eventCount
      , min
      , max
    )))
  }


}
