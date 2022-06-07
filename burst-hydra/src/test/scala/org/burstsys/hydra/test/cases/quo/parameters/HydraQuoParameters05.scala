/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.parameters

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.quo.parameters.HydraQuoParameters01.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoParameters05 extends HydraUseCase(1, 1, "quo") {

  //  override val sweep = new B5BDFC7003B7E47B1823E350B72C9A29D

  override def analysisSource: String =
    s"""
       |hydra $analysisName() {
       |  schema $schemaName
       |  frame $frameName  {
       |    cube user {
       |      limit = 10000
       |      cube user.sessions.events {
       |        dimensions {
       |          'key':verbatim[string]
       |          'parameter':verbatim[string]
       |        }
       |      }
       |    }
       |    user.sessions.events.parameters ⇒ {
       |      situ ⇒ {
       |        $analysisName.$frameName.'key' = key(user.sessions.events.parameters)
       |        $analysisName.$frameName.'parameter' = value(user.sessions.events.parameters)
       |        insert($analysisName.$frameName)
       |      }
       |    }
       |  }
       |}
     """.stripMargin

  override def
  validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)
  }


}
