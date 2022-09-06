/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.eql

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraUnityCaseEql01 extends HydraUseCase(200, 200, "unity") {

  // override val sweep: HydraSweep = new B15AE977E768E4F6B8F2D859BF50E1007

  override val analysisSource: String =
    s"""
       |hydra $analysisName() {
       |	schema unity
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
       |			cube user.sessions {
       |				dimensions {
       |					id:verbatim[long]
       |				}
       |			}
       |		}
       |		user => {
       |			pre => 			{
       |				T1 = (user.application.firstUse.languageId != 555666L)
       |				T2_summary = false
       |			}
       |			post => 			{
       |				if( T2_summary && T1 ) {
       |					T1_summary = true
       |					$analysisName.$frameName.frequency = 1L
       |				}
       |			}
       |		}
       |		user.sessions.events => {
       |			pre => 			{
       |				T2 = T1
       |				if( T2 ) {
       |					$analysisName.$frameName.id = user.sessions.events.id
       |				}
       |			}
       |			post => 			{
       |				if( T2 ) {
       |					T2_summary = true
       |					insert($analysisName.$frameName)
       |				}
       |			}
       |		}
       |	}
       |}     """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames("$queryName"))
    assertLimits(r)

    val found = r.rowSet.map {
      row => (row.cells(0).asLong, row.cells(1).asLong)
    }.sortBy(_._1)
    //    found should equal(Array((9876, 416), (54321, 417), (54329, 417)))
    found should equal(
      Array((1, 40), (2, 40), (3, 40), (4, 40), (5, 40), (6, 40), (7, 40), (8, 40), (9, 40), (10, 40), (11, 40))
    )

  }

}
