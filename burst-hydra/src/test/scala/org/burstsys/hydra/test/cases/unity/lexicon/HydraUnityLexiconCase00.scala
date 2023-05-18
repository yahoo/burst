/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.lexicon

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.wave.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase

import scala.language.postfixOps

object HydraUnityLexiconCase00 extends HydraUseCase(200, 200, "unity") {

  //  override lazy val sweep = new BEBE9171C7E9F490E83DB379C007B8728
  //  override val serializeTraversal = true

  override val frameSource: String =
    s"""
       |frame $frameName {
       |  cube user {
       |    limit = 1
       |    aggregates {
       |      count:sum[long]
       |    }
       |  }
       |  user => {
       |    pre => {
       |      if(  user.id == null ) { $analysisName.$frameName.count = 1 }
       |      insert($analysisName.$frameName)
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
      row => row.cells(0).asLong
    }
  }

  val expected: Array[Any] = Array(0)

}
