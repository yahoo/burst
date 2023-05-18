/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.lexicon

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoLexiconCase00 extends HydraUseCase(1, 1, "quo", executionCount = 1.toInt) {

  //    override val sweep = new B4C23889184E24B97B0EC5D3974AB0DF0
  //  override val serializeTraversal = true

  override val frameSource: String =
    s"""
       |frame $frameName {
       |   cube user {
       |      limit = 100
       |      aggregates {
       |         hitTally:sum[long]
       |         missTally:sum[long]
       |      }
       |   }
       |   user.sessions.events => {
       |      pre => {
       |         if(user.sessions.events.parameters["cg"] == "300") {
       |            $analysisName.$frameName.hitTally = 1
       |         } else {
       |            $analysisName.$frameName.missTally = 1
       |         }
       |         insert($analysisName.$frameName)
       |      }
       |   }
       |}""".stripMargin

  override def
  validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)
    r.rowSet.map {
      row => (row.cells(0).asLong, row.cells(1).asLong)
    } should equal(
      Array((1426,163056))
    )
  }


}
