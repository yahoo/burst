/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.lexicon

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraUnityLexiconSpec01 extends HydraUseCase(200, 200, "unity") {

  //  override lazy val sweep = new BEBE9171C7E9F490E83DB379C007B8728
  //  override val serializeTraversal = true

  override def frameSource: String =
    s"""
       |  frame $frameName  {
       |    cube user {
       |      limit = 999999
       |      dimensions {
       |        pKeys:verbatim[string]
       |      }
       |    }
       |    user.sessions.events.parameters ⇒ {
       |      situ ⇒ {
       |        $analysisName.$frameName.pKeys =  value(user.sessions.events.parameters)
       |        insert($analysisName.$frameName)
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
    Array("EV1", "EV2", "EV3", "EV4", "EV5", "EV6", "EV7")

}
