/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.funnels

import org.burstsys.alloy.alloy.store.AlloyView
import org.burstsys.alloy.alloy.views.AlloyJsonUseCaseViews.miniToAlloy
import org.burstsys.alloy.views.UnitMiniView
import org.burstsys.alloy.views.unity.UnityUseCaseViews.unitySchema
import org.burstsys.brio.flurry.provider.unity._
import org.burstsys.eql.test.support.EqlAlloyTestRunner

final
class EqlBracketFunnelSpec extends EqlAlloyTestRunner {

  override protected lazy val localViews: Array[AlloyView] = {
    val mv = UnitMiniView(unitySchema, 99, 99,
      Array(
        UnityMockUser(id = s"User1", sessions = Array(
          UnityMockSession(id = 1, startTime = 9, events = Array(
            UnityMockEvent(id = 2, eventType = 1, startTime = 10),
            UnityMockEvent(id = 4, eventType = 1, startTime = 11),
            UnityMockEvent(id = 6, eventType = 1, startTime = 12),
            UnityMockEvent(id = 8, eventType = 1, startTime = 13)
          )),
          UnityMockSession(id = 2, startTime = 19, events = Array(
            UnityMockEvent(id = 1, eventType = 1, startTime = 20),
            UnityMockEvent(id = 3, eventType = 1, startTime = 21),
            UnityMockEvent(id = 5, eventType = 1, startTime = 22),
            UnityMockEvent(id = 7, eventType = 1, startTime = 23)
          )),
          UnityMockSession(id = 3, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 1, startTime = 30),
            UnityMockEvent(id = 13, eventType = 1, startTime = 31),
            UnityMockEvent(id = 15, eventType = 1, startTime = 32),
            UnityMockEvent(id = 17, eventType = 1, startTime = 33)
          ))
        )),
        UnityMockUser(id = s"User2", sessions = Array(
          UnityMockSession(id = 1, startTime = 9, events = Array(
            UnityMockEvent(id = 2, eventType = 1, startTime = 10),
            UnityMockEvent(id = 4, eventType = 1, startTime = 11),
            UnityMockEvent(id = 6, eventType = 1, startTime = 12),
            UnityMockEvent(id = 8, eventType = 1, startTime = 13)
          )),
          UnityMockSession(id = 2, startTime = 19, events = Array(
            UnityMockEvent(id = 1, eventType = 1, startTime = 20),
            UnityMockEvent(id = 3, eventType = 1, startTime = 21),
            UnityMockEvent(id = 5, eventType = 1, startTime = 22),
            UnityMockEvent(id = 7, eventType = 1, startTime = 23)
          )),
          UnityMockSession(id = 3, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 1, startTime = 30),
            UnityMockEvent(id = 13, eventType = 1, startTime = 31),
            UnityMockEvent(id = 15, eventType = 1, startTime = 32),
            UnityMockEvent(id = 17, eventType = 1, startTime = 33)
          ))
        ))
      )
    )
    miniToAlloy(Array(mv))
  }

  it should "do grouping conversion funnel" in {
    val source =
      """
        |funnel test conversion {
        |   step 1 when start of user.sessions
        |   step 2 when user.sessions.events.id in (1, 2)
        |   step 3 when user.sessions.events.id in (3, 4)
        |   step 4 when user.sessions.events.id in (5, 6)
        |   step 5 when user.sessions.events.id in (7, 8)
        |
        |    1 : [1 2 3 4]+ : 5
        |} from schema Unity
        |
        |select as query_test t.paths.steps.id as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((1,2), (2,2), (3,2), (4,2), (5,2)))
    })
  }

  it should "do simple negating grouping conversion funnel" in {
    val source =
      """
        |funnel test conversion {
        |   step 1 when start of user.sessions
        |   step 2 when user.sessions.events.id in (1, 2)
        |   step 3 when user.sessions.events.id in (3, 4)
        |   step 4 when user.sessions.events.id in (5, 6)
        |   step 5 when user.sessions.events.id in (7, 8)
        |
        |    1 : [^5]+ : 5
        |} from schema Unity
        |
        |select as query_test t.paths.steps.id as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((1,2), (2,2), (3,2), (4,2), (5,2)))
    })
  }

  it should "do negating grouping conversion funnel" in {
    val source =
      """
        |funnel test conversion {
        |   step 1 when start of user.sessions
        |   step 2 when user.sessions.events.id in (1, 2)
        |   step 3 when user.sessions.events.id in (3, 4)
        |   step 4 when user.sessions.events.id in (5, 6)
        |   step 5 when user.sessions.events.id in (7, 8)
        |
        |    1 : [^1 5]+ : 5
        |} from schema Unity
        |
        |select as query_test t.paths.steps.id as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((1,2), (2,2), (3,2), (4,2), (5,2)))
    })
  }
}
