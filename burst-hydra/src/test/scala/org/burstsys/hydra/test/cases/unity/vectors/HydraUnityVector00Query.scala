/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.vectors

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase
import org.burstsys.hydra.test.cases.unity.splits.HydraUnitySplitCase00.{analysisName, frameName}

object HydraUnityVector00Query extends HydraUseCase(200, 200, "unity") {

  /*
    override val sweep = new BB7123E91DD4F43BE8287851A3F67C39D
    override val serializeTraversal: Boolean = true
  */

  override val frameSource: String =
    s"""|
        |frame $frameName {
        |  cube user {
        |    limit = 10
        |    aggregates {
        |      'isOther':sum[long]
        |      'isFirst':sum[long]
        |      'isLast':sum[long]
        |    }
        |  }
        |  user.sessions => {
        |    pre => {
        |       $analysisName.$frameName.'isOther' = 1
        |       if( isFirst(user.sessions) ) {
        |         $analysisName.$frameName.'isFirst' = 1
        |       }
        |       if( isLast(user.sessions) ) {
        |         $analysisName.$frameName.'isLast' = 1
        |       }
        |    }
        |  }
        |}
       """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)
    val found = r.rowSet.map {
      row => (row.cells(0).asLong, row.cells(1).asLong, row.cells(2).asLong)
    }.sortBy(_._1)
    found should equal(
      Array((1250, 50, 50))
    )

  }


}
