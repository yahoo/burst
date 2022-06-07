/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.groupby

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.quo.conditionals.HydraQuoConditionals02.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

/**
  * {{{
  * over ("quo", 1)
  * aggregate (
  *   eventCount as count(user.sessions.events);
  * )
  * dimension (
  *   "eventId" as user.sessions.events.eventId;
  * )
  * }}}
  */
object HydraQuoGroupBy07 extends HydraUseCase(1, 1, "quo") {

  override val frameSource: String =
    s"""
       frame $frameName {
          cube user {
            limit = 15
            cube user.sessions.events {
              aggregates {
                eventCount:sum[long]
              }
              dimensions {
                eventId:verbatim[long]
              }
            }
          }
          user.sessions.events ⇒ {
            pre ⇒ {
              $analysisName.$frameName.eventId = user.sessions.events.eventId
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

  def found(rowSet: Array[FabricResultRow]): Array[_] =
    rowSet.map {
      row =>
        row.cells.map {
          cell => cell.asLong
        }
    }.sortBy(_.head)

  val expected: Array[Any] = Array(Array(4498113, 297), Array(4498114, 1122), Array(4498115, 821), Array(4498116, 33119), Array(4498117, 33110), Array(4498118, 33099), Array(4498119, 14555), Array(4498120, 13434), Array(4498121, 29937), Array(4498122, 2885), Array(4498123, 1301), Array(4498124, 14), Array(4498126, 390), Array(5555423, 119), Array(6049337, 279))

  Array(
    Array(4498113, 3259), Array(4498114, 22053), Array(4498115, 14442), Array(4498116, 323619),
    Array(4498117, 323564), Array(4498118, 323440), Array(4498119, 149417), Array(4498120, 131209),
    Array(4498121, 286501), Array(4498122, 24750), Array(4498123, 11242), Array(4498124, 51),
    Array(4498126, 4150), Array(5555423, 655), Array(6049337, 2751)
  )

}
