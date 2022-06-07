/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.convert

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.quo.conditionals.HydraQuoConditionals02.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoLongToDoubleCast extends HydraUseCase(1, 1, "quo") {

  //  override lazy val sweep: HydraSweep = new BD3D86F156175474DAF5A27C56C794AE7

  override def analysisSource: String =
    s"""
       |hydra $analysisName() {
       |  schema quo
       |  frame $frameName  {
       |    cube user {
       |      limit = 999999
       |      dimensions {
       |        'cast':verbatim[double]
       |      }
       |    }
       |
       |    user.sessions.events ⇒ {
       |      pre ⇒ {
       |        $analysisName.$frameName.'cast' = cast( user.sessions.events.eventId as double )
       |        insert($analysisName.$frameName)
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
      row => row.cells(0).asDouble
    }.sorted
  }

  val expected: Array[Any] =
    Array(
      4498113.0, 4498114.0, 4498115.0, 4498116.0, 4498117.0, 4498118.0, 4498119.0, 4498120.0, 4498121.0,
      4498122.0, 4498123.0, 4498124.0, 4498126.0, 5555423.0, 6049337.0
    )


}
