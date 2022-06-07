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
class EqlSegmentSpec extends EqlAlloyTestRunner {
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

  it should "simple segment test" in {
    val source =
      s"""
         | segment 'simple' {
         |     segment 1 when user.sessions.id % 2 == 0
         |     segment 2 when user.sessions.id % 4 == 0
         |     segment 3 when user.sessions.id % 3 == 0
         |     segment 4 when user.sessions.id % 5 == 0
         | } from schema Unity
         | select count(simple.members) as 'num',
         |        simple.members.id as 'id'
         | from schema Unity, segment 'simple'
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

  it should "simple segment unique test" in {
    val source =
      s"""
         | segment 'simple' {
         |     segment 1 when user.sessions.id % 2 == 0
         |     segment 2 when user.sessions.id % 4 == 0
         |     segment 3 when user.sessions.id % 3 == 0
         |     segment 4 when user.sessions.id % 5 == 0
         | } from schema Unity
         | select unique(simple.members) as 'num',
         |        simple.members.id as 'id'
         | from schema Unity, segment 'simple'
       """.stripMargin
    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((1,2), (2,2), (3,2), (4,2)))
    })
  }

  it should "simple segment with cube test" in {
    val source =
      s"""
         | segment 'simple' {
         |     segment 1 when user.sessions.id % 2 == 0
         |     segment 2 when user.sessions.id % 4 == 0
         |     segment 3 when user.sessions.id % 3 == 0
         |     segment 4 when user.sessions.id % 5 == 0
         | } from schema Unity
         | select count(user) as 'num',
         |        simple.members.id as 'id'
         | from schema Unity, segment 'simple'
       """.stripMargin
    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((1,2), (2,2), (3,2), (4,2)))
    })
  }

  it should "segment with global where" in {
    val source =
      s"""
         | segment 'simple' {
         |     segment 1 when user.sessions.id % 2 == 0
         |     segment 2 when user.sessions.id % 2 == 1
         | } from schema Unity
         | select count(user) as 'num',
         |        simple.members.id as 'id'
         | from schema Unity, segment 'simple' where (count(user.sessions.events) scope user.sessions where user.sessions.events.eventType == 1) > 0
       """.stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((1, 1), (2, 1)))
    })
  }
}
