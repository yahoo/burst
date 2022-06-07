/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.globalvars

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.quo.conditionals.HydraQuoConditionals02.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoGlobalVars01 extends HydraUseCase(1, 1, "quo") {

  //   override val sweep = new B0A89E3FC4C3F4A97AC1C2CC703CF4D00

  override def analysisSource: String =
    s"""
       |hydra $analysisName(p1:string = "cg") {
       |  schema quo
       |  frame $frameName  {
       |
       |    val gv1:long = 555 // global variable (query scoped)
       |
       |    cube user {
       |      limit = 9999
       |      cube user.sessions.events {
       |        aggregates {
       |          count:sum[long]
       |        }
       |        dimensions {
       |          num:verbatim[long]
       |        }
       |      }
       |    }
       |
       |    user.sessions.events.parameters ⇒ {
       |      situ ⇒ {
       |        $analysisName.$frameName.num = gv1
       |        if( key(user.sessions.events.parameters) == p1) {
       |          $analysisName.$frameName.count = 1
       |        }
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
      row => (row.cells(0).asLong, row.cells(1).asLong)
    }.sortBy(_._1)
  }

  val expected: Array[Any] = Array((555,14555))


}
