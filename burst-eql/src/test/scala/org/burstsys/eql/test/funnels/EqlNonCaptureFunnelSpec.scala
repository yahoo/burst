/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.funnels

import org.burstsys.alloy.alloy.store.AlloyView
import org.burstsys.alloy.alloy.views.AlloyJsonUseCaseViews.miniToAlloy
import org.burstsys.alloy.views.UnitMiniView
import org.burstsys.alloy.views.unity.UnityUseCaseViews.unitySchema
import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.brio.flurry.provider.unity._

final
class EqlNonCaptureFunnelSpec extends EqlAlloyTestRunner {

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
        )),
        UnityMockUser(id = s"User3", sessions = Array(
          // has events that match only accorss sessions
          UnityMockSession(id = 1, startTime = 9, events = Array(
            UnityMockEvent(id = 2, eventType = 1, startTime = 10),
            UnityMockEvent(id = 1, eventType = 1, startTime = 11),
            UnityMockEvent(id = 1, eventType = 1, startTime = 12)
          )),
          UnityMockSession(id = 2, startTime = 19, events = Array(
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
          UnityMockSession(id = 3, startTime = 29, events = Array(
            UnityMockEvent(id = 6, eventType = 1, startTime = 30),
            UnityMockEvent(id = 13, eventType = 1, startTime = 31),
            UnityMockEvent(id = 7, eventType = 1, startTime = 32)
          ))
        ))
      )
    )
    val mv3 = UnitMiniView(unitySchema, 97, 97,
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
          ))
        )),
        UnityMockUser(id = s"User2", sessions = Array(
          UnityMockSession(id = 1, startTime = 9, events = Array(
            UnityMockEvent(id = 1, eventType = 1, startTime = 10),
            UnityMockEvent(id = 3, eventType = 1, startTime = 15),
            UnityMockEvent(id = 5, eventType = 1, startTime = 23)
          )),
          UnityMockSession(id = 3, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 1, startTime = 30),
            UnityMockEvent(id = 13, eventType = 1, startTime = 31),
            UnityMockEvent(id = 15, eventType = 1, startTime = 32)
          ))
        )),
        UnityMockUser(id = s"User3", sessions = Array(
          // has events that match only accorss sessions
          UnityMockSession(id = 1, startTime = 9, events = Array(
            UnityMockEvent(id = 2, eventType = 1, startTime = 10),
            UnityMockEvent(id = 1, eventType = 1, startTime = 11),
            UnityMockEvent(id = 1, eventType = 1, startTime = 12)
          )),
          UnityMockSession(id = 3, startTime = 29, events = Array(
            UnityMockEvent(id = 6, eventType = 1, startTime = 30),
            UnityMockEvent(id = 13, eventType = 1, startTime = 31),
            UnityMockEvent(id = 7, eventType = 1, startTime = 32)
          ))
        ))
      )
    )
    miniToAlloy(Array(mv1, mv2, mv3))
  }

  //StaticSweep = null // new B1DD228162F7747278E2BE0B740144A7F
  //SerializeTraversal = false // true

  // repeat of a test but helps with a sanity check for later non-capturing group
  it should "do simple conversion funnel" in {
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
    //|    2  : (?:(2 | 4 | 5)*): 3 : (?:(2 | 3 | 5)*) : 4: (?:(2 | 3 | 4)*) : 5
    //|    2 : (?:[^3]*): 3 : (?:[^4]*) : 4: (?:[^5]*) : 5

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((2,6), (3,3), (4,2), (5,1)))
    })
  }

  it should "do simple transaction funnel" in {
    val source =
      """
        |funnel test transaction {
        |   step 1 when start of user.sessions
        |   step 2 when user.sessions.events.id in (1, 2)
        |   step 3 when user.sessions.events.id in (3, 4)
        |   step 4 when user.sessions.events.id in (5, 6)
        |   step 5 when user.sessions.events.id in (7, 8)
        |
            2  : (?:(2 | 4 | 5)*): 3 : (?:(2 | 3 | 5)*) : 4: (?:(2 | 3 | 4)*) : 5
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
      r should equal(Array((2,4), (3,4), (4,4), (5,2)))
    })
  }

  it should "do simple bracketed transaction funnel" in {
    val source =
      """
        |funnel test transaction {
        |   step 1 when start of user.sessions
        |   step 2 when user.sessions.events.id in (1, 2)
        |   step 3 when user.sessions.events.id in (3, 4)
        |   step 4 when user.sessions.events.id in (5, 6)
        |   step 5 when user.sessions.events.id in (7, 8)
        |
        |   2 : (?:[^3 1]*): 3 : (?:[^4 1]*) : 4: (?:[^5 1]*) : 5
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
      r should equal(Array((2,4), (3,4), (4,4), (5,2)))
    })
  }

  it should "do simple bracketed conversion funnel" in {
    val source =
      """
        |funnel test conversion {
        |   step 2 when user.sessions.events.id in (1, 2)
        |   step 3 when user.sessions.events.id in (3, 4)
        |   step 4 when user.sessions.events.id in (5, 6)
        |
        |   2 : (?:[^3]*): 3 : (?:[^4]*) : 4
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
      r should equal(Array((2,3), (3,3), (4,3)))
    })
  }

  it should "do simple cross session transaction funnel" in {
    val source =
      """
        |funnel test transaction {
        |   step 1 when start of user.sessions
        |   step 2 when user.sessions.events.id in (1, 2)
        |   step 3 when user.sessions.events.id in (3, 4)
        |   step 4 when user.sessions.events.id in (5, 6)
        |   step 5 when user.sessions.events.id in (7, 8)
        |
        |   2 : (?:[^3]*): 3 : (?:[^4]*) : 4: (?:[^5]*) : 5
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
      r should equal(Array((2,4), (3,4), (4,3), (5,2)))
    })

    runTest(source, 98, 98, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((2,5), (3,5), (4,4), (5,3)))
    })
  }

  it should "do step complete only cross session transaction funnel" in {
    val source =
      """
        |funnel test transaction {
        |   step 1 when start of user.sessions
        |   step 2 when user.sessions.events.id in (1, 2)
        |   step 3 when user.sessions.events.id in (3, 4)
        |   step 4 when user.sessions.events.id in (5, 6)
        |   step 5 when user.sessions.events.id in (7, 8)
        |
        |   2 : (?:[^3]*): 3 : (?:[^4]*) : 4: (?:[^5]*) : 5
        |} from schema Unity
        |
        |select as query_test t.paths.steps.id as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test as t where t.paths.steps.isComplete
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((2,2), (3,2), (4,2), (5,2)))
    })

    runTest(source, 98, 98, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((2,3), (3,3), (4,3), (5,3)))
    })
  }

  it should "do path complete only cross session transaction funnel" in {
    val source =
      """
        |funnel test transaction {
        |   step 1 when start of user.sessions
        |   step 2 when user.sessions.events.id in (1, 2)
        |   step 3 when user.sessions.events.id in (3, 4)
        |   step 4 when user.sessions.events.id in (5, 6)
        |   step 5 when user.sessions.events.id in (7, 8)
        |
        |   2 : (?:[^3]*): 3 : (?:[^4]*) : 4: (?:[^5]*) : 5
        |} from schema Unity
        |
        |select as query_test t.paths.steps.id as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test as t where t.paths.isComplete
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((2,2), (3,2), (4,2), (5,2)))
    })

    runTest(source, 98, 98, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((2,3), (3,3), (4,3), (5,3)))
    })
  }

  it should "do time constrained funnel" in {
    val source =
      """
        |funnel test transaction within 10 {
        |   step 2 when user.sessions.events.id in (1)
        |   step 3 when user.sessions.events.id in (3)
        |   step 4 when user.sessions.events.id in (5)
        |
        |    2 : (?:[^ 3]*) : 3 : (?:[^ 4]*) : 4
        |} from schema Unity
        |
        |select as query_test
        |     count(t.paths.steps) as num,
        |     t.paths.steps.id as id,
        |     t.paths.steps.time as 'time'
        |from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 97, 97, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("time")).asLong, row(names("num")).asLong)
      }.sortBy(_._2).sortBy(_._1)
      r should equal(Array((2,10,1), (2,11,1), (2,20,1), (3,15,1), (3,21,1), (4,22,1)))
    })
  }

  it should "do time constrained after step funnel" in {
    val source =
      """
        |funnel test transaction  {
        |   step 2 when user.sessions.events.id in (1)
        |   step 3 when user.sessions.events.id in (3)
        |   step 4 when user.sessions.events.id in (5) timing on user.sessions.events.startTime after 2
        |
        |    2 : (?:[^ 3]*) : 3 : (?:[^ 4]*) : 4
        |} from schema Unity
        |
        |select as query_test
        |     count(t.paths.steps) as num,
        |     t.paths.steps.id as id,
        |     t.paths.steps.time as 'time'
        |from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 97, 97, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("time")).asLong, row(names("num")).asLong)
      }.sortBy(_._2).sortBy(_._1)
      r should equal(Array((2,10,1), (2,11,1), (2,20,1), (3,15,1), (3,21,1), (4,23,1)))
    })
  }

  it should "do time constrained within step funnel" in {
    val source =
      """
        |funnel test transaction  {
        |   step 2 when user.sessions.events.id in (1)
        |   step 3 when user.sessions.events.id in (3)
        |   step 4 when user.sessions.events.id in (5) timing on user.sessions.events.startTime within 2
        |
        |    2 : (?:[^ 3]*) : 3 : (?:[^ 4]*) : 4
        |} from schema Unity
        |
        |select as query_test
        |     count(t.paths.steps) as num,
        |     t.paths.steps.id as id,
        |     t.paths.steps.time as 'time'
        |from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 97, 97, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("time")).asLong, row(names("num")).asLong)
      }.sortBy(_._2).sortBy(_._1)
      r should equal(Array((2,10,1), (2,11,1), (2,20,1), (3,15,1), (3,21,1), (4,22,1)))
    })
  }

  it should "do funnel with repeated first steps in match" in {
    val source =
      """
        |funnel test transaction {
        |   step 2 when user.sessions.events.id in (1)
        |   step 3 when user.sessions.events.id in (3)
        |   step 4 when user.sessions.events.id in (5)
        |
        |    2 : (?:[^ 3]*) : 3 : (?:[^ 4]*) : 4
        |} from schema Unity
        |
        |select as query_test
        |     count(t.paths.steps) as num,
        |     t.paths.steps.id as id,
        |     t.paths.steps.time as 'time'
        |from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 97, 97, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("time")).asLong, row(names("num")).asLong)
      }.sortBy(_._2).sortBy(_._1)
      r should equal(Array((2,10,1), (2,11,1), (2,20,1), (3,15,1), (3,21,1), (4,22,1), (4,23,1)))
    })
  }
}
