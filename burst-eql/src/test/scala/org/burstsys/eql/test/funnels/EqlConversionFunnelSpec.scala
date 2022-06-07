/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.funnels

import org.burstsys.alloy.alloy.store.AlloyView
import org.burstsys.alloy.alloy.views.AlloyJsonUseCaseViews.miniToAlloy
import org.burstsys.alloy.views.UnitMiniView
import org.burstsys.alloy.views.unity.UnityUseCaseViews.unitySchema
import org.burstsys.brio.flurry.provider.unity._
import org.burstsys.eql.test.support.EqlAlloyTestRunner


final
class EqlConversionFunnelSpec extends EqlAlloyTestRunner {

  override protected lazy val localViews: Array[AlloyView] = {
    val mv1 = UnitMiniView(unitySchema, 99, 99,
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
            UnityMockEvent(id = 5, eventType = 1, startTime = 22)
          )),
          UnityMockSession(id = 3, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 1, startTime = 30),
            UnityMockEvent(id = 13, eventType = 1, startTime = 31),
            UnityMockEvent(id = 15, eventType = 1, startTime = 32)
          ))
        )),
        UnityMockUser(id = s"User2", sessions = Array(
          UnityMockSession(id = 1, startTime = 9, events = Array(
            UnityMockEvent(id = 2, eventType = 1, startTime = 10),
            UnityMockEvent(id = 4, eventType = 1, startTime = 11),
            UnityMockEvent(id = 6, eventType = 1, startTime = 12)
          )),
          // match events have lots significant events that should be ignored
          UnityMockSession(id = 2, startTime = 19, events = Array(
            UnityMockEvent(id = 1, eventType = 1, startTime = 20),
            UnityMockEvent(id = 1, eventType = 1, startTime = 21),
            UnityMockEvent(id = 1, eventType = 1, startTime = 22),
            UnityMockEvent(id = 7, eventType = 1, startTime = 23),
            UnityMockEvent(id = 3, eventType = 1, startTime = 24),
            UnityMockEvent(id = 5, eventType = 1, startTime = 25),
            UnityMockEvent(id = 1, eventType = 1, startTime = 26),
            UnityMockEvent(id = 3, eventType = 1, startTime = 27),
            UnityMockEvent(id = 7, eventType = 1, startTime = 28)
          )),
          UnityMockSession(id = 3, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 1, startTime = 30),
            UnityMockEvent(id = 13, eventType = 1, startTime = 31),
            UnityMockEvent(id = 15, eventType = 1, startTime = 32)
          ))
        ))
      )
    )
    val mv2 = UnitMiniView(unitySchema, 98, 98,
      Array(
        UnityMockUser(id = s"User1",
          application = UnityMockApplication(firstUse = UnityMockUse(sessionTime = 5)),
          sessions = Array(
          UnityMockSession(id = 1, startTime = 9, sessionType = 1, events = Array(
            UnityMockEvent(id = 2, eventType = 1, startTime = 10),
            UnityMockEvent(id = 4, eventType = 1, startTime = 11),
            UnityMockEvent(id = 6, eventType = 1, startTime = 12),
            UnityMockEvent(id = 8, eventType = 1, startTime = 13)
          )),
          UnityMockSession(id = 2, startTime = 19, sessionType = 1, events = Array(
            UnityMockEvent(id = 1, eventType = 1, startTime = 20),
            UnityMockEvent(id = 3, eventType = 1, startTime = 21),
            UnityMockEvent(id = 5, eventType = 1, startTime = 22)
          )),
          UnityMockSession(id = 3, startTime = 29, sessionType = 1, events = Array(
            UnityMockEvent(id = 11, eventType = 1, startTime = 30),
            UnityMockEvent(id = 13, eventType = 1, startTime = 31),
            UnityMockEvent(id = 15, eventType = 1, startTime = 32)
          ))
        )),
        UnityMockUser(id = s"User2", sessions = Array(
          UnityMockSession(id = 1, startTime = 9, sessionType = 1, events = Array(
            UnityMockEvent(id = 2, eventType = 1, startTime = 10),
            UnityMockEvent(id = 4, eventType = 1, startTime = 11),
            UnityMockEvent(id = 6, eventType = 1, startTime = 12)
          )),
          // match events have lots significant events that should be ignored
          UnityMockSession(id = 2, startTime = 19, sessionType = 1, events = Array(
            UnityMockEvent(id = 1, eventType = 1, startTime = 20),
            UnityMockEvent(id = 1, eventType = 1, startTime = 21),
            UnityMockEvent(id = 1, eventType = 1, startTime = 22),
            UnityMockEvent(id = 7, eventType = 1, startTime = 23),
            UnityMockEvent(id = 3, eventType = 1, startTime = 24),
            UnityMockEvent(id = 5, eventType = 1, startTime = 25),
            UnityMockEvent(id = 1, eventType = 1, startTime = 26),
            UnityMockEvent(id = 3, eventType = 1, startTime = 27),
            UnityMockEvent(id = 7, eventType = 1, startTime = 28)
          )),
          UnityMockSession(id = 3, startTime = 29, sessionType = 1, events = Array(
            UnityMockEvent(id = 11, eventType = 1, startTime = 30),
            UnityMockEvent(id = 13, eventType = 1, startTime = 31),
            UnityMockEvent(id = 15, eventType = 1, startTime = 32)
          ))
        )),
        UnityMockUser(id = s"User3", sessions = Array(
          // has events that match only accorss sessions
          UnityMockSession(id = 1, startTime = 9, sessionType = 1, events = Array(
            UnityMockEvent(id = 2, eventType = 1, startTime = 10),
            UnityMockEvent(id = 1, eventType = 1, startTime = 11),
            UnityMockEvent(id = 1, eventType = 1, startTime = 12)
          )),
          UnityMockSession(id = 2, startTime = 19, sessionType = 1, events = Array(
            UnityMockEvent(id = 1, eventType = 1, startTime = 20),
            UnityMockEvent(id = 6, eventType = 1, startTime = 21),
            UnityMockEvent(id = 1, eventType = 1, startTime = 22),
            UnityMockEvent(id = 5, eventType = 1, startTime = 23),
            UnityMockEvent(id = 3, eventType = 1, startTime = 24),
            UnityMockEvent(id = 4, eventType = 1, startTime = 25),
            UnityMockEvent(id = 1, eventType = 1, startTime = 26),
            UnityMockEvent(id = 3, eventType = 1, startTime = 27),
            UnityMockEvent(id = 7, eventType = 1, startTime = 28)
          )),
          UnityMockSession(id = 3, startTime = 29, sessionType = 1, events = Array(
            UnityMockEvent(id = 6, eventType = 1, startTime = 30),
            UnityMockEvent(id = 13, eventType = 1, startTime = 31),
            UnityMockEvent(id = 7, eventType = 1, startTime = 32)
          ))
        ))
      )
    )
    miniToAlloy(Array(mv1, mv2))
  }

  it should "do conversion funnel" in {
    //StaticSweep = new BEDC95D9126D140C7BB74EC5244689170
    // SerializeTraversal = true
    val source =
      """
        |funnel 'cf' conversion limit 100000 {
        |  step 1 when user.application.firstUse.sessionTime > 0 timing on user.application.firstUse.sessionTime
        |  1
        |} from schema Unity
        |select uniques(cf.paths.steps) as 'cohortSize',
        |       lastPathStepTime(cf) as 'timestamp'
        |from schema Unity, funnel cf
        |where lastPathIsComplete(cf)
        |""".stripMargin

    runTest(source, 98, 98, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("cohortSize")).asLong, row(names("timestamp")).asLong)
      }.sortBy(_._1)
      r should equal(Array((1,5)))
    })
  }

  it should "do simple conversion funnel" in {
    val source =
      """
        |funnel test conversion {
        |   step 2 when user.sessions.events.id in (1, 2)
        |   step 3 when user.sessions.events.id in (3, 4)
        |   step 4 when user.sessions.events.id in (5, 6)
        |   step 5 when user.sessions.events.id in (7, 8)
        |
        |    2  : 3 : 4 : 5
        |} from schema Unity
        |
        |select as query_test t.paths.steps.id as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 98, 98, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((2,12), (3,4), (4,2), (5,1)))
    })
  }

  it should "do simple conversion funnel with session stop" in {
    val source =
      """
        |funnel test conversion {
        |   step 1 when start of user.sessions
        |   step 2 when user.sessions.events.id in (1, 2)
        |   step 3 when user.sessions.events.id in (3, 4)
        |   step 4 when user.sessions.events.id in (5, 6)
        |   step 5 when user.sessions.events.id in (7, 8)
        |
        |    2  : 3 : 4 : 5
        |} from schema Unity
        |
        |select as query_test t.paths.steps.id as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 98, 98, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((2,12), (3,4), (4,2), (5,1)))
    })
  }

  it should "do simple conversion funnel ignoring partial paths" in {
    val source =
      """
        |funnel test conversion {
        |   step 1 when start of user.sessions
        |   step 2 when user.sessions.events.id in (1, 2)
        |   step 3 when user.sessions.events.id in (3, 4)
        |   step 4 when user.sessions.events.id in (5, 6)
        |   step 5 when user.sessions.events.id in (7, 8)
        |
        |    2  : 3 : 4 : 5
        |} from schema Unity
        |
        |select as query_test t.paths.steps.id as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test as t where t.paths.steps.isComplete
        |""".stripMargin

    runTest(source, 98, 98, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((2,1), (3,1), (4,1), (5,1)))
    })
  }
}
