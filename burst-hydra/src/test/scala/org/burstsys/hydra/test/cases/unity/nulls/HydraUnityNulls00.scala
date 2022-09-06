/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.nulls

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase

import scala.language.postfixOps

object HydraUnityNulls00 extends HydraUseCase(200, 200, "unity") {

  //  override val sweep: BurstHydraSweep = new B71882B91BF9C45D9B97023F85E9AA0D0

  override val frameSource: String =
    s"""
       |frame $frameName {
       |  cube user {
       |    limit = 1
       |    aggregates {
       |      count1:sum[long]
       |      count2:sum[long]
       |    }
       |  }
       |  user => {
       |    pre => {
       |      if( user.application.firstUse != null ) { $analysisName.$frameName.count1 = 1 }
       |      if(  user.id == null ) { $analysisName.$frameName.count2 = 1 }
       |    }
       |  }
       |}
       |""".stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)
    found(r.rowSet) should equal(expected)
  }


  def found(rowSet: Array[FabricResultRow]): Array[_] = {
    rowSet.map {
      row => (row.cells(0).asLong, row.cells(1).asLong)
    } sortBy (_._1)
  }

  val expected: Array[Any] = Array(
    (50, 0)
  )


}
