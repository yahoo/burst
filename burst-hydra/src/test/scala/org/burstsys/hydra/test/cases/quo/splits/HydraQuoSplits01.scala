/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.splits

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.quo.parameters.HydraQuoParameters01.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoSplits01 extends HydraUseCase(1, 1, "quo") {

  override def frameSource: String =
    s"""
       |  frame $frameName  {
       |
       |    var gv1:long = 0
       |
       |    cube user {
       |      limit = 36
       |      cube user.sessions {
       |        dimensions {
       |          d1:verbatim[string]
       |        }
       |      }
       |    }
       |
       |    user.sessions.events => {
       |      pre => {
       |        $analysisName.$frameName.d1 = split(1, 2, 3, 5)
       |      }
       |    }
       |
       |  }
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
    Array((0, 1696), (10, 14258), (100, 112111), (9223372036854775807L, 1494208))


}
