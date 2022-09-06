/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.offaxis

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraUnityOffAxis02 extends HydraUseCase(100, 100, "unity") {

  //    override val sweep = new BC2340EAFACBE4CF2B3B24652AA475691

  override val frameSource: String =
    s"""
       |	frame $frameName {
       |		var T1_summary:boolean = false
       |		var T1:boolean = false
       |		cube user {
       |			limit = 100
       |			aggregates {
       |				projects:sum[long]
       |			}
       |			cube user.application {
       |				dimensions {
       |					languageId:verbatim[long]
       |				}
       |			}
       |		}
       |		user => {
       |			pre => 			{
       |				T1 = true
       |			}
       |			post => 			{
       |				if( T1 )
       |				{
       |					T1_summary = true
       |					$analysisName.$frameName.projects = 1
       |				}
       |			}
       |		}
       |		user.application.firstUse => {
       |			pre => 			{
       |				$analysisName.$frameName.languageId = user.application.firstUse.languageId
       |			}
       |			post => 			{
       |				insert($analysisName.$frameName)
       |			}
       |		}
       |	}
       |       """.stripMargin

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
    (0, 4)
  )


}
