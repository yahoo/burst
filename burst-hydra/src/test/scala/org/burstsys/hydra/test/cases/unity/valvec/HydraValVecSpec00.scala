/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.valvec

import org.burstsys.alloy.views.unity.UnityUseCaseViews.over_200_200
import org.burstsys.hydra.test.support.HydraAlloyTestRunner

//@Ignore
final
class HydraValVecSpec00 extends HydraAlloyTestRunner {

  it should "perform a query using a value vector relationship in the lattice 00" in {
    val source =
      s"""
         |hydra myAnalysis() {
         |	schema unity
         |	frame myCube {
         |		cube user {
         |			limit = 100
         |			aggregates {
         |				userCount:sum[long]
         |			}
         |			cube user.interests {
         |				dimensions {
         |					interests:verbatim[long]
         |				}
         |			}
         |		}
         |		user.interests => {
         |			situ => 			{
         |				myCube.interests = value(user.interests)
         |				insert( myCube )
         |			}
         |		}
         |		user => {
         |			post => 			{
         |				myCube.userCount = 1
         |			}
         |		}
         |	}
         |}""".stripMargin

    test(
      hydra = source,
      ds = over_200_200,
      validate = {
        (name, result) =>
          result.resultName match {
            case "myCube" =>
              val found = result.rowSet.map {
                row => (row[Long]("interests"), row[Long]("userCount"))
              } sortBy (_._2) sortBy (_._1)
              val expected =
                Array((1, 10), (2, 10), (3, 10), (4, 10), (5, 10), (6, 10), (7, 10), (8, 10), (9, 10), (10, 10))
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // Some(new B53F39494BD91407FABA1F0D7057CD468)
    )
  }

}
