/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.exprblk

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.wave.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase
import org.burstsys.hydra.test.cases.unity.eql.HydraUnityCaseEql01.{analysisName, frameName}

import scala.language.postfixOps

object HydraUnityExprBlk03 extends HydraUseCase(200, 200, "unity") {

  //  override val sweep = new BF9813F2190CF4DCDBB97ED7CF0D366A8

  override val frameSource: String =
    s"""
       |frame $frameName {
       |  cube user {
       |    limit = 1000
       |    aggregates {
       |      count:sum[long]
       |    }
       |  }
       |  user.sessions.events => {
       |    pre => {
       |        if(true) {
       |          $analysisName.$frameName.count = 1
       |        } else if(false) {
       |          $analysisName.$frameName.count = 2
       |        } else {
       |          $analysisName.$frameName.count = 3
       |        }
       |        insert($analysisName.$frameName)
       |    }
       |  }
       |}""".stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)
    found(r.rowSet) should equal(expected)
  }

  def found(rowSet: Array[FabricResultRow]): Array[_] = {
    rowSet.map {
      row => row.cells(0).asLong
    } sorted
  }

  val expected: Array[Any] = Array(12500)


}
