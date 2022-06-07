/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.enums

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.quo.conditionals.HydraQuoConditionals02.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoEnums00 extends HydraUseCase(1, 1, "quo") {

  override def analysisSource: String =
    s"""
       |hydra $analysisName() {
       |  schema quo
       |  frame $frameName  {
       |    cube user {
       |      limit = 10000
       |      cube user.sessions.events {
       |        aggregates {
       |          a1:top[long](5)
       |        }
       |        dimensions {
       |          d1:enum[string]("sc", "cg", "as", "Other")
       |        }
       |      }
       |    }
       |
       |    user.sessions.events.parameters ⇒ {
       |      situ ⇒ {
       |        $analysisName.$frameName.d1 = key(user.sessions.events.parameters)
       |        $analysisName.$frameName.a1 = 1
       |      }
       |    }
       |
       |  }
       |}
     """.stripMargin

  override def
  validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)
    found(r.rowSet) should equal(expected)
  }


  def found(rowSet: Array[FabricResultRow]): Array[_] = {
    rowSet.map {
      row => (row.cells(0).asString, row.cells(1).asLong)
    }.sortBy(_._2)
  }

  val expected: Array[Any] =
    Array(("sc",2885), ("cg",14555), ("as",33119), ("Other",529179))

}
