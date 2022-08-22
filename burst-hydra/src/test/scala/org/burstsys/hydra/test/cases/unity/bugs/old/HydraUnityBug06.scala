/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.bugs.old

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraUnityBug06 extends HydraUseCase(210, 210, "unity") {

  //  override val sweep = new B6C8DD06ABF5348E48E934A0201DF1BC6

  override val serializeTraversal = true

  override val frameSource: String =
    s"""|
        |   frame $frameName {
        |      cube user {
        |         cube user.sessions {
        |            dimensions {
        |               beforeCount:verbatim[long]
        |               afterCount:verbatim[long]
        |            }
        |         }
        |      }
        |      user.sessions => {
        |         before => {
        |               $analysisName.$frameName.beforeCount = 1
        |               insert($analysisName.$frameName)
        |         }
        |         after => {
        |               $analysisName.$frameName.afterCount = 2
        |               insert($analysisName.$frameName)
        |         }
        |      }
        |   }""".stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

  }


}
