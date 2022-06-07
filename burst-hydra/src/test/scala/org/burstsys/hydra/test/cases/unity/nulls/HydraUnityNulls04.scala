/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.nulls

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase

/**
 * https://git.ouroath.com/burst/burst/issues/1762
 */
object HydraUnityNulls04 extends HydraUseCase(200, 200, "unity") {

  //  override val sweep = new B13DC2D5C565D42DDB4186D3D57096EEE

  //  override val serializeTraversal = true

  override val frameSource: String =
    s"""|
        |   frame $frameName {
        |   var T1_summary:boolean=false
        |   var T1:boolean=false
        |   cube user {
        |      limit = 100
        |      aggregates {
        |         frequency:sum[long]
        |      }
        |   }
        |   user => {
        |      pre => {
        |         T1 = ((user.application.firstUse) != null)
        |      }
        |      post => {
        |         if (T1) {
        |            T1_summary = true
        |            $analysisName.$frameName.frequency = 1
        |         }
        |      }
        |   }
        | }""".stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)
    val names = result.resultSets(0).columnNamesMap
    val expected = result.resultSets(0).rowSet.map {
      row => (row(names("frequency")).asLong)
    }

    expected should equal(
      Array(50)
    )
  }

}
