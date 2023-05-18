/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.conditional

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase
import org.burstsys.hydra.test.cases.unity.conditional.HydraUnityCase06.{analysisName, frameName}

object HydraUnityCase07 extends HydraUseCase(100, 100, "unity") {

  //  override val sweep: BurstHydraSweep = new B90197A0BF6974C9B88A832ABC56F379D

  override val frameSource: String =
    s"""
       |	frame $frameName {
       |		var T1:integer = 0
       |		cube user {
       |			limit = 100
       |			aggregates {
       |				frequency:sum[long]
       |			}
       |		}
       |		user => {
       |			post => 	{
       |				if( (T1 == 0) )	{
       |					$analysisName.$frameName.frequency = 1
       |				}
       |			}
       |		}
       |	}
       |""".stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

  }


}
