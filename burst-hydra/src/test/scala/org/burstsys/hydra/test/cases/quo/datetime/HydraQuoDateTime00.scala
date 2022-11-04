/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.datetime

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.wave.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.quo.conditionals.HydraQuoConditionals02.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoDateTime00 extends HydraUseCase(1, 1, "quo") {

  //    override val sweep = new BBC4D567451624479BB231FAC3C11E12C

  override val frameSource: String =
    s"""
       | frame $frameName {
       |   cube user {
       |     limit = 1
       |     cube user.sessions {
       |       dimensions {
       |         'datetime':verbatim[long]
       |       }
       |     }
       |   }
       |   user => {
       |     pre => {
       |       $analysisName.$frameName.'datetime' = datetime("2012-01-19T19:00:00-05:00")
       |       insert($analysisName.$frameName)
       |     }
       |   }
       | }
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
    }.sorted
  }

  val expected: Array[Any] = Array(1327017600000L)


}
