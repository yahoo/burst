/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.routes.old

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraUnityRoute01Query extends HydraUseCase(200, 200, "unity") {

  //    override val sweep = new B96D1CAAC89D44DDD86DF1CC574C5892E

  override def frameSource: String =
    s"""
       |frame $frameName {
       |  cube user {
       |    limit = 9999
       |    aggregates {
       |      a0:sum[long]
       |    }
       |    dimensions {
       |      d0:verbatim[long]
       |    }
       |  }
       |  user.sessions ⇒ {
       |    pre ⇒ {
       |       if(routeFsmStepAssert( $analysisName.frame2, 1, -1, now() ) ){
       |         insert($analysisName.$frameName)
       |       }
       |    }
       |  }
       |}
       |frame frame2 {
       |  route user.sessions {
       |    graph {
       |      enter 1 {
       |          to(2)
       |          to(3, 0, 0)
       |      }
       |      exit 2 {
       |      }
       |      exit 3 {
       |      }
       |    }
       |  }
       |}
       """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

  }


}
