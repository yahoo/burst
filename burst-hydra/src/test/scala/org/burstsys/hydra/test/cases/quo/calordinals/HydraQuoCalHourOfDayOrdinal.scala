/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.calordinals

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.wave.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.quo.calordinals.HydraQuoCalDayOfYearOrdinal.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoCalHourOfDayOrdinal extends HydraUseCase(1, 1, "quo") {

  override def analysisSource: String =
    s"""
       |hydra $analysisName() {
       |  schema quo
       |  frame $frameName  {
       |    cube user {
       |      limit = 24
       |      cube user.sessions.events {
       |        aggregates {
       |          count:sum[long]
       |        }
       |        dimensions {
       |          grain:hourOfDayOrdinal[long]
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
    Array((0, 136588), (1, 117179), (2, 123913), (3, 112457), (4, 168953), (5, 161084), (6, 217041), (7, 232552), (8, 287365), (9, 310046), (10, 328308), (11, 316435), (12, 341107), (13, 379898), (14, 291684), (15, 319139), (16, 313117), (17, 320946), (18, 290786), (19, 256260), (20, 243753), (21, 175976), (22, 127819), (23, 126205))


}
