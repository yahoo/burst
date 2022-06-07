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
class ParameterizedSegmentSpec extends EqlAlloyTestRunner {
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
        ))

      )
    )
    miniToAlloy(Array(mv))
  }

  it should "simple segment constant parameter test" in {
    val source =
      """
         | segment 'simple'(one: long, two: long, three: long) {
         |     segment 1 when user.sessions.id % $one == 0
         |     segment 2 when user.sessions.id % $two == 0
         |     segment 3 when user.sessions.id % $three == 0
         |     segment 4 when user.sessions.id % 5 == 0
         | } from schema Unity
         | select count(simple.members) as 'num',
         |        simple.members.id as 'id'
         | from schema Unity, segment 'simple'(2, 4, 3)
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

  it should "simple segment runtime parameter test" in {
    val source =
      """
        | segment 'simple'(one: long, two: long, three: long) {
        |     segment 1 when user.sessions.id % $one == 0
        |     segment 2 when user.sessions.id % $two == 0
        |     segment 3 when user.sessions.id % $three == 0
        |     segment 4 when user.sessions.id % 5 == 0
        | } from schema Unity
        | select(one: long, two: long, three: long) count(simple.members) as 'num',
        |        simple.members.id as 'id'
        | from schema Unity, segment 'simple'($one, $two, $three)
       """.stripMargin
    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((1,10), (2,4), (3,6), (4,4)))
    }, s"""{"one": 2, "two": 4, "three": 3}""")
  }

  it should "simple segment runtime parameter in where clause test" in {
    val source =
      """
        | segment 'simple'(one: long, two: long, three: long) {
        |     segment 1 when user.sessions.id % $one == 0
        |     segment 2 when user.sessions.id % $two == 0
        |     segment 3 when user.sessions.id % $three == 0
        |     segment 4 when user.sessions.id % 5 == 0
        | } from schema Unity
        | select(one: long, two: long, three: long, four: long) count(simple.members) as 'num',
        |        simple.members.id as 'id'
        | from schema Unity, segment 'simple'($one, $two, $three) where user.sessions.id != $four
       """.stripMargin
    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((1,8), (2,2), (3,6), (4,4)))
    }, s"""{"one": 2, "two": 4, "three": 3, "four": 8}""")
  }

  it should "simple segment with cube test" in {
    val source =
      """
         | segment 'simple'(f: long, s: long) {
         |     segment 1 when user.sessions.id % $f == 0
         |     segment 2 when user.sessions.id % $s == 0
         |     segment 3 when user.sessions.id % 3 == 0
         |     segment 4 when user.sessions.id % 5 == 0
         | } from schema Unity
         | select(f: long, s: long) count(user) as 'num',
         |        simple.members.id as 'id'
         | from schema Unity, segment 'simple'($f, $s)
       """.stripMargin
    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((1,2), (2,2), (3,2), (4,2)))
    }, s"""{"f": 2, "s": 4}""")
  }

}
