/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.routes.old

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase

import scala.language.postfixOps

object
HydraUnityRoute09Query extends HydraUseCase(200, 200, "unity") {

  //        override val sweep = new B4DB4B8CE815541C781137AA2AB51C6B8
  override val serializeTraversal: Boolean = true

  val cubeFrame1 = "myCube1"
  val routeFrame1 = "myRoute1"

  override def frameSource: String =
    s"""
       |frame $cubeFrame1 {
       |  cube user {
       |    limit = 9999
       |    dimensions {
       |      'pathOrdinal':verbatim[long]
       |      'stepKey':verbatim[long]
       |      'stepTag':verbatim[long]
       |      'stepTime':verbatim[long]
       |    }
       |  }
       |  $analysisName.$routeFrame1.paths.steps user (4) ⇒ {
       |    situ => {
       |      $analysisName.$cubeFrame1.'pathOrdinal' = routeVisitPathOrdinal( $analysisName.$routeFrame1 )
       |      $analysisName.$cubeFrame1.'stepKey' = routeVisitStepKey( $analysisName.$routeFrame1 )
       |      $analysisName.$cubeFrame1.'stepTag' = routeVisitStepTag( $analysisName.$routeFrame1 )
       |      $analysisName.$cubeFrame1.'stepTime' = routeVisitStepTime( $analysisName.$routeFrame1 )
       |      insert( $analysisName.$cubeFrame1 )
       |    }
       |  }
       |}
       |frame $routeFrame1 {
       |  route {
       |    maxPartialPaths = 1000
       |    maxSteps = 1000
       |    graph {  enter 1 { to(2) } exit 2 { } }
       |  }
       |  user.sessions (3) ⇒ {
       |    pre ⇒ {
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
    val f = found(r.rowSet)
    f should equal(expected)
  }

  def found(rowSet: Array[FabricResultRow]): Array[_] = {
    rowSet.map {
      row => (row.cells(0) asLong, row.cells(1) asLong, row.cells(2) asLong, row.cells(3) asLong)
    } sortBy (_._1) sortBy (_._2) sortBy (_._3) sortBy (_._4)
  }

  val expected: Array[Any] = Array(
    (1, 1, 101, 1111), (2, 1, 101, 1111), (3, 1, 101, 1111), (4, 1, 101, 1111), (5, 1, 101, 1111), (6, 1, 101, 1111), (7, 1, 101, 1111),
    (8, 1, 101, 1111), (9, 1, 101, 1111), (10, 1, 101, 1111), (11, 1, 101, 1111), (12, 1, 101, 1111), (13, 1, 101, 1111),
    (14, 1, 101, 1111), (15, 1, 101, 1111), (16, 1, 101, 1111), (17, 1, 101, 1111), (18, 1, 101, 1111), (19, 1, 101, 1111),
    (20, 1, 101, 1111), (21, 1, 101, 1111), (22, 1, 101, 1111), (23, 1, 101, 1111), (24, 1, 101, 1111), (25, 1, 101, 1111),
    (1, 2, 101, 1111), (2, 2, 101, 1111), (3, 2, 101, 1111), (4, 2, 101, 1111), (5, 2, 101, 1111), (6, 2, 101, 1111), (7, 2, 101, 1111),
    (8, 2, 101, 1111), (9, 2, 101, 1111), (10, 2, 101, 1111), (11, 2, 101, 1111), (12, 2, 101, 1111), (13, 2, 101, 1111),
    (14, 2, 101, 1111), (15, 2, 101, 1111), (16, 2, 101, 1111), (17, 2, 101, 1111), (18, 2, 101, 1111), (19, 2, 101, 1111),
    (20, 2, 101, 1111), (21, 2, 101, 1111), (22, 2, 101, 1111), (23, 2, 101, 1111), (24, 2, 101, 1111), (25, 2, 101, 1111)
  )


}
