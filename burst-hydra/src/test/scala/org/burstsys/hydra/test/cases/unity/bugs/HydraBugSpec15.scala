/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.bugs

import org.burstsys.alloy.views.unity.UnityUseCaseViews.over_200_200
import org.burstsys.hydra.test.support.HydraAlloyTestRunner
import org.scalatest.Ignore

import scala.language.postfixOps

@Ignore
final
class HydraBugSpec15 extends HydraAlloyTestRunner {

  // This will result in a stack overflow at compilation
  it should "successfully do two routes with tablet" in {
    val source =
      s"""|
          |hydra $AnalysisName() {
          |	schema 'unity'
          |	frame $CubeFrame {
          |		cube user {
          |			limit = 1
          |			aggregates {
          |				'_a1_':sum[double]
          |			}
          |		}
          |		user.sessions.events  => {
          |			post => 			{
          |				$AnalysisName.$CubeFrame.'_a1_' = 				cast(user.sessions.events.parameters["friendId"] as double)
          |
          |			}
          |		}
          |	}
          |}
          |""".stripMargin

    test(source, over_200_200, {
      (name, result) =>
        name match {
          case CubeFrame =>
            val found = result.rowSet.map {
              row => row.cells(0) asLong
            } sortBy(r => r)
            /*
            val expected = Array(
              (1,1,101,1111), (2,1,101,1111), (3,1,101,1111), (4,1,101,1111), (5,1,101,1111), (6,1,101,1111), (7,1,101,1111), (8,1,101,1111), (9,1,101,1111), (10,1,101,1111), (11,1,101,1111), (12,1,101,1111), (13,1,101,1111), (14,1,101,1111), (15,1,101,1111), (16,1,101,1111), (17,1,101,1111), (18,1,101,1111), (19,1,101,1111), (20,1,101,1111), (21,1,101,1111), (22,1,101,1111), (23,1,101,1111), (24,1,101,1111), (25,1,101,1111)
            )
            found should equal(expected)
            */
          case _ =>
        }
    }
    )
  }

}
