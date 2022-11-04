/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.offaxis

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.wave.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase
import org.burstsys.hydra.test.cases.unity.offaxis.HydraUnityOffAxis02.{analysisName, frameName}

object HydraUnityOffAxis03 extends HydraUseCase(200, 200, "unity") {

  //      override val sweep = new BE72B33040ECE432D8AF3CDCBEF74F92C

  override val frameSource: String =
    s"""
       |	frame $frameName {
       |		var T1_summary:boolean = false
       |		var T1:boolean = false
       |		var T2_summary:boolean = false
       |		var T2:boolean = false
       |		cube user {
       |			limit = 100
       |			aggregates {
       |				frequency:sum[long]
       |			}
       |			cube user.application {
       |				dimensions {
       |					id:verbatim[long]
       |				}
       |			}
       |		}
       |		user => {
       |			pre => 			{
       |				T1 = (user.deviceModelId == 555666)
       |				T2_summary = false
       |			}
       |			post => 			{
       |
       |				if( T2_summary && T1 )
       |				{
       |					T1_summary = true
       |					$analysisName.$frameName.frequency = 1
       |				}
       |			}
       |		}
       |		user.application.firstUse => {
       |		pre => 			{
       |				T2 = T1
       |				if( T2 )
       |				{
       |					$analysisName.$frameName.id = user.application.firstUse.languageId
       |				}
       |			}
       |			post => 			{
       |				if( T2 )
       |				{
       |					T2_summary = true
       |					insert($analysisName.$frameName)
       |				}
       |			}
       |		}
       |	}
       |   """.stripMargin

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
    (111222, 3), (333444, 3), (555666, 3), (777888, 4), (888999, 3)
  )


}
