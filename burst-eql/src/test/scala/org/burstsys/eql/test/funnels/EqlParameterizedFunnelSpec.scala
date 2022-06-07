/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.funnels

import org.burstsys.alloy.alloy.store.AlloyView
import org.burstsys.alloy.alloy.views.AlloyJsonUseCaseViews.miniToAlloy
import org.burstsys.alloy.views.UnitMiniView
import org.burstsys.alloy.views.unity.UnityUseCaseViews.unitySchema
import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.hydra.runtime.StaticSweep
import org.burstsys.brio.flurry.provider.unity._

final
class EqlParameterizedFunnelSpec extends EqlAlloyTestRunner {

  override protected lazy val localViews: Array[AlloyView] = {
    val mv = UnitMiniView(unitySchema, 99, 99,
      Array(
        UnityMockUser( id = s"User1", sessions = Array(
          UnityMockSession( id = 1, startTime= 9, events = Array(
            UnityMockEvent( id = 2, eventType = 1, startTime = 10 ),
            UnityMockEvent( id = 4, eventType = 1, startTime = 11 ),
            UnityMockEvent( id = 6, eventType = 1, startTime = 12)
          )),
          UnityMockSession( id = 2, startTime = 19, events = Array(
            UnityMockEvent( id = 1, eventType = 1, startTime = 20 ),
            UnityMockEvent( id = 3, eventType = 1, startTime = 21 ),
            UnityMockEvent( id = 5, eventType = 1, startTime = 22)
          )),
          UnityMockSession( id = 3, startTime = 29, events = Array(
            UnityMockEvent( id = 11, eventType = 1, startTime = 30 ),
            UnityMockEvent( id = 13, eventType = 1, startTime = 31 ),
            UnityMockEvent( id = 15, eventType = 1, startTime = 32)
          ))
        )),
        UnityMockUser( id = s"User2", sessions = Array(
          UnityMockSession( id = 7, startTime= 9, events = Array(
            UnityMockEvent( id = 2, eventType = 1, startTime = 10 ),
            UnityMockEvent( id = 4, eventType = 1, startTime = 11 ),
            UnityMockEvent( id = 6, eventType = 1, startTime = 12)
          )),
          UnityMockSession( id = 8, startTime = 19, events = Array(
            UnityMockEvent( id = 1, eventType = 1, startTime = 20 ),
            UnityMockEvent( id = 3, eventType = 1, startTime = 21 ),
            UnityMockEvent( id = 5, eventType = 1, startTime = 22)
          )),
          UnityMockSession( id = 9, startTime = 29, events = Array(
            UnityMockEvent( id = 11, eventType = 1, startTime = 30 ),
            UnityMockEvent( id = 13, eventType = 1, startTime = 31 ),
            UnityMockEvent( id = 15, eventType = 1, startTime = 32)
          ))
        ))
      )
    )
    miniToAlloy(Array(mv))
  }

  it should "do simple single parameter linear funnel" in {
    val source =
      """
        |funnel test(f: long) transaction {
        |   step 1 when user.sessions.events.id == $f
        |   step 2 when user.sessions.events.id in (3, 4, 13)
        |   step 99 when start of user.sessions
        |   1 : 2
        |} from schema Unity
        |
        |select(one: long) t.paths.steps.id as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test($one) as t
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong )
      }.sortBy(_._1)
      r should equal(Array((1,2), (2,2)))
    }, s"""{"one": 2}""")
  }

  it should "do simple multi-parameter linear funnel" in {
    val source =
      """
        |funnel test(f: long, s: long) transaction {
        |   step 1 when user.sessions.events.id == $f
        |   step 2 when user.sessions.events.id == $s
        |   step 3 when user.sessions.events.id in (5, 6, 15)
        |   step 99 when start of user.sessions
        |   1 : 2 : 3
        |} from schema Unity
        |
        |select(one: long, two: long) t.paths.steps.id as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test($one, $two) as t
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong )
      }.sortBy(_._1)
      r should equal(Array((1,2), (2,2), (3,2)))
    }, s"""{"one": 2, "two": 4}""")
  }

  it should "do simple multi-parameter linear funnel with global where" in {
    val source =
      """
        |funnel test(f: long, s: long) transaction {
        |   step 1 when user.sessions.events.id == $f
        |   step 2 when user.sessions.events.id == $s
        |   step 3 when user.sessions.events.id in (5, 6, 15)
        |   step 99 when start of user.sessions
        |   1 : 2 : 3
        |} from schema Unity
        |
        |select(one: long, two: long, three: long) t.paths.steps.id as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test($one, $two) as t where user.sessions.id != $three
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong )
      }.sortBy(_._1)
      r should equal(Array((1,1), (2,1), (3,1)))
    }, s"""{"one": 2, "two": 4, "three": 7}""")
  }

  it should "do parameterized funnel with cubed steps" in {
    StaticSweep = null
    val source =
      """
        |funnel test(f: long, s: long) transaction {
        |   step 1 when start of user.sessions
        |   step 2 when user.sessions.events.id in ($f, 2)
        |   step 3 when user.sessions.events.id in ($s, 4)
        |   step 4 when user.sessions.events.id in (5, 6)
        |
        |    1 : 2 : 3 : 4
        |} from schema Unity
        |
        |select(f: long, s: long) as query_test count(user) as number,
        |                     t.paths.steps.id as ids
        |   from schema Unity, funnel test($f, $s) as t
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("number")).asLong, row(names("ids")).asLong)
      }.sortBy(_._2)
      r should equal(Array((2,1), (2,2), (2,3), (2,4)))
    }, s"""{"f": 1, "s": 3}""")
  }

  it should "do parameterized funnel with besides" in {
    StaticSweep = null
    val source =
      """
        |funnel test(f: long, s: long) transaction {
        |   step 1 when start of user.sessions
        |   step 2 when user.sessions.events.id in ($f, 2)
        |   step 3 when user.sessions.events.id in ($s, 4)
        |   step 4 when user.sessions.events.id in (5, 6)
        |
        |    1 : 2 : 3 : 4
        |} from schema Unity
        |
        |select(f: long, s: long) as query_test count(user) as number,
        |                     t.paths.steps.id as ids
        |beside select count(user) where user.application.firstUse.sessionTime > $f
        |from schema Unity, funnel test($f, $s) as t
        |where user.sessions.startTime > $s
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("number")).asLong, row(names("ids")).asLong)
      }.sortBy(_._2)
      r should equal(Array((2,1), (2,2), (2,3), (2,4)))
    }, s"""{"f": 1, "s": 3}""")
  }

  it should "do parameterized multiple funnels with besides" in {
    StaticSweep = null
    val validResults = Array(
      Array((2,1), (2,1001)),
      Array((2,1), (2,2), (2,1001))
    )
    val source =
      """
        |funnel f1(s1: long, s2: long, e1: long, e2: long) transaction {
        |   step 1 when user.sessions.events.id==$s1
        |   step 1001 when user.sessions.events.id==$e1
        |   step 1002 when user.sessions.events.id==$e2
        |   1 : [1001 1002]
        |} from schema Unity
        |
        |funnel f2(s1: long, s2: long, e1: long, e2: long) transaction {
        |    step 1 when user.sessions.events.id==$s1
        |    step 2 when user.sessions.events.id==$s2
        |    step 1001 when user.sessions.events.id==$e1
        |    step 1002 when user.sessions.events.id==$e2
        |    1 : 2 : [1001 1002]
        | } from schema Unity
        |
        |select(s1: long, s2: long, e1: long, e2: long) as query_p1
        |      count(user) as number, f1.paths.steps.id as ids
        |beside select as query_p2 count(user) as number,  f2.paths.steps.id as ids
        |from schema Unity, funnel f1($s1, $s2, $e1, $e2), funnel f2($s1, $s2, $e1, $e2)
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      checkResults(result)
      val results = result.resultSets.values.filter(_.resultName.startsWith("query")).toArray.sortBy(_.resultName)

      for (i <- results.indices) {
        val resultSet = results(i)
        val names = resultSet.columnNames.zipWithIndex.toMap
        val r = resultSet.rowSet.map {
          row => (row(names("number")).asLong, row(names("ids")).asLong)
        }.sortBy(_._2)
        r should equal(validResults(i))
      }

    }, s"""{"s1": 1, "s2": 3, "e1": 5, "e2": 6}""")
  }
}
