/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.segments

import org.burstsys.alloy.alloy.store.AlloyView
import org.burstsys.alloy.alloy.views.AlloyJsonUseCaseViews.miniToAlloy
import org.burstsys.alloy.views.UnitMiniView
import org.burstsys.alloy.views.unity.UnityUseCaseViews.unitySchema
import org.burstsys.brio.flurry.provider.unity._
import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.joda.time.DateTimeZone
import org.joda.time.MutableDateTime

/**
 */
final
class EqlSegmentRootSpec extends EqlAlloyTestRunner {
  val installTime: MutableDateTime = MutableDateTime.now(DateTimeZone.UTC)
  installTime.setMillis(0)
  private def dayTime(i: Integer) = {installTime.setDayOfYear(i); installTime.getMillis}
  override protected def localViews: Array[AlloyView] = {
    val mv = UnitMiniView(unitySchema, 99, 99,
      Array(
        UnityMockUser(id = s"User1",
          deviceModelId=3, application = UnityMockApplication(firstUse = UnityMockUse(sessionTime = dayTime(5))), sessions = Array(
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
        UnityMockUser(id = s"User2", deviceModelId=4, application = UnityMockApplication(firstUse = UnityMockUse(sessionTime = dayTime(6))), sessions = Array(
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
          )),
          UnityMockSession(id = 11, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 2, startTime = 30)
          ))
        ))
      )
    )
    miniToAlloy(Array(mv))
  }

  it should "simple segment test with evaluation at root level" in {
    val source =
      s"""
         | segment 'simple' {
         |     segment 1 when user.deviceModelId == 3 && user.application.firstUse.sessionTime > 0
         | } from schema Unity
         |
         | select count(user) as 'num'
         | from schema Unity, segment 'simple'
         | where simple.members.id in (1)
         |""".stripMargin
    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => row(names("num")).asLong
      }
      r should equal(Array(1))
    })
  }

  it should "segment test with control variables at root level" in {
    val source =
      s"""
         |segment 'simple' {
         |   segment 1 when ((count(user.sessions) where user.sessions.startTime >= 0) >= 10) && user.application.firstUse.sessionTime > 0
         |   segment 2 when ((count(user.sessions) where user.sessions.startTime > 0) >= 11) && user.deviceModelId == 3
         |   segment 3 when user.deviceModelId == 3 && user.application.firstUse.sessionTime > 0
         |} from schema Unity
         | select count(user) as 'num'
         | from schema Unity, segment 'simple'
         | where simple.members.id in (2)
         |""".stripMargin
    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => row(names("num")).asLong
      }
      r should equal(Array(1))
    })
  }
}
