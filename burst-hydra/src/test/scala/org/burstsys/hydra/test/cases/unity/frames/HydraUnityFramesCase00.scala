/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.frames

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraUnityFramesCase00 extends HydraUseCase(100, 100, "unity") {

  //    override val sweep = new BC8EC3D2294774BEDA5347FFDC7CA8625

  override def analysisSource: String =
    s"""
       |hydra $analysisName(p1:long = 0) {
       |  schema unity
       |  frame $frameName {
       |    cube user {
       |      limit = 1
       |      aggregates {
       |        tally1:sum[long]
       |      }
       |      dimensions {
       |        keys1:verbatim[integer]
       |      }
       |      cube user.sessions {
       |        aggregates {
       |          tally2:sum[long]
       |        }
       |        dimensions {
       |          keys2:verbatim[integer]
       |        }
       |      }
       |    } // end cube
       |
       |    user.sessions  => {
       |      post => {
       |        $analysisName.$frameName.tally1 = 1 // cube aggregation reference (absolute)
       |        $analysisName.$frameName.tally2 = 1 // cube aggregation reference (absolute)
       |        $analysisName.$frameName.keys1 = 1 // cube dimension reference (relative)
       |        $analysisName.$frameName.keys2 = 1 // cube dimension reference (relative)
       |        insert($analysisName.$frameName) // cube reference (absolute)
       |        insert($analysisName.$frameName) // cube reference (relative)
       |      }
       |    } // end visit
       |
       |  } // end frame
       |
       |} // end analysis
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
