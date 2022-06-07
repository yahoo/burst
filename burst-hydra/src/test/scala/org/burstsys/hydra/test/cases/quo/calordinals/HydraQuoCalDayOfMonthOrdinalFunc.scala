/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.calordinals

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoCalDayOfMonthOrdinalFunc extends HydraUseCase(1, 1, "quo") {

  override def analysisSource: String =
    s"""
       |hydra $analysisName() {
       |  schema quo
       |  frame $frameName  {
       |    cube user {
       |      limit = 36
       |      cube user.sessions.events {
       |        aggregates {
       |          count:sum[long]
       |        }
       |        dimensions {
       |          grain:verbatim[long]
       |        }
       |      }
       |    }
       |
       |    user.sessions.events.parameters ⇒ {
       |      situ ⇒ {
       |        $analysisName.$frameName.grain = dayOfMonthOrdinal(user.sessions.events.startTime)
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
    Array((1, 190952), (2, 196366), (3, 250733), (4, 219570), (5, 211587), (6, 177198), (7, 190960), (8, 187416), (9, 183830), (10, 190255), (11, 197688), (12, 171134), (13, 150275), (14, 143530), (15, 191862), (16, 191648), (17, 230099), (18, 215385), (19, 228198), (20, 178788), (21, 147713), (22, 4), (23, 176565), (24, 188606), (25, 169910), (26, 202306), (27, 212583), (28, 210097), (29, 179755), (30, 158199), (31, 155399))


}
