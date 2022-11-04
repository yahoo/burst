/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.routes.old

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.wave.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase

object
HydraUnityRoute06Query extends HydraUseCase(200, 200, "unity") {

  //        override val sweep = new B9FEB367C929E4DFCB5575355CFC7C9C2
  override val serializeTraversal: Boolean = true

  val cubeFrame = "myCube"
  val routeFrame = "myRoute"

  override def frameSource: String =
    s"""FeltNameSpace
       |frame $cubeFrame {
       |  cube user {
       |    limit = 500
       |    aggregates {
       |      'keys':sum[long]
       |      'times':sum[long]
       |    }
       |    dimensions {
       |      'path':verbatim[long]
       |      'tag':verbatim[long]
       |    }
       |  }
       |  // dynamic path visit
       |  $analysisName.$routeFrame.paths user.sessions (1) => {
       |    before => {
       |      $analysisName.$cubeFrame.'keys' = routeVisitStepKey( $analysisName.$routeFrame )
       |      $analysisName.$cubeFrame.'times' = routeVisitStepTime( $analysisName.$routeFrame )
       |      $analysisName.$cubeFrame.'tag' = routeVisitStepTag( $analysisName.$routeFrame )
       |    }
       |  }
       |  // dynamic path visit
       |  $analysisName.$routeFrame.paths.steps user.sessions (2) => {
       |    situ => {
       |      $analysisName.$cubeFrame.'keys' = routeVisitStepKey( $analysisName.$routeFrame )
       |      $analysisName.$cubeFrame.'times' = routeVisitStepTime( $analysisName.$routeFrame )
       |      $analysisName.$cubeFrame.'tag' = routeVisitStepTag( $analysisName.$routeFrame )
       |    }
       |  }
       |} // end of frame
       |
       |frame $routeFrame {
       |  route {
       |    maxPartialPaths = 1000
       |    maxSteps = 1000
       |    graph {
       |      enter 1 {
       |        to(2)
       |      }
       |      2 {
       |        to(3)
       |      }
       |      exit 3 {
       |      }
       |    }
       |  }
       |  // static path visit
       |  user.sessions => {
       |    pre => {
       |        routeScopeStart( $analysisName.$routeFrame )
       |        routeFsmStepAssert( $analysisName.$routeFrame, 1, 101, 1111 )
       |        routeFsmStepAssert( $analysisName.$routeFrame, 2, 103, 2222 )
       |        routeFsmStepAssert( $analysisName.$routeFrame, 3, 107, 7777 )
       |        routeFsmAssertTime( $analysisName.$routeFrame, 3, 3333)
       |
       |        routeFsmStepAssert( $analysisName.$routeFrame, 1, 109, 4444 )
       |        routeFsmStepAssert( $analysisName.$routeFrame, 2, 113, 5555 )
       |        routeFsmStepAssert( $analysisName.$routeFrame, 3, 127, 7777 )
       |        routeFsmAssertTime( $analysisName.$routeFrame, 3, 6666)
       |        routeScopeCommit( $analysisName.$routeFrame )
       |    }
       |  }
       |}
       """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(cubeFrame))
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
