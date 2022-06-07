/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.subcubes

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.quo.parameters.HydraQuoParameters01.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

/**
  * {{{
  * over ("quo", 1)
  * aggregate (
  *   eventParameterFrequency as count(user.sessions.events);
  * )
  * dimension (
  *   "eventId" as user.sessions.events.eventId,
  *   "eventParameterKey" as user.sessions.events.parameters.key;
  * )
  * }}}
  */
object HydraQuoSubCubes00 extends HydraUseCase(10, 10, "quo") {

  //  override val sweep: BurstHydraSweep = new B4EA59AAA1DF4460A91BD5F45D67B3A97

  override val frameSource: String =
    s"""
      frame $frameName {
        cube user {
          limit = 12
          cube user.sessions.events {
            aggregates {
              eventParameterFrequency:sum[long]
            }
            dimensions {
              eventId:verbatim[long]
              eventParameterKey:verbatim[string]
            }
          }
        }
        user.sessions.events.parameters ⇒ {
          situ ⇒ {
            $analysisName.$frameName.eventId = user.sessions.events.eventId
            $analysisName.$frameName.eventParameterKey = key(user.sessions.events.parameters)
            $analysisName.$frameName.eventParameterFrequency = 1
          }
        }
      }
    """.stripMargin

  override def
  validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

    val f = found(r.rowSet)
    f should equal(expected)
  }

  def found(rowSet: Array[FabricResultRow]): Array[_] = {
    rowSet.map {
      row => (row.cells(0).asLong, row.cells(1).asString, row.cells(2).asLong.toInt)
    }.sortBy(_._2).sortBy(_._1)
  }

  val expected: Array[Any] = Array(
    (123456, "k1", 1),
    (123456, "k10", 1),
    (123456, "k2", 1),
    (123456, "k9", 1),
    (123457, "k11", 1),
    (123457, "k12", 1),
    (123457, "k3", 1),
    (123457, "k4", 1),
    (123457, "k5", 1),
    (123457, "k6", 1),
    (123457, "k7", 1),
    (123457, "k8", 1)
  )


}
