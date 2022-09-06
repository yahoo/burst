/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.offaxis

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraUnityOffAxis01 extends HydraUseCase(200, 200, "unity") {

//    override val sweep = new B50FB8E30855E467280E91B0149FFBAED

  override val frameSource: String =
    s"""
       |	frame $frameName {
       |		cube user {
       |			limit = 100
       |			dimensions {
       |				ky:verbatim[string]
       |				id:verbatim[long]
       |			}
       |		}
       |		user.sessions.events.parameters => {
       |			situ => 			{
       |				$analysisName.$frameName.ky = 				key(user.sessions.events.parameters)
       |				insert($analysisName.$frameName)
       |			}
       |		}
       |		user.sessions.events => {
       |			pre => 			{
       |				$analysisName.$frameName.id = user.sessions.events.id
       |			}
       |			post => 			{
       |			}
       |		}
       |	}
       |""".stripMargin

  override def
  validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)
    found(r.rowSet) should equal(expected)
  }
  
  def found(rowSet: Array[FabricResultRow]): Array[_] = {
    rowSet.map {
      row => (  row.cells(1).asLong, row.cells(0).asString)
    }.sortBy(_._1).sortBy(_._2)
  }

  val expected: Array[Any] = Array(
    (1,"EK1"), (2,"EK1"), (3,"EK1"), (4,"EK1"), (5,"EK1"), (6,"EK1"), (7,"EK1"), (8,"EK1"), (9,"EK1"), (10,"EK1"), (11,"EK1"),
    (1,"EK2"), (2,"EK2"), (3,"EK2"), (4,"EK2"), (5,"EK2"), (6,"EK2"), (7,"EK2"), (8,"EK2"), (9,"EK2"), (10,"EK2"), (11,"EK2"),
    (1,"EK3"), (2,"EK3"), (3,"EK3"), (4,"EK3"), (5,"EK3"), (6,"EK3"), (7,"EK3"), (8,"EK3"), (9,"EK3"), (10,"EK3"), (11,"EK3"),
    (1,"EK4"), (2,"EK4"), (3,"EK4"), (4,"EK4"), (5,"EK4"), (6,"EK4"), (7,"EK4"), (8,"EK4"), (9,"EK4"), (10,"EK4"), (11,"EK4"),
    (1,"EK5"), (2,"EK5"), (3,"EK5"), (4,"EK5"), (5,"EK5"), (6,"EK5"), (7,"EK5"), (8,"EK5"), (9,"EK5"), (10,"EK5"), (11,"EK5"),
    (1,"EK6"), (2,"EK6"), (3,"EK6"), (4,"EK6"), (5,"EK6"), (6,"EK6"), (7,"EK6"), (8,"EK6"), (9,"EK6"), (10,"EK6"), (11,"EK6"),
    (1,"EK7"), (2,"EK7"), (3,"EK7"), (4,"EK7"), (5,"EK7"), (6,"EK7"), (7,"EK7"), (8,"EK7"), (9,"EK7"), (10,"EK7"), (11,"EK7")
  )

}
