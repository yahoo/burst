/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.convert

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.wave.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.quo.parameters.HydraQuoParameters01.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoStringToByteCast extends HydraUseCase(1, 1, "quo") {

//  override lazy val sweep: HydraSweep = new BD3D86F156175474DAF5A27C56C794AE7

  override def analysisSource: String =
    s"""
       |hydra $analysisName() {
       |   schema 'quo'
       |   frame $frameName {
       |      var T1_summary:boolean=false
       |      var T1:boolean=false
       |      cube user {
       |         limit = 1
       |         dimensions {
       |            id:verbatim[byte]
       |         }
       |      }
       |      user => {
       |         pre => {
       |            T1 = true
       |            if (T1) {
       |               $analysisName.$frameName.id = cast("12" as byte)
       |            }
       |         }
       |         post => {
       |            if (T1) {
       |               T1_summary = true
       |               insert($analysisName.$frameName)
       |            }
       |         }
       |      }
       |   }
       |}
       |     """.stripMargin

  override def
  validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)
    found(r.rowSet) should equal(expected)
  }


  def found(rowSet: Array[FabricResultRow]): Array[_] = {
    rowSet.map {
      row => row.cells(0).asByte
    }.sorted
  }

  val expected: Array[Any] = Array(12)

}
