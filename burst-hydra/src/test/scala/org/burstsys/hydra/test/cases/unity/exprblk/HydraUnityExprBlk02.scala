/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.exprblk

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase
import org.burstsys.hydra.test.cases.unity.eql.HydraUnityCaseEql01.{analysisName, frameName}

object HydraUnityExprBlk02 extends HydraUseCase(100, 100, "unity") {

  //  override val sweep: HydraSweep = new BBF5458D702E848F6AD7D6D754199FCB1

  override val frameSource: String =
    s"""
      frame $frameName {
        cube user {
          limit = 1000
          cube user.application {
            dimensions {
              appId:verbatim[long]
            }
          }
          cube user.sessions {
            aggregates {
              sessionCount:sum[long]
            }
          }
        }

        user.application ⇒ {
          pre ⇒ {
            $analysisName.$frameName.appId = user.application.id * 2
            insert($analysisName.$frameName)
          }
        }

        user.sessions ⇒ {
          pre ⇒ {
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
    } sortBy (_._1) sortBy (_._2)
  }

  val expected: Array[Any] = Array((24690, 1), (469134, 2), (691356, 3), (913578, 4))


}
