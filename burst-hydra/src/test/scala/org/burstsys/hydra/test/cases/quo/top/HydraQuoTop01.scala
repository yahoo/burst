/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.top

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.wave.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.quo.parameters.HydraQuoParameters01.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoTop01 extends HydraUseCase(1, 1, "quo") {

  //  override val sweep: BurstHydraSweep = new B4EA59AAA1DF4460A91BD5F45D67B3A97

  override val frameSource: String =
    s"""
       |   frame $frameName {
       |      cube user {
       |         limit = 10000
       |         aggregates {
       |            eventParameterFrequency:top[long](3 * 1, 3 * 2, 3 * 3) // scatterK, sliceK, itemK
       |         }
       |         cube user.sessions.events.parameters {
       |            dimensions {
       |               eventParameterKey:verbatim[string]
       |            }
       |         }
       |      }
       |      user.sessions.events.parameters => {
       |         situ => {
       |            $analysisName.$frameName.eventParameterKey = key(user.sessions.events.parameters)
       |            insert($analysisName.$frameName)
       |         }
       |      }
       |      user.sessions.events => {
       |         post => {
       |            $analysisName.$frameName.eventParameterFrequency = 1
       |         }
       |      }
       |}""".stripMargin

  override def
  validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

    val f = found(r.rowSet)
    f should equal(expected)
  }

  def found(rowSet: Array[FabricResultRow]): Array[_] = {
    rowSet.map {
      row => ( row.cells(0).asString, row.cells(1).asLong)
    }.sortBy(_._2).sortBy(_._1)
  }

  val expected: Array[Any] =
    Array(("Game Type",1446709), ("Game Version",1622273), ("User Level",1622273)
    )


}
