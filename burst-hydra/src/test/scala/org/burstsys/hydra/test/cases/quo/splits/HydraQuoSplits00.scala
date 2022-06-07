/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.splits

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.quo.parameters.HydraQuoParameters01.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoSplits00 extends HydraUseCase(1, 1, "quo") {

  override def analysisSource: String =
    s"""
       |hydra $analysisName() {
       |  schema quo
       |  frame $frameName  {
       |
       |    var gv1:long = 0
       |
       |    cube user {
       |      limit = 36
       |      cube user.sessions.events {
       |        aggregates {
       |          a1:sum[long]
       |        }
       |        dimensions {
       |          d1:split[long](0, 10, 100, 1000)
       |        }
       |      }
       |    }
       |
       |    user.sessions.events ⇒ {
       |      pre ⇒ {
       |        $analysisName.$frameName.d1 = gv1
       |        $analysisName.$frameName.a1 = 1
       |        gv1 = gv1 + 2
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
    Array((0,111), (10,900), (100,8283), (9223372036854775807L,155188))


}
