/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.calordinals

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.wave.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.quo.calgrain.HydraQuoCalDayGrainFunc.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoCalDayOfWeekOrdinal extends HydraUseCase(1, 1, "quo") {

  override def analysisSource: String =
    s"""
       |hydra $analysisName() {
       |  schema quo
       |  frame $frameName  {
       |    cube user {
       |      limit =7
       |      cube user.sessions.events {
       |        aggregates {
       |          count:sum[long]
       |        }
       |        dimensions {
       |          grain:dayOfWeekOrdinal[long]
       |        }
       |      }
       |    }
       |
       |    user.sessions.events.parameters => {
       |      situ => {
       |        $analysisName.$frameName.grain = user.sessions.events.startTime
       |        $analysisName.$frameName.count = 1
       |      }
       |    }
       |
       |  }
       |}
     """.stripMargin

  override def
  validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)
    found(r.rowSet) should equal(expected)
  }


  def found(rowSet: Array[FabricResultRow]): Array[_] = {
    rowSet.map {
      row => (row.cells(0).asLong, row.cells(1).asLong)
    }.sortBy(_._1)
  }

  val expected: Array[Any] =
    Array((1, 790674), (2, 841025), (3, 826208), (4, 740140), (5, 774150), (6, 883670), (7, 842744))


}
