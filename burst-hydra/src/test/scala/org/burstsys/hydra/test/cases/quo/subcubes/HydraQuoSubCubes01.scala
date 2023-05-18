/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.subcubes

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.wave.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoSubCubes01 extends HydraUseCase(1, 1, "quo") {

  //  override val sweep = new B9A8A8CCA89504DE793DB8EDE58887007

  override def analysisSource: String =
    s"""
       |hydra $analysisName() {
       |   schema 'quo'
       |   frame $frameName {
       |     cube user {
       |         limit = 14000
       |         aggregates {
       |            eventParameterFrequency:sum[long]
       |         }
       |         dimensions {
       |            eventParameters:verbatim[long]
       |         }
       |         cube user.sessions.events.parameters {
       |            dimensions {
       |               eventParameterKey:verbatim[string]
       |            }
       |         }
       |      }
       |     user.sessions.events.parameters => {
       |         situ => {
       |               $analysisName.$frameName.eventParameterKey = key(user.sessions.events.parameters)
       |               insert($analysisName.$frameName)
       |         }
       |      }
       |      user.sessions.events => {
       |         pre => {
       |            $analysisName.$frameName.eventParameters = user.sessions.events.eventId
       |         }
       |         post => {
       |            $analysisName.$frameName.eventParameterFrequency = 1
       |         }
       |      }
       |   }
       |}
       """.stripMargin


  override def
  validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

    val f = found(r.rowSet)
    f should equal(expected)
  }

  def found(rowSet: Array[FabricResultRow]): Array[_] = {
    rowSet.map {
      row => (row.cells(0).asLong, row.cells(1).asString, row.cells(2).asLong)
    }.sortBy(_._2).sortBy(_._1)
  }

  val expected: Array[Any] = Array((4498113,"gv",297), (4498113,"ul",297), (4498114,"gv",1122), (4498114,"ul",1122), (4498115,"gt",821), (4498115,"gv",821), (4498115,"ul",821), (4498116,"as",33119), (4498116,"gt",33119), (4498116,"gv",33119), (4498116,"ul",33119), (4498117,"gt",33110), (4498117,"gv",33110), (4498117,"ul",33110), (4498118,"gt",33099), (4498118,"gv",33099), (4498118,"rsl",33099), (4498118,"ul",33099), (4498119,"cg",14555), (4498119,"gb",14555), (4498119,"gv",14555), (4498119,"ul",14555), (4498120,"gt",13434), (4498120,"gv",13434), (4498120,"ul",13434), (4498121,"gt",29937), (4498121,"gv",29937), (4498121,"ul",29937), (4498122,"ab",2885), (4498122,"gt",2885), (4498122,"gv",2885), (4498122,"sc",2885), (4498122,"ul",2885), (4498123,"cge",1301), (4498123,"gt",1301), (4498123,"gv",1301), (4498123,"ul",1301), (4498124,"gv",14), (4498124,"ul",14), (4498126,"gt",390), (4498126,"gv",390), (4498126,"ul",390), (5555423,"gv",119), (5555423,"ul",119), (6049337,"gt",279), (6049337,"gv",279), (6049337,"ul",279))


}
