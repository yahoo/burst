/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.cubes

import org.burstsys.alloy.alloy.store.AlloyView
import org.burstsys.alloy.alloy.views.AlloyJsonUseCaseViews.miniToAlloy
import org.burstsys.alloy.views.UnitMiniView
import org.burstsys.alloy.views.unity.UnityUseCaseViews
import org.burstsys.alloy.views.unity.UnityUseCaseViews.unitySchema
import org.burstsys.brio.flurry.provider.unity.{UnityMockEvent, UnityMockSession, UnityMockUser}
import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.vitals.errors.VitalsException

final
class EqlTopCubeQuerySpec extends EqlAlloyTestRunner {
  val userCount = 40
  override protected lazy val localViews: Array[AlloyView] = {
    val mv = UnitMiniView(unitySchema, 99, 99,
        (1 to userCount).map{ i =>
          UnityMockUser(id = s"User$i", sessions =
            (1 to i).map{j =>
              UnityMockSession(id = j, startTime = 9, osVersionId = i , events = Array(
                UnityMockEvent(id = 1, eventType = 1, startTime = j)
              ))
            }.toArray
          )}.toArray
    )
    miniToAlloy(UnityUseCaseViews.views ++ Array(mv))
  }

it should "successfully generate a top aggregate query" in {
    val source =
      s"""
         | select top[8](user.sessions.events) as evnts, user.sessions.events.parameters.key as kys
         | from schema Unity
       """.stripMargin

    runTest(source, 100, 200, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")
      if (result.groupMetrics.executionMetrics.rowCount <= 0)
        throw VitalsException(s"execution row count mismatch: expected rows got ${result.groupMetrics.executionMetrics.rowCount}")

      // all the besides should return a result set
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("evnts")).asLong, row(names("kys")).asString.take(2))
      }.sortBy{r => r._1}.reverse

      r should contain theSameElementsAs Array(
        (8929, "EK"), (8929, "EK"), (8929, "EK"), (8929, "EK"), (8928, "EK"), (8928, "EK"), (8928, "EK")
      )
    })
  }

  it should "really check top aggregate query" in {
    val source =
      s"""
         |select top[5](user.sessions) as sessions, user.sessions.osVersionId as osIds, user.id as userId
         |limit $userCount
         |from schema unity
         """.stripMargin

    runTest(source, 99, 99, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")
      if (result.groupMetrics.executionMetrics.rowCount <= 0)
        throw VitalsException(s"execution row count mismatch: expected rows got ${result.groupMetrics.executionMetrics.rowCount}")

      // all the besides should return a result set
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("sessions")).asLong, row(names("osIds")).asLong, row(names("userId")).asString)
      }


      r should contain theSameElementsAs Array((40,40,"User40"), (39,39,"User39"), (38,38,"User38"), (37,37,"User37"), (36,36,"User36"))
    })
  }

}
