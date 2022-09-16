/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.convert

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.quo.conditionals.HydraQuoConditionals02.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoLongToStringCast extends HydraUseCase(1, 1, "quo") {

  //  override lazy val sweep: HydraSweep = new BD3D86F156175474DAF5A27C56C794AE7
  // override val serializeTraversal = true

  override def frameSource: String =
    s"""
       |  frame $frameName  {
       |    cube user {
       |      limit = 999999
       |      dimensions {
       |        'cast':verbatim[string]
       |      }
       |    }
       |    user.sessions.events => {
       |      pre => {
       |        $analysisName.$frameName.'cast' = cast( user.sessions.events.eventId as string )
       |        insert( $analysisName.$frameName )
       |      }
       |    }
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
      row => row.cells(0).asString
    }.sorted
  }

  val expected: Array[Any] =
    Array(
      "4498113", "4498114", "4498115", "4498116", "4498117", "4498118", "4498119", "4498120", "4498121",
      "4498122", "4498123", "4498124", "4498126", "5555423", "6049337"
    )

}
