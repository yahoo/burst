/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.calordinals

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.wave.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.quo.calordinals.HydraQuoCalDayOfYearOrdinal.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoCalDayOfYearOrdinalFunc extends HydraUseCase(1, 1, "quo") {

  override def analysisSource: String =
    s"""
       |hydra $analysisName() {
       |  schema quo
       |  frame $frameName  {
       |    cube user {
       |      limit = 365
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
       |    user.sessions.events.parameters => {
       |      situ => {
       |        $analysisName.$frameName.grain = dayOfYearOrdinal(user.sessions.events.startTime)
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
    Array((1, 190948), (2, 196366), (3, 250733), (4, 219570), (5, 211587), (6, 177198), (7, 190960), (8, 187416), (9, 183830), (10, 190255), (11, 197688), (12, 171134), (13, 150275), (14, 143530), (15, 191862), (16, 191648), (17, 230099), (18, 215385), (19, 228198), (20, 178788), (21, 147713), (22, 4), (32, 4), (357, 176565), (358, 188606), (359, 169910), (360, 202306), (361, 212583), (362, 210097), (363, 179755), (364, 158199), (365, 155399))


}
