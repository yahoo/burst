/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.offaxis

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraUnityOffAxis00 extends HydraUseCase(200, 200, "unity") {

  //    override val sweep = new B50FB8E30855E467280E91B0149FFBAED

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
       |		user ⇒ {
       |			pre ⇒ 			{
       |				T1 = (user.deviceModelId == 555666)
       |				T2_summary = false
       |			}
       |			post ⇒ 			{
       |				if( T2_summary && T1 )
       |				{
       |					T1_summary = true
       |					$analysisName.$frameName.frequency = 1
       |				}
       |			}
       |		}
       |		user.application.firstUse ⇒ {
       |			pre ⇒ 			{
       |				T2 = T1
       |				if( T2 )
       |				{
       |					$analysisName.$frameName.id = user.application.firstUse.languageId
       |				}
       |			}
       |			post ⇒ 			{
       |				if( T2 )
       |				{
       |					T2_summary = true
       |					insert($analysisName.$frameName)
       |				}
       |			}
       |		}
       |	}  """.stripMargin

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

  //Array((16,0)) did not equal Array((3,111222), (3,333444), (3,555666), (4,777888), (3,888999))

  val expected: Array[Any] = Array(
    //    (3,111222), (3,333444), (3,555666), (4,777888), (3,888999)
    (111222, 3), (333444, 3), (555666, 3), (777888, 4), (888999, 3)
  )


}
