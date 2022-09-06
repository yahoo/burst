/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.mutables

import org.burstsys.alloy.views.AlloySmallDatasets.smallDataset_2_users_5_sessions
import org.burstsys.hydra.test.support.HydraAlloyTestRunner
import org.scalatest.Ignore

import scala.language.postfixOps

@Ignore
final
class HydraMutableSpec00 extends HydraAlloyTestRunner {

  it should "successfully execute query using mutable set parameter" in {
    val source =
      s"""|
          |hydra myAnalysis(
          |   p1:set[long] = set( 6049337, 4498119 )
          |) {
          |   schema unity
          |   frame myCube {
          |     cube user {
          |       limit = 9999
          |       dimensions {
          |         d1:verbatim[boolean]
          |       }
          |     }
          |     user => {
          |       pre => {
          |         myCube.d1 = contains( p1, 6049337 )
          |       }
          |     }
          |  }
          |}
          |""".stripMargin

    test(
      hydra = source,
      ds = smallDataset_2_users_5_sessions,
      validate = {
        (name, result) =>
          result.resultName match {
            case "myCube" =>
              val found = result.rowSet.map {
                row => (row[Long]("a1"), row[Long]("d1"))
              } sortBy (_._2) sortBy (_._1)
              val expected =
                Array()
              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // Some(new B53F39494BD91407FABA1F0D7057CD468)
    )




  }


}
