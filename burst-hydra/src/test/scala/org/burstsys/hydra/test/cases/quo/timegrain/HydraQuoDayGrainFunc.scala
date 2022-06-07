/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.timegrain

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.quo.conditionals.HydraQuoConditionals02.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoDayGrainFunc extends HydraUseCase(1, 1, "quo") {

  override def analysisSource: String =
    s"""
       |hydra $analysisName() {
       |  schema quo
       |  frame $frameName  {
       |    cube user {
       |      limit = ${expected.length}
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
       |        $analysisName.$frameName.grain = dayGrain(user.sessions.events.startTime)
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
    Array((1419321600000L,25244), (1419408000000L,21145), (1419494400000L,16873), (1419580800000L,36018), (1419667200000L,29246), (1419753600000L,28494), (1419840000000L,6333), (1419926400000L,4830), (1420012800000L,12990), (1420099200000L,36666), (1420185600000L,32387), (1420272000000L,32920), (1420358400000L,20150), (1420444800000L,9696), (1420531200000L,9931), (1420617600000L,7929), (1420704000000L,5602), (1420790400000L,4571), (1420876800000L,31095), (1420963200000L,23734), (1421049600000L,11654), (1421136000000L,4608), (1421222400000L,9360), (1421308800000L,7248), (1421395200000L,34097), (1421481600000L,28107), (1421568000000L,23699), (1421654400000L,37160), (1421740800000L,15797), (1421827200000L,12150), (1421913600000L,4))


}
