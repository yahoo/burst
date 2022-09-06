/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.frames

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraUnityFramesCase01 extends HydraUseCase(100, 100, "unity") {

  //  override val sweep: BurstHydraSweep = new B71882B91BF9C45D9B97023F85E9AA0D0

  override def analysisSource: String =
    s"""
       |hydra $analysisName(p1:long) {
       |  schema unity
       |  frame $frameName { // a 'cube' frame container
       |    cube user {
       |      limit = 1
       |      aggregates {
       |        tally:sum[long]
       |      }
       |      dimensions {
       |        keys:verbatim[integer]
       |        tags:verbatim[integer]
       |        times:verbatim[integer]
       |      }
       |    }
       |    //val v1:boolean = false // frame level variables (not shared inter-frame)
       |
       |    // visits/actions
       |     user.sessions.myRoute.paths.steps  => {
       |      post => {
       |        tally += 1 // relative reference to local frame
       |        keys = routeVisitStepKey(myRoute)
       |        times = routeVisitStepTime(myRoute)
       |        tags = routeVisitStepTag(myRoute)
       |      }
       |    }
       |  }
       |  frame myRoute {  // a 'route' frame container
       |    route user.sessions {
       |      graph {
       |        enter 1 {
       |          to(2)
       |          to(3, 0, 0)
       |        }
       |        exit 2 {
       |        }
       |        exit 3 {
       |        }
       |      }
       |    }
       |    // visits/actions
       |    user.sessions.events => {
       |      post =>  {
       |        routeScopeStart( myRoute)
       |        routeFsmStepAssert( myRoute, 1, 101, 1111 )
       |        routeScopeCommit( myRoute )
       |      }
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
      row => (row.cells(0).asLong, row.cells(1).asLong)
    } sortBy (_._1)
  }

  val expected: Array[Any] = Array(
    (12345, 1),
    (234567, 2),
    (345678, 3),
    (456789, 4)
  )

}
