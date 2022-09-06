/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.lexicon

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase

import scala.language.postfixOps

object HydraUnityLexiconCase02 extends HydraUseCase(200, 200, "unity") {

  //    override lazy val sweep = new B78774D0A50A94254817FD71EEBE30DCC
  //    override val serializeTraversal = true

  override val frameSource: String =
    s"""
       |frame $frameName {
       |  cube user {
       |    limit = 100
       |    dimensions {
       |      id:verbatim[string]
       |    }
       |  }
       |  user => {
       |    pre => 			{
       |      $analysisName.$frameName.id = "foo"
       |    }
       |    post => 			{
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
      row =>
        val cell = row.cells(0)
        if (cell.isNull) null else cell.asString
    }
  }

  val expected: Array[Any] = Array(null)


}
