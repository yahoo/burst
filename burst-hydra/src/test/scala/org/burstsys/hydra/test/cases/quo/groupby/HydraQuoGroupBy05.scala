/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.groupby

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.wave.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.quo.conditionals.HydraQuoConditionals02.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

/**
  * {{{
  * over ("quo", 1)
  * aggregate (
  *     sessions as count(user.sessions);
  * )
  * dimension (
  *     "osVersionId" as user.sessions.osVersion;
  * )
  * }}}
  */
object HydraQuoGroupBy05 extends HydraUseCase(10, 10, "quo") {

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
                osVersion:verbatim[long]
              }
            }
          }
          user.sessions => {
            pre => {
              $analysisName.$frameName.osVersion = user.sessions.osVersion
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

  val expected: Array[Any] = Array(
    Array(4505050505L, 1), Array(54545445454L, 1)
  )

}
