/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.offaxis

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoOffAxis01 extends HydraUseCase(1, 1, "quo") {

  //  override val sweep = new BDDCEB6AB4DFB48EA8A5F0826E67ECC91

  override val frameSource: String =
    s"""
       frame $frameName {
          cube user {
            limit = 64
            cube user.sessions.events {
                aggregates {
                  eventCount:sum[long]
                }
            }
            cube user.personas {
              dimensions {
                persona:verbatim[long]
              }
            }
          }

          user.personas => {
             post => {
                $analysisName.$frameName.persona = user.personas.personaId
                insert($analysisName.$frameName)
             }
          }

          user.sessions.events => {
            post => {
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
      row => (row.cells(0).asLong, row.cells(1).asLong)
    }.sortBy(_._1)
  }

  val expected: Array[Any] = Array(

  )


}
