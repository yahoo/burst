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
object HydraQuoGroupBy04 extends HydraUseCase(10, 10, "quo") {

  override val frameSource: String =
    s"""
      frame $frameName {
        cube user {
          limit = 2
          cube user.sessions {
            aggregates {
              sessions:sum[long]
            }
            dimensions {
              providedOrigin:verbatim[string]
            }
          }
        }
        user.sessions => {
          pre => {
            $analysisName.$frameName.providedOrigin = user.sessions.providedOrigin
            $analysisName.$frameName.sessions = 1
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
      row => (row.cells(0).asString, row.cells(1).asLong)
    }.sortBy(_._1)
  }

  val expected: Array[Any] = Array(
    ("ThisIsAnOrigin", 1), ("ThisIsAnOriginToo", 1)
  )

}
