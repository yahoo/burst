/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.nested

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase
import org.burstsys.hydra.test.cases.unity.conditional.HydraUnityCase06.{analysisName, frameName}

object HydraUnityCase13 extends HydraUseCase(domain = 200, view = 200, schemaName = "unity") {

  //  override val sweep: HydraSweep = new BE9B14CD531734B938B82077CE14C9347

  override val frameSource: String =
    s"""
       frame $frameName {
          cube user {
            limit = 11
            cube user.sessions.events {
              aggregates {
                eventFrequency:sum[long]
              }
              dimensions {
                eventId:verbatim[long]
              }
            }
          }

          user.sessions.events ⇒ {
            pre ⇒ {
              $analysisName.$frameName.eventId = user.sessions.events.id
              $analysisName.$frameName.eventFrequency = 1
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
    found should equal(Array((1, 1136), (2, 1137), (3, 1137), (4, 1137), (5, 1137), (6, 1136), (7, 1136), (8, 1136), (9, 1136), (10, 1136), (11, 1136)))

  }


}
