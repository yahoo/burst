/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.valuemaps

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.quo.parameters.HydraQuoParameters01.{analysisName, frameName}
import org.burstsys.hydra.test.cases.quo.valuemaps.HydraQuoValueMap04.{assertLimits, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

/**
  * ******* RESULTS ********
  * --------------------------------------------------------------------------------------
  * 84.6 query(s) per sec
  * 11.8 msec per query
  * --------------------------------------------------------------------------------------
  */
object HydraQuoValueMap02 extends HydraUseCase(10, 10, "quo") {

  //  override val sweep: HydraSweep = new B78ACF5280964474D87A8A310DBE53C16

  override val frameSource: String =
    s"""
       |   frame $frameName {
       |     cube user {
       |         limit = 100
       |         aggregates {
       |            'eventParameterFrequency':sum[long]
       |         }
       |         cube user.sessions.events.parameters {
       |            dimensions {
       |               'eventParameterKey':verbatim[string]
       |            }
       |         }
       |      }
       |
       |     user.sessions.events.parameters => {
       |         situ => {
       |            $analysisName.$frameName.eventParameterKey = key(user.sessions.events.parameters)
       |            insert($analysisName.$frameName)
       |         }
       |      }
       |
       |      user.sessions.events => {
       |         post => {
       |            $analysisName.$frameName.eventParameterFrequency = 1
       |         }
       |      }
       |   }
       """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

    result.resultSets.keys.size should be > 0

    /*
    val names = result.resultSets(0).columnNames.zipWithIndex.toMap
    val found = result.resultSets(0).rowSet.map {
      row => (row(names("eventParameterFrequency")).asLong, row(names("eventParameterKey")).asString)
    }.sortBy(_._2)

    found should equal(Array())
    */

  }


}
