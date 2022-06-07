/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.multivisit

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.quo.conditionals.HydraQuoConditionals02.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoMultiVisit00 extends HydraUseCase(1, 1, "quo") {

  /*
    override val sweep: HydraSweep = new BF3F4EF953B884DAD8267EE2FD51086F0

    override def serializeTraversal: Boolean = true
  */


  override val frameSource: String =
    s"""
      frame $frameName {
        cube user {
          limit = 1
          aggregates {
            userCount:sum[long]
            sessionCount:sum[long]
            eventCount:sum[long]
          }
        }

        user ⇒ {
          pre ⇒ {
            $analysisName.$frameName.userCount = 1
          }
        }

        user.sessions ⇒ {
          pre ⇒ {
            $analysisName.$frameName.sessionCount = 1
          }
        }

        user.sessions.events ⇒ {
          pre ⇒ {
            $analysisName.$frameName.eventCount = 1
          }
        }

      }
    """.stripMargin

  override def
  validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

    found(r.rowSet) should equal(expected)
  }


  def found(rowSet: Array[FabricResultRow]): Array[_] = {
    rowSet.map {
      row => (row.cells(0).asLong, row.cells(1).asLong, row.cells(2).asLong)
    }
  }

  val expected: Array[Any] = Array(
    (26,1207,164482)
  )


}
