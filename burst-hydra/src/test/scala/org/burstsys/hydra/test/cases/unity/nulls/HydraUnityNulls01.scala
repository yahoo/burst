/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.nulls

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase

import scala.language.postfixOps

object HydraUnityNulls01 extends HydraUseCase(150, 150, "unity") {

  //  override val sweep = new B53FDFE4FB84E470E8921ACA88A86BDDC

  override val frameSource: String =

    s"""|frame $frameName {
        | cube user {
        |   limit = 4
        |   dimensions {
        |      d1:verbatim[long]
        |    }
        | }
        | user ⇒ {
        |    pre ⇒ {
        |     if(user.id == null) {
        |       $analysisName.$frameName.d1 = 1
        |     } else {
        |       $analysisName.$frameName.d1 = 10
        |     }
        |     insert($analysisName.$frameName)
        |   }
        | }
        |}""".stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)
    found(r.rowSet) should equal(expected)
  }

  def found(rowSet: Array[FabricResultRow]): Array[_] = {
    rowSet.map {
      row => row.cells(0).asLong
    } sorted
  }

  val expected: Array[Any] = Array(10)


}
