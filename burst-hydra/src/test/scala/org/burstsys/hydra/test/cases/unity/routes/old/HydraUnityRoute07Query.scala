/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.routes.old

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.wave.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase

import scala.language.postfixOps

object
HydraUnityRoute07Query extends HydraUseCase(200, 200, "unity") {

  //        override val sweep = new B69C2347A98D545BFAF863456D80675E0
  override val serializeTraversal: Boolean = true

  val cubeFrame1 = "myCube1"
  val cubeFrame2 = "myCube2"
  val routeFrame1 = "myRoute1"
  val routeFrame2 = "myRoute2"

  override def frameSource: String =
    s"""
       |frame $cubeFrame1 {
       |  cube user { limit = 9999 aggregates { 'keys1':sum[long]  }  }
       |  $routeFrame1.paths.steps user.sessions (4) => {
       |    before => {
       |      $cubeFrame1.'keys1' = routeVisitStepKey( $routeFrame1 )
       |    }
       |    situ => {
       |      $cubeFrame1.'keys1' = routeVisitStepKey( $routeFrame1 )
       |    }
       |    after => {
       |      $cubeFrame1.'keys1' = routeVisitStepKey( $routeFrame1 )
       |    }
       |  }
       |  $routeFrame2.paths.steps user.sessions (6) => {
       |    before => {
       |      $cubeFrame1.'keys1' = routeVisitStepKey( $routeFrame1 )
       |    }
       |    situ => {
       |      $cubeFrame1.'keys1' = routeVisitStepKey( $routeFrame1 )
       |    }
       |    after => {
       |      $cubeFrame1.'keys1' = routeVisitStepKey( $routeFrame1 )
       |    }
       |  }
       |}
       |frame $cubeFrame2 {
       |  cube user { limit = 9999 aggregates { 'keys2':sum[long]  }  }
       |  $routeFrame1.paths.steps user.sessions (2) => {
       |    before => {
       |      $cubeFrame2.'keys2' = routeVisitStepKey( $routeFrame2 )
       |    }
       |    situ => {
       |      $cubeFrame2.'keys2' = routeVisitStepKey( $routeFrame2 )
       |    }
       |    after => {
       |      $cubeFrame2.'keys2' = routeVisitStepKey( $routeFrame2 )
       |    }
       |  }
       |  $routeFrame2.paths.steps user.sessions (10) => {
       |    before => {
       |      $cubeFrame2.'keys2' = routeVisitStepKey( $routeFrame2 )
       |    }
       |    situ => {
       |      $cubeFrame2.'keys2' = routeVisitStepKey( $routeFrame2 )
       |    }
       |    after => {
       |      $cubeFrame2.'keys2' = routeVisitStepKey( $routeFrame2 )
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
       |        routeScopeStart( $routeFrame1 )
       |        routeFsmStepAssert( $routeFrame1, 1, 101, 1111 )
       |        routeFsmStepAssert( $routeFrame1, 2, 101, 1111 )
       |        routeScopeCommit( $routeFrame1 )
       |    }
       |  }
       |}
       |frame $routeFrame2 {
       |  route {
       |    maxPartialPaths = 1000
       |    maxSteps = 1000
       |    graph {  enter 1 { to(2) } exit 2 { } }
       |  }
       |  // static path visit
       |  user.sessions => {
       |    pre => {
       |        routeScopeStart( $routeFrame2 )
       |        routeFsmStepAssert( $routeFrame2, 1, 101, 1111 )
       |        routeFsmStepAssert( $routeFrame2, 2, 101, 1111 )
       |        routeScopeCommit( $routeFrame2 )
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
      row => (row.cells(0) asLong, row.cells(1) asLong, row.cells(2) asLong, row.cells(3) asLong)
    } sortBy (_._1) sortBy (_._2) sortBy (_._3) sortBy (_._4)
  }

  val expected: Array[Any] = Array()


}
