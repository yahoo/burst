/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.valuemaps

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.quo.parameters.HydraQuoParameters01.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoValueMap01 extends HydraUseCase(1, 1, "quo") {

  //  override val sweep = new BDDCEB6AB4DFB48EA8A5F0826E67ECC91

  override val frameSource: String =
    s"""
             frame $frameName {
                cube user {
                  limit = 1000
                  cube user.sessions.events {
                    aggregates {
                      eventValueCount:top[long](10)
                    }
                    dimensions {
                      eventValue:verbatim[string]
                    }
                  }
                }

                user.sessions.events.parameters ⇒ {
                  situ ⇒ {
                    $analysisName.$frameName.eventValue = value(user.sessions.events.parameters)
                    $analysisName.$frameName.eventValueCount = 1
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
    } sortBy (_._1)
  }

  val expected: Array[Any] = Array(
    ("1.5.0", 628),
    ("15", 79),
    ("400", 1),
    ("49", 49),
    ("500", 45),
    ("Bonus collected", 45),
    ("Joker Poker", 9),
    ("Lose", 6),
    ("Three Of A Kind", 1),
    ("Win", 1)

  )

}
