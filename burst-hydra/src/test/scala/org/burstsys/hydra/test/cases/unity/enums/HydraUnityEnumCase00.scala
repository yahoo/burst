/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.enums

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase
import org.burstsys.hydra.test.cases.unity.eql.HydraUnityCaseEql01.{analysisName, frameName}

import scala.language.postfixOps

object HydraUnityEnumCase00 extends HydraUseCase(200, 200, "unity") {

  //  override lazy val sweep = new BEBE9171C7E9F490E83DB379C007B8728
  //  override val serializeTraversal = true

  override val frameSource: String =
    s"""
       |frame $frameName {
       |  cube user {
       |    limit = 10
       |    dimensions {
       |      symbol:verbatim[long]
       |    }
       |  }
       |  user => {
       |    pre => {
       |      $analysisName.$frameName.symbol = enum(4L, 6L, 8L, 4L)
       |      insert($analysisName.$frameName)
       |      $analysisName.$frameName.symbol = enum(4L, 6L, 8L, 6L)
       |      insert($analysisName.$frameName)
       |      $analysisName.$frameName.symbol = enum(4L, 6L, 8L, 3L)
       |      insert($analysisName.$frameName)
       |    }
       |  }
       |}
       |""".stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)
    r.rowSet.map {
      row => row.cells(0).asLong
    } should equal(
      Array(4, 6, 8)
    )
  }

}
