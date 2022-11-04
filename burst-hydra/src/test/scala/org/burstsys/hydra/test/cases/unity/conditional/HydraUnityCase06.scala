/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.conditional

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraUnityCase06 extends HydraUseCase(100, 100, "unity") {

  //  override val sweep: BurstHydraSweep = new B90197A0BF6974C9B88A832ABC56F379D

  override val frameSource: String =
    s"""
       |frame $frameName {
       |	var T1_summary:boolean = false
       |	var T2:boolean = false
       |	var T1:boolean = false
       |	cube user {
       |		limit = 100
       |		aggregates {
       |			frequency:sum[long]
       |		}
       |		cube user.sessions {
       |			dimensions {
       |				id:verbatim[long]
       |			}
       |		}
       |	}
       |	user => {
       |		pre => 			{
       |			T1 = (user.deviceModelId == 555666)
       |		}
       |		post => 			{
       |			if( T1 )
       |			{
       |				T1_summary = true
       |				$analysisName.$frameName.frequency = 1
       |			}
       |		}
       |	}
       |	user.sessions.events => {
       |		pre => 			{
       |			T2 = T1
       |			if( T2 )
       |			{
       |				$analysisName.$frameName.id = user.sessions.events.id
       |			}
       |		}
       |		post => 			{
       |			if( T2 )
       |			{
       |				insert($analysisName.$frameName)
       |			}
       |		}
       |	}
       |}""".stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

  }


}
