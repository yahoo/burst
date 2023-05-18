/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.bugs.old

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraUnityBug04 extends HydraUseCase(200, 200, "unity") {

  //    override val sweep = new B3365E46EA6FA45D5BB40AD9440241835

  override val analysisSource: String =
    s"""|
        |hydra $analysisName() {
        |	schema unity
        |	frame $frameName {
        |		cube user {
        |			limit = 100
        |			dimensions {
        |				id:verbatim[string]
        |			}
        |		}
        |		user.sessions.events.parameters => {
        |			situ => 			{
        |				I00000.id = key(user.sessions.events.parameters)
        |				insert(I00000)
        |			}
        |		}
        |	}
        |	frame frame2 {
        |		cube user {
        |			limit = 100
        |			aggregates {
        |				count:sum[long]
        |			}
        |			cube user.application {
        |				cube user.application.lastUse {
        |					dimensions {
        |						end:dayGrain[long]
        |					}
        |				}
        |				cube user.application.firstUse {
        |					dimensions {
        |						start:dayGrain[long]
        |					}
        |				}
        |			}
        |		}
        |		user => {
        |			post => 			{
        |				I00001.count = 1
        |
        |			}
        |		}
        |		user.application.firstUse => {
        |			pre => 			{
        |				I00001.start = user.application.firstUse.sessionTime
        |
        |			}
        |			post => 			{
        |				insert(I00001)
        |
        |			}
        |		}
        |		user.application.lastUse => {
        |			pre => 			{
        |				I00001.end = user.application.lastUse.sessionTime
        |			}
        |			post => 			{
        |				insert(I00001)
        |			}
        |		}
        |	}
        |}
        |""".stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

  }

}
