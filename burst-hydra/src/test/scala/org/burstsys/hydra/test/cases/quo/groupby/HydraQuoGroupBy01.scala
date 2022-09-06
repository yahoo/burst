/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.groupby

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase

/**
 */
object HydraQuoGroupBy01 extends HydraUseCase(domain = 10, view = 10, schemaName = "quo") {

  override val frameSource: String =
    s"""
      frame $frameName {
        cube user {
          limit = 2
          cube user.sessions {
            aggregates {
              sessionCount:sum[long]
            }
            dimensions {
              appVersion:verbatim[long]
            }
          }
        }
        user.sessions => {
          pre => {
            $analysisName.$frameName.appVersion = user.sessions.appVersionId
            $analysisName.$frameName.sessionCount = 1
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
      row =>
        row.cells.map {
          cell => cell.asLong
        }
    }.sortBy(_.head)
  }

  val expected: Array[Any] = Array(Array(323232, 1), Array(545454, 1))


}
