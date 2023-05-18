/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.control

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoAllControls extends HydraUseCase(1, 1, "quo") {

  //    override val sweep = new B8C7D16F0903A4F24860D0303398C04B1

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
       |      cube user.sessions {
       |        aggregates {
       |          a1:sum[long]
       |        }
       |        dimensions {
       |          d1:verbatim[long]
       |        }
       |        cube user.sessions.events {
       |          aggregates {
       |            a2:sum[long]
       |          }
       |          dimensions {
       |            d2:verbatim[long]
       |          }
       |        }
       |      }
       |    }
       |    user.sessions.events => {
       |      pre => {
       |        commitRelation(user.sessions.events)
       |        abortRelation(user.sessions.events)
       |        commitMember(user)
       |        abortMember(user.sessions)
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
