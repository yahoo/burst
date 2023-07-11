/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.besides

import org.burstsys.alloy.AlloyDatasetSpec
import org.burstsys.alloy.alloy.store.AlloyView
import org.burstsys.alloy.alloy.views.AlloyJsonUseCaseViews.miniToAlloy
import org.burstsys.alloy.views.UnitMiniView
import org.burstsys.alloy.views.unity.UnityGenerator.generated
import org.burstsys.alloy.views.unity.UnityUseCaseViews.unitySchema
import org.burstsys.brio.flurry.provider.unity._
import org.burstsys.brio.types.BrioTypes
import org.burstsys.eql.canned.EqlQueriesCan
import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.vitals.errors.VitalsException
import org.scalatest.Ignore

/**
  *  Performance test of besides query
  *
  */
@Ignore // this is a performance test, not a unit test, so it should be ignored by default
final
class EqlPerfSpec extends EqlAlloyTestRunner {
  override protected lazy val localViews: Array[AlloyView] = {
    val mv1 = UnitMiniView(AlloyDatasetSpec(unitySchema, 99L),
      generated(userCount = 5000, sessionCount = 100, eventCount = 10, parameterCount = 5)
    )
    miniToAlloy(Array(mv1))
  }
  it should "run the v3 UI dimensions query" in {
    // goes in the event parameterKey query
    for (i <- 0 until 1000000)
    runTest(EqlQueriesCan.uiUnityDimensionUsageSourceV3, 99, 99, { result =>
      log info s"iteration: $i"
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")
      if (result.groupMetrics.executionMetrics.rowCount <= 0)
        throw VitalsException(s"execution row count mismatch: expected rows got ${result.groupMetrics.executionMetrics.rowCount}")

      // all the besides should return a result sert
      result.resultSets.keys.size should be > 0
      for (i <- 0 until result.resultSets.size) {
        val r = result.resultSets(i).rowSet.map {
          row =>
            row.cells.map {
              cell =>
                cell.bType match {
                  case BrioTypes.BrioLongKey => cell.asLong
                  case BrioTypes.BrioStringKey => cell.asString.hashCode.toLong
                  case _ => throw VitalsException("not implemented")
                }
            }
        }.sortBy(_.last).sortBy(_ (1)).sortBy(_.head)

      }
    })
  }
}
