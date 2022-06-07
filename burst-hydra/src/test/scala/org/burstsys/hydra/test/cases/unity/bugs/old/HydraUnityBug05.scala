/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.bugs.old

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase

/**
 * https://git.ouroath.com/burst/burst/issues/1727
 */
object HydraUnityBug05 extends HydraUseCase(200, 200, "unity") {

  //  override val sweep = new B38B8E377B3E6467ABD1A2B5DAD0D589B

  override val serializeTraversal = true

  override val frameSource: String =
    s"""|
        |  frame $frameName {
        |      var T2:boolean=null
        |      cube user {
        |         limit = 100
        |         dimensions {
        |            test1:verbatim[byte]
        |            test2:verbatim[byte]
        |            test3:verbatim[byte]
        |         }
        |      }
        |      user.sessions => {
        |         pre => {
        |            if(T2 == null) {
        |               $analysisName.$frameName.test1 = 1
        |               T2 = true
        |            } else if(T2 != null) {
        |               $analysisName.$frameName.test2 = 1
        |               T2 = null
        |            } else {
        |               $analysisName.$frameName.test3 = 1
        |            }
        |            insert($analysisName.$frameName)
        |         }
        |      }
        |   }
        |""".stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

  }


}
