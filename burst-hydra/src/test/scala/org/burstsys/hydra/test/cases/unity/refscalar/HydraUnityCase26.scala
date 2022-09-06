/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.refscalar

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase
import org.burstsys.hydra.test.cases.unity.conditional.HydraUnityCase06.{analysisName, frameName}

object HydraUnityCase26 extends HydraUseCase(150, 150, "unity") {

  //  override val sweep = new B53FDFE4FB84E470E8921ACA88A86BDDC

  override val frameSource: String =

    s"""|frame $frameName {
        |  cube user {
        |    limit = 4
        |    dimensions {
        |      d1:verbatim[long]
        |      d2:verbatim[long]
        |      d3:verbatim[long]
        |      d4:verbatim[long]
        |      d5:verbatim[long]
        |    }
        |  }
        |  user.sessions => {
        |    pre => {
        |      $analysisName.$frameName.d1 = user.application.id
        |      $analysisName.$frameName.d2 = user.application.firstUse.appVersion.id
        |      $analysisName.$frameName.d3 = user.application.mostUse.appVersion.id
        |      $analysisName.$frameName.d4 = user.application.lastUse.appVersion.id
        |      $analysisName.$frameName.d5 = user.sessions.appVersion.id
        |      insert($analysisName.$frameName)
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
      row => (row.cells(0).asLong, row.cells(1).asLong, row.cells(2).asLong, row.cells(3).asLong, row.cells(4).asLong)
    } sortBy (_._1) sortBy (_._2) sortBy (_._3) sortBy (_._4) sortBy (_._5)
  }

  val expected: Array[Any] = Array(
    (12345, 123, 789, 456, 1010),
    (12345, 123, 789, 456, 1111)
  )


}
