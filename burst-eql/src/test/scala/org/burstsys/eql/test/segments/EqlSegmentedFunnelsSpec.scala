/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.segments

import org.burstsys.alloy.alloy.store.AlloyView
import org.burstsys.alloy.alloy.views.AlloyJsonUseCaseViews.miniToAlloy
import org.burstsys.alloy.views.UnitMiniView
import org.burstsys.alloy.views.unity.UnityUseCaseViews.unitySchema
import org.burstsys.brio.flurry.provider.unity._
import org.burstsys.eql.test.support.EqlAlloyTestRunner

/**
 */
final
class EqlSegmentedFunnelsSpec extends EqlAlloyTestRunner {
  override protected lazy val localViews: Array[AlloyView] = {
    val mv = UnitMiniView(unitySchema, 99, 99,
      Array(
        UnityMockUser(id = s"User1", sessions = Array(
          UnityMockSession(id = 1, startTime = 9, events = Array(
            UnityMockEvent(id = 2, eventType = 1, startTime = 10)
          )),
          UnityMockSession(id = 2, startTime = 19, events = Array(
            UnityMockEvent(id = 1, eventType = 1, startTime = 20)
          )),
          UnityMockSession(id = 3, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 1, startTime = 30)
          )),
          UnityMockSession(id = 4, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 1, startTime = 30)
          )),
          UnityMockSession(id = 5, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 1, startTime = 30)
          )),
          UnityMockSession(id = 6, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 1, startTime = 30)
          )),
          UnityMockSession(id = 7, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 1, startTime = 30)
          )),
          UnityMockSession(id = 8, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 1, startTime = 30)
          )),
          UnityMockSession(id = 9, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 1, startTime = 30)
          )),
          UnityMockSession(id = 10, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 1, startTime = 30)
          ))
        )),
        UnityMockUser(id = s"User2", sessions = Array(
          UnityMockSession(id = 1, startTime = 9, events = Array(
            UnityMockEvent(id = 2, eventType = 2, startTime = 10)
          )),
          UnityMockSession(id = 2, startTime = 19, events = Array(
            UnityMockEvent(id = 1, eventType = 2, startTime = 20)
          )),
          UnityMockSession(id = 3, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 2, startTime = 30)
          )),
          UnityMockSession(id = 4, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 2, startTime = 30)
          )),
          UnityMockSession(id = 5, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 2, startTime = 30)
          )),
          UnityMockSession(id = 6, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 2, startTime = 30)
          )),
          UnityMockSession(id = 7, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 2, startTime = 30)
          )),
          UnityMockSession(id = 8, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 2, startTime = 30)
          )),
          UnityMockSession(id = 9, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 2, startTime = 30)
          )),
          UnityMockSession(id = 10, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 2, startTime = 30)
          ))
        ))

      )
    )
    miniToAlloy(Array(mv))
  }

  // TODO Hydra is complaining about visiting the route from the tablet.
  ignore should "segment on funnel test" in {
    val source =
      s"""
         |funnel test transaction {
         |   step 1 when start of user.sessions
         |   step 2 when user.sessions.events.id in (2, 1)
         |   1: 2
         |} from schema Unity
         |
         | segment 'simple' {
         |     segment 1 when t.paths.steps.id == 1
         |     segment 2 when t.paths.steps.id == 2
         | } from schema Unity, funnel test as t
         |
         |select t.paths.steps.id as id, count(t.paths.steps) as num
         |    where t.paths.steps.isComplete
         |beside
         |select count(s.members) as 'num', s.members.id as 'id'
         | from schema Unity, segment 'simple' as s, funnel 'test' as t
       """.stripMargin
    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((1,10), (2,4), (3,6), (4,4)))
    })
  }

  // TODO counting expressions not supported
  ignore should "counting segment on funnel test" in {
    val source =
      s"""
         |funnel test transaction {
         |   step 1 when start of user.sessions
         |   step 2 when user.sessions.events.id in (2, 1)
         |   1: 2
         |} from schema Unity
         |
         | segment 'simple' {
         |     segment 1 when (count(t.paths.steps) where t.paths.steps.id == 2) > 0
         |     segment 2 when (count(t.paths.steps) where t.paths.steps.id == 2) == 0
         | } from schema Unity, funnel test as t
         |
         |select t.paths.steps.id as id, count(t.paths.steps) as num
         |    where t.paths.steps.isComplete
         |beside
         |select count(s.members) as 'num', s.members.id as 'id'
         | from schema Unity, segment 'simple' as s, funnel 'test' as t
       """.stripMargin
    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((1,10), (2,4), (3,6), (4,4)))
    })
  }

}
