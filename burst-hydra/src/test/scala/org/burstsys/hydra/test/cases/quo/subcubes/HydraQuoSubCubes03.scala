/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.subcubes

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.wave.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.quo.parameters.HydraQuoParameters01.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

/**
  * {{{
  * over ("quo", 1)
  * aggregate (
  *     sessions as count(user.sessions);
  * )
  * dimension (
  *     "originMethodType" as user.sessions.originMethodType;
  * )
  * }}}
  */
object HydraQuoSubCubes03 extends HydraUseCase(1, 1, "quo") {

  override val frameSource: String =
    s"""
         frame $frameName {
            cube user {
              limit = 34
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

  def found(rowSet: Array[FabricResultRow]): Array[_] = {
    rowSet.map {
      row =>
        row.cells.map {
          cell => cell.asLong
        }
    }.sortBy(_.head)
  }

  val expected: Array[Any] = Array(Array(0, 1), Array(5698, 1), Array(5923, 5), Array(5924, 5), Array(5958, 3), Array(5959, 1), Array(6461, 2), Array(6462, 1), Array(6595, 3), Array(6598, 1), Array(6802, 2), Array(6804, 1))

}
