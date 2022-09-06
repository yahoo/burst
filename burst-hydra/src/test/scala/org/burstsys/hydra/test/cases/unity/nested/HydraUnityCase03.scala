/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.nested

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase
import org.burstsys.hydra.test.cases.unity.bugs.old.HydraUnityBug05.{analysisName, frameName}

object HydraUnityCase03 extends HydraUseCase(100, 100, "unity") {

  //  override val sweep: BurstHydraSweep = new B90197A0BF6974C9B88A832ABC56F379D

  override val frameSource: String =
    s"""
      frame $frameName {
        cube user {
         limit = 4
          cube user.application {
            dimensions {
              appVersion:verbatim[long]
            }
          }
          cube user.sessions {
            aggregates {
              sessionCount:sum[long]
            }
          }
        }
        user.application => {
          pre => {
            $analysisName.$frameName.appVersion = user.application.id
            insert($analysisName.$frameName)
          }
        }
        user.sessions => {
          pre => {
            $analysisName.$frameName.sessionCount = 1
          }
        }
      }
    """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)
    found(r.rowSet) should equal(expected)

  }

  def found(rowSet: Array[FabricResultRow]): Array[_] = {
    rowSet.map {
      row => (row.cells(0).asLong, row.cells(1).asLong)
    } sortBy (_._1)
  }

  val expected: Array[Any] = Array(
    (12345, 1),
    (234567, 2),
    (345678, 3),
    (456789, 4)
  )


}
