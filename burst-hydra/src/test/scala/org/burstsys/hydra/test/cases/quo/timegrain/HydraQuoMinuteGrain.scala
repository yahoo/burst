/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.timegrain

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.quo.conditionals.HydraQuoConditionals02.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

/**
  * takes too long
  */
object HydraQuoMinuteGrain extends HydraUseCase(1, 1, "quo") {

  override def analysisSource: String =
    s"""
       |hydra $analysisName() {
       |  schema quo
       |  frame $frameName  {
       |    cube user {
       |      limit = 9999999
       |      cube user.sessions.events {
       |        aggregates {
       |          count:sum[long]
       |        }
       |        dimensions {
       |          grain:minuteGrain[long]
       |        }
       |      }
       |    }
       |
       |    user.sessions.events.parameters ⇒ {
       |      situ ⇒ {
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
    found(r.rowSet).take(50) should equal(expected)
  }


  def found(rowSet: Array[FabricResultRow]): Array[_] = {
    rowSet.map {
      row => (row.cells(0).asLong, row.cells(1).asLong)
    }.sortBy(_._1)
  }

  val expected: Array[Any] =
    Array(
      (1419321780000L, 74), (1419321840000L, 93), (1419321900000L, 114), (1419321960000L, 107), (1419322020000L, 106),
      (1419322080000L, 6), (1419322200000L, 2), (1419322380000L, 36), (1419322440000L, 81), (1419322500000L, 93),
      (1419322560000L, 52), (1419322620000L, 64), (1419322680000L, 86), (1419322740000L, 80), (1419322800000L, 53),
      (1419322980000L, 6), (1419323040000L, 6), (1419323280000L, 6), (1419323340000L, 63), (1419323460000L, 10),
      (1419323520000L, 77), (1419323580000L, 94), (1419323640000L, 44), (1419323700000L, 6), (1419323760000L, 4),
      (1419324000000L, 118), (1419324060000L, 88), (1419324120000L, 57), (1419324180000L, 154), (1419324240000L, 80),
      (1419324300000L, 70), (1419324360000L, 38), (1419324540000L, 6), (1419324600000L, 8), (1419325500000L, 152),
      (1419325560000L, 202), (1419325620000L, 274), (1419325680000L, 280), (1419325740000L, 196), (1419325800000L, 102),
      (1419325860000L, 88), (1419325920000L, 98), (1419325980000L, 103), (1419326040000L, 180), (1419326100000L, 193),
      (1419326160000L, 91), (1419326220000L, 119), (1419326280000L, 141), (1419326340000L, 200), (1419326400000L, 28)
    )


}
