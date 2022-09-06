/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.valuemaps

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.quo.parameters.HydraQuoParameters01.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoValueMap03 extends HydraUseCase(1, 1, "quo") {

  //  override val sweep = new BDDCEB6AB4DFB48EA8A5F0826E67ECC91

  override val frameSource: String =
    s"""

             frame $frameName {
                cube user {
                  limit = 9999
                  cube user.sessions.events {
                    aggregates {
                      eventValueCount:top[long](15)
                    }
                    dimensions {
                      eventKey:verbatim[string]
                      eventValue:verbatim[string]
                    }
                  }
                }

                user.sessions.events.parameters => {
                  situ => {
                    $analysisName.$frameName.eventKey = key(user.sessions.events.parameters)
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
      row => (row.cells(0).asString, row.cells(1).asString, row.cells(2).asLong)
    }.sortBy(_._2).sortBy(_._1)
  }

  val expected: Array[Any] = Array(
    ("Amount Spent", "200", 78), ("Chips Gained", "400", 6), ("Chips Gained", "500", 45), ("Chips Gained", "600", 8),
    ("Gained By", "Bonus collected", 45), ("Gained By", "Win", 36), ("Game Type", "Aces and Eights", 8),
    ("Game Type", "High Stakes Joker Poker", 13), ("Game Type", "Jacks Or Better", 8), ("Game Type", "Joker Poker", 9),
    ("Game Version", "1.5.0", 280), ("Result", "Three Of A Kind", 1), ("Result", "Three of a Kind", 1),
    ("User Level", "15", 79), ("User Level", "49", 71)

  )

}
