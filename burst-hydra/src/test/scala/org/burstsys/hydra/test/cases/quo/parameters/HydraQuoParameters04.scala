/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.parameters

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.quo.parameters.HydraQuoParameters01.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoParameters04 extends HydraUseCase(1, 1, "quo" /*, executionCount = 1e6.toInt*/) {

  //  override val sweep = new BD6800F1F086948D992091BCE1EF46A82

  override def analysisSource: String =
    s"""
       |hydra $analysisName() {
       |  schema $schemaName
       |  frame $frameName  {
       |    cube user {
       |      limit = 1
       |      cube user.sessions.events {
       |        aggregates {
       |          count:sum[long]
       |        }
       |      }
       |    }
       |    user.sessions.events => {
       |      pre => {
       |        if( user.sessions.events.parameters["ul"] == "1") {
       |          $analysisName.$frameName.count = 1
       |        }
       |      }
       |    }
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
      row => row.cells(0).asLong
    }
  }

  val expected: Array[Any] = Array(671)


}
