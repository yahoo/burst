/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.parameters

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.quo.parameters.HydraQuoParameters01.{analysisName, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase

/**
 * {{{
 * aggregate(
 * 'c' as TOP[35](user.sessions) where satisfies( user.sessions.events.parameters['EXP_ERR_crashFingerprint'] == '${fingerprint}' );
 * )
 * dimension(
 * 'd' as (user.sessions.events.parameters['application.state']) where satisfies( user.sessions.events.parameters['EXP_ERR_crashFingerprint'] == '${fingerprint}' );
 * )
 * limit = 35
 * }}}*/
object HydraQuoParameters02 extends HydraUseCase(1, 1, "quo") {

  //  override val sweep = new B40BD3A6B97E1417987E749D725FCCFC4
  override def analysisSource: String =
    s"""
       |hydra $analysisName(p1:string = "Chips Gained") {
       |  schema $schemaName
       |  frame $frameName  {
       |    cube user {
       |      limit = 9999
       |      cube user.sessions {
       |        aggregates {
       |          count:sum[long]
       |        }
       |      }
       |      cube user.sessions.events {
       |        aggregates {
       |          count:top[long](35)
       |        }
       |      }
       |    }
       |    user.sessions => {
       |      pre => {
       |        if( key(user.sessions.events.parameters) == p1) {
       |          $analysisName.$frameName.count = 1
       |        }
       |      }
       |    }
       |    user.sessions.events.parameters => {
       |      situ => {
       |        if( key(user.sessions.events.parameters) == p1) {
       |          $analysisName.$frameName.count = 1
       |        }
       |      }
       |    }
       | }
       |}
     """.stripMargin

  override def
  validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

    found(r.rowSet) should equal(expected)
  }


  def found(rowSet: Array[FabricResultRow]): Array[_] = {
    rowSet.map {
      row => (row.cells(0).asString, row.cells(1).asLong)
    }
  }

  val expected: Array[Any] = Array(149535)


}
