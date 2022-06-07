/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.routes.old

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase

object
HydraUnityRoute04Query extends HydraUseCase(200, 200, "unity") {

  //      override val sweep = new B7AE373F2B27343A0AF424AF61F3FDF5E
  override val serializeTraversal: Boolean = true

  override def frameSource: String =
    s"""
       |frame $frameName {
       |  cube user {
       |    limit = 500
       |    aggregates {
       |      'keys':sum[long]
       |      'times':sum[long]
       |    }
       |    dimensions {
       |      'path':verbatim[long]
       |    }
       |    cube user.one {
       |      dimensions {
       |        'tag':verbatim[long]
       |      }
       |    }
       |  }
       |
       |  myRoute.user.one.paths.steps ⇒ {
       |    situ => {
       |      // for each step
       |      $analysisName.$frameName.'keys' = event.??? // routeVisitStepKey(myRoute)
       |      $analysisName.$frameName.'times' = routeVisitStepTime(myRoute)
       |      $analysisName.$frameName.'tag' = routeVisitStepTag(myRoute)
       |    }
       |  }
       |} // end of frame
       |
       |frame myRoute {
       |  route user {
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
       |  user.sessions ⇒ {
       |    pre ⇒ {
       |        routeScopeStart(r1)
       |        routeFsmStepAssert( r1, 1, 101, 1111 )
       |        routeFsmStepAssert( r1, 2, 103, 2222 )
       |        routeFsmStepAssert( r1, 3, 107, 7777 )
       |        routeFsmAssertTime( r1, 3, 3333)
       |
       |        routeFsmStepAssert( r1, 1, 109, 4444 )
       |        routeFsmStepAssert( r1, 2, 113, 5555 )
       |        routeFsmStepAssert( r1, 3, 127, 7777 )
       |        routeFsmAssertTime( r1, 3, 6666)
       |        routeScopeCommit( r1 )
       |    }
       |  }
       |}
       """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)
    found(r.rowSet) should equal(expected)
  }

  def found(rowSet: Array[FabricResultRow]): Array[_] = {
    rowSet.map {
      row => (row.cells(0).asLong, row.cells(1).asLong, row.cells(2).asLong, row.cells(3).asLong)
    } sortBy (_._1) sortBy (_._2) sortBy (_._3) sortBy (_._4)
  }

  val expected: Array[Any] =
    Array(
      (1, 0, 50, 55550), (1, 127, 1200, 1333200),
      (1, 101, 2500, 2777500), (1, 103, 3750, 4166250),
      (2, 107, 1250, 5555000), (2, 109, 2500, 6943750),
      (2, 113, 3750, 8332500)
    )
}
