/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.bugs.old

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraUnityBug00 extends HydraUseCase(200, 200, "unity") {

  //  override val sweep = new BCE228332C97A44169647712DD6C1C522

  val cubeName: String = frameName

  override val frameSource: String =
    s"""
	frame $frameName {
		var T2:boolean = false
		var T1_summary:boolean = false
		var T1:boolean = false
		var T2_summary:boolean = false
		cube  user {
			limit = 100
			aggregates {
				events:sum[long]
			}
			cube user.sessions {
				dimensions {
					pkey:verbatim[string]
				}
			}
		}
		user => {
			pre => 			{
				T1_summary = false
			}
			post => 			{
				if( T1_summary )
				{
					T2_summary = true
					$cubeName.events = 1
				}
			}
		}
		user.sessions.events => {
			pre => 			{
				T1 = (				notNull(user.sessions.events.parameters["one"]))
				if( T1 )
				{
					$cubeName.pkey = user.sessions.events.parameters["one"]
				}
			}
			post => 			{
				if( T1 )
				{
					T1_summary = true
					insert($cubeName)
				}
			}
		}
	}
  """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

    val names = result.resultSets(0).columnNames.zipWithIndex.toMap
    val data = result.resultSets(0).rowSet.map {
      row => (row(names("events")).asLong, if (row(names("pkey")).isNull) "(NULL)" else row(names("pkey")).asString)
    }.sortBy(_._2)

    data should equal(Array((1, "1")))

  }


}
