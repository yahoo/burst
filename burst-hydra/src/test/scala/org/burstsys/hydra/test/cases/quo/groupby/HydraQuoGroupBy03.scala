/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.groupby

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.wave.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.quo.conditionals.HydraQuoConditionals02.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoGroupBy03 extends HydraUseCase(1, 1, "quo") {

  //  override val sweep: HydraSweep = new B537BE28A89DE413788734B67B4A149A6

  override def analysisSource: String =
    s"""
       |hydra $analysisName() {
       |   schema 'quo'
       |   frame $frameName {
       |     cube user {
       |         limit = 15
       |            aggregates {
       |              eventCount:sum[long]
       |            }
       |            dimensions {
       |              eventId:verbatim[long]
       |            }
       |      }
       |      user.sessions.events => {
       |         pre => {
       |            $analysisName.$frameName.eventId = user.sessions.events.eventId
       |            $analysisName.$frameName.eventCount = 1
       |         }
       |      }
       |   }
       |}
       """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)
    found(r.rowSet) should equal(expected)
  }

  def found(rowSet: Array[FabricResultRow]): Array[_] =
    rowSet.map {
      row => (row.cells(0).asLong, row.cells(1).asLong)
    }.sortBy(_._1)

  val expected: Array[Any] = Array((4498113,297), (4498114,1122), (4498115,821), (4498116,33119), (4498117,33110), (4498118,33099), (4498119,14555), (4498120,13434), (4498121,29937), (4498122,2885), (4498123,1301), (4498124,14), (4498126,390), (5555423,119), (6049337,279))



}
