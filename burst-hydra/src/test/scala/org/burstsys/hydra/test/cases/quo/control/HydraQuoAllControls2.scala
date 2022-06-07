/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.control

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoAllControls2 extends HydraUseCase(1, 1, "quo") {

  //    override val sweep = new B64BFA4A97AE84B7DB8DC7BB34C672ADA

  override def analysisSource: String =
    s"""
       |hydra $analysisName() {
       |  schema 'quo'
       |  frame $frameName {
       |    cube user {
       |      limit = 1
       |      aggregates {
       |        a0:sum[long]
       |      }
       |      dimensions {
       |        d0:verbatim[long]
       |      }
       |      cube user.sessions.events {
       |        aggregates {
       |          a1:sum[long]
       |        }
       |        dimensions {
       |          d1:verbatim[long]
       |        }
       |      }
       |    }
       |    user.sessions.events ⇒ {
       |      pre ⇒ {
       |        commitVisit(user.sessions.events)
       |        abortVisit(user.sessions.events)
       |        commitInstance(user)
       |        abortInstance(user.sessions)
       |        commitRelation(user)
       |        abortRelation(user.sessions)
       |      }
       |    }
       |  }
       |}
       """.stripMargin

  // 4866819
  // 48668190

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

  }


}
