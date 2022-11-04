/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.routes.old

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.wave.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase

object
HydraUnityRoute08Query extends HydraUseCase(200, 200, "unity") {

  //        override val sweep = new B69C2347A98D545BFAF863456D80675E0
  override val serializeTraversal: Boolean = true

  val cubeFrame1 = "myCube1"
  val routeFrame1 = "myRoute1"

  override def frameSource: String =
    s"""
       |frame $cubeFrame1 {
       |  cube user { limit = 9999 aggregates { 'keys1':sum[long]  }  }
       |  $analysisName.$routeFrame1.paths.steps user.sessions (4) => {
       |    before => {
       |      $analysisName.$cubeFrame1.'keys1' = routeVisitStepKey( $analysisName.$routeFrame1 )
       |    }
       |    pre => {
       |      $analysisName.$cubeFrame1.'keys1' = routeVisitStepKey( $analysisName.$routeFrame1 )
       |    }
       |    situ => {
       |      $analysisName.$cubeFrame1.'keys1' = routeVisitStepKey( $analysisName.$routeFrame1 )
       |    }
       |    post => {
       |      $analysisName.$cubeFrame1.'keys1' = routeVisitStepKey( $analysisName.$routeFrame1 )
       |    }
       |    after => {
       |      $analysisName.$cubeFrame1.'keys1' = routeVisitStepKey( $analysisName.$routeFrame1 )
       |    }
       |  }
       |}
       |frame $routeFrame1 {
       |  route {
       |    maxPartialPaths = 1000
       |    maxSteps = 1000
       |    graph {  enter 1 { to(2) } exit 2 { } }
       |  }
       |  user.sessions (3) => {
       |    pre => {
       |        routeScopeStart( $analysisName.$routeFrame1 )
       |        routeFsmStepAssert( $analysisName.$routeFrame1, 1, 101, 1111 )
       |        routeFsmStepAssert( $analysisName.$routeFrame1, 2, 101, 1111 )
       |        routeScopeCommit( $analysisName.$routeFrame1 )
       |    }
       |  }
       |}
       """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(cubeFrame1))
    assertLimits(r)
    found(r.rowSet) should equal(expected)
  }

  def found(rowSet: Array[FabricResultRow]): Array[_] = {
    rowSet.map {
      row => (row.cells(0).asLong, row.cells(1).asLong, row.cells(2).asLong, row.cells(3).asLong)
    } sortBy (_._1) sortBy (_._2) sortBy (_._3) sortBy (_._4)
  }

  val expected: Array[Any] = Array()


}
