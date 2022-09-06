/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.now

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.quo.conditionals.HydraQuoConditionals02.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase
import org.joda.time.DateTime

/*
  */
object HydraQuoNow00 extends HydraUseCase(1, 1, "quo") {


  //    override val sweep: HydraSweep = new BA8D0554E1CDE4860BFC9B66D97B443DB

  override val frameSource: String =
    s"""
       |frame $frameName {
       |  cube user {
       |    limit = 1
       |    dimensions {
       |      'now':verbatim[long]
       |    }
       |  }
       |  user.sessions => {
       |    pre => {
       |      $analysisName.$frameName.'now' = now()
       |      insert($analysisName.$frameName)
       |    }
       |  }
       |}
       |""".stripMargin

  override def
  validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)
    r.rowCount should equal(1)

    r(0).cells.length should equal(1)

    val ticks = r(0)(0).asLong

    val now = new DateTime().withMillis(ticks)
    val current = new DateTime()
    now.isBefore(current) should be(true)

    current.minusYears(1).isBefore(now) should be(true)
    current.minusHours(1).isBefore(now) should be(true)
    current.minusMinutes(15).isBefore(now) should be(true)

    now.plusMinutes(15).isAfter(current) should be(true)
  }

  def found(rowSet: Array[FabricResultRow]): Array[_] = {
    rowSet.map {
      row => row.cells(0).asLong
    }
  }


}
