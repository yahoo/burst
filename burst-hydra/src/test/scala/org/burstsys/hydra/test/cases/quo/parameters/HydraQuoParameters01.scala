/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.parameters

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.wave.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoParameters01 extends HydraUseCase(1, 1, "quo", executionCount = 1.toInt) {

  //      override val sweep = new B862553156F274687A3DF83BB713A12BE // lexicon
  //  override val sweep = new BCC0B4FD604214DFA82D026A0672C0054 // no lexicon
  override val serializeTraversal = true


  override def analysisSource: String =
    s"""
       |hydra $analysisName(p1:string = "cg") {
       |  schema $schemaName
       |  frame $frameName  {
       |    cube user {
       |      limit = 9999
       |      cube user.sessions.events {
       |        aggregates {
       |          hitCount:sum[long]
       |          missCount:sum[long]
       |        }
       |      }
       |    }
       |    user.sessions.events.parameters => {
       |      situ => {
       |        if( key(user.sessions.events.parameters) == p1) {
       |          $analysisName.$frameName.hitCount = 1
       |        } else {
       |          $analysisName.$frameName.missCount = 1
       |        }
       |      }
       |    }
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
    }
  }

  val expected: Array[Any] =
    Array((14555,565183)) //  no lexicon


}
