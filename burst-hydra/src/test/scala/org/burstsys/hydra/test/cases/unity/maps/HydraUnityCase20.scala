/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.maps

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase
import org.burstsys.hydra.test.cases.unity.conditional.HydraUnityCase06.{analysisName, frameName}

object HydraUnityCase20 extends HydraUseCase(200, 200, "unity") {

  //      override val sweep: BurstHydraSweep = new BE65BEBF88AFD4AE18AEBF403F019E3BC

  override val frameSource: String =
    s"""
      frame $frameName {
          cube user {
              limit =7
              aggregates {
               eventFrequency:sum[long]
              }
              cube user.sessions.events.parameters {
                dimensions {
                  parameterKey:verbatim[string]
                }
              }
          }
          user.sessions.events => {
             pre => {
               $analysisName.$frameName.eventFrequency = 1
             }
          }
          user.sessions.events.parameters => {
             situ => {
               $analysisName.$frameName.parameterKey = key(user.sessions.events.parameters)
               insert($analysisName.$frameName)
             }
          }
      }
    """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)
    val names = r.columnNames

    val found = r.rowSet.map {
      row => (row.cells(0).asString, row.cells(1).asLong)
    }.sortBy(_._1)
    found should equal(Array(("EK1", 8928), ("EK2", 8929), ("EK3", 8929), ("EK4", 8929), ("EK5", 8929), ("EK6", 8928), ("EK7", 8928)))

  }


}
