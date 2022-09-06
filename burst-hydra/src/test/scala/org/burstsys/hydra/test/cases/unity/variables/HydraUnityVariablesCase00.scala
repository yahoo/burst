/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.variables

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.quo.timegrain.HydraQuoDayGrain.{expected, frameName}
import org.burstsys.hydra.test.cases.support.HydraUseCase
import org.burstsys.hydra.test.cases.unity.splits.HydraUnitySplitCase00.{analysisName, frameName}

object HydraUnityVariablesCase00 extends HydraUseCase(200, 200, "unity") {

  //      override val sweep: BurstHydraSweep = new BE65BEBF88AFD4AE18AEBF403F019E3BC

  override def analysisSource: String =
    s"""
       |hydra $analysisName(gv1:array[long] = array(6049337, 4498119)) {
       |  schema unity
       |  frame $frameName  {
       |
       |    val gv2:set[long] = set(6049337, 4498119)
       |
       |    cube user {
       |      aggregates {
       |        count:sum[long]
       |      }
       |    }
       |
       |    user => {
       |      post => {
       |        $analysisName.$frameName.count = 1
       |        insert($analysisName.$frameName)
       |      }
       |    }
       |
       |  }
       |}
     """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
/*
    val r = result.resultSets(result.resultSetNames(queryName))
    checkNotLimited(r)
    val names = r.columnNames

    val found = r.rowSet.map {
      row => (row.cells(0).asLong, row.cells(1).asLong)
    }.sortBy(_._1)
    found should equal(Array(("EK1", 8928), ("EK2", 8929), ("EK3", 8929), ("EK4", 8929), ("EK5", 8929), ("EK6", 8928), ("EK7", 8928)))
*/

  }


}
