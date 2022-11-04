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
  *   users as count(user);
  * )
  * dimension (
  *   "deviceModelId" as user.deviceModelId;
  * )
  * GistCanonicalQuery2 should get the same results
  *
  * }}}
  */
object HydraQuoGroupBy02 extends HydraUseCase(11, 11, "quo") {

  override val frameSource: String =
    s"""
      frame $frameName {
        cube user {
         limit = 4
          cube user.sessions {
            aggregates {
              userCount:sum[long]
            }
            dimensions {
              deviceModel:verbatim[long]
            }
          }
        }
        user => {
          pre => {
            $analysisName.$frameName.deviceModel = user.deviceModelId
            $analysisName.$frameName.userCount = 1
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

  def found(rowSet: Array[FabricResultRow]): Array[_] =
    rowSet.map {
      row => (row.cells(0).asLong, row.cells(1).asLong)
    }.sortBy(_._1)

  val expected: Array[Any] = Array(
    (111111, 1), (22222222, 1), (33333333, 1), (44444444, 1)
  )


}
