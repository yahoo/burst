/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.funnels

import org.burstsys.alloy.alloy.store.AlloyView
import org.burstsys.alloy.alloy.views.AlloyJsonUseCaseViews.miniToAlloy
import org.burstsys.alloy.views.UnitMiniView
import org.burstsys.alloy.views.unity.UnityUseCaseViews.unitySchema
import org.burstsys.brio.flurry.provider.unity.UnityMockEvent
import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.hydra.runtime.{SerializeTraversal, StaticSweep}
import org.burstsys.brio.flurry.provider.unity._
import org.joda.time.{DateTimeZone, MutableDateTime}

final
class EqlRetentionFunnelSpec extends EqlAlloyTestRunner {

  val installTime: MutableDateTime = MutableDateTime.now(DateTimeZone.UTC)
  installTime.setMillis(0)
  private def dayTime(i: Integer) = {installTime.setDayOfYear(i); installTime.getMillis}
  override protected lazy val localViews: Array[AlloyView] = {
    val mv1 = UnitMiniView(unitySchema, 99, 99,
      Array(
        UnityMockUser(id = s"User1", deviceModelId=3,
          application = UnityMockApplication(firstUse = UnityMockUse(sessionTime = dayTime(5))),
          sessions = Array(
          UnityMockSession(id = 1, startTime = 9, events = Array(
            UnityMockEvent(id = 2, eventType = 1, startTime = dayTime(10)),
            UnityMockEvent(id = 4, eventType = 1, startTime = dayTime(11)),
            UnityMockEvent(id = 6, eventType = 1, startTime = dayTime(12)),
            UnityMockEvent(id = 8, eventType = 1, startTime = dayTime(13))
          )),
          UnityMockSession(id = 2, startTime = 19, events = Array(
            UnityMockEvent(id = 1, eventType = 1, startTime = dayTime(20)),
            UnityMockEvent(id = 3, eventType = 1, startTime = dayTime(21)),
            UnityMockEvent(id = 5, eventType = 1, startTime = dayTime(22))
          )),
          UnityMockSession(id = 3, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 1, startTime = dayTime(30)),
            UnityMockEvent(id = 13, eventType = 1, startTime = dayTime(31)),
            UnityMockEvent(id = 15, eventType = 1, startTime = dayTime(32))
          ))
        )),
        UnityMockUser(id = s"User2", deviceModelId= 8,
          application = UnityMockApplication(firstUse = UnityMockUse(sessionTime = dayTime(8))),
          sessions = Array(
          UnityMockSession(id = 1, startTime = 9, events = Array(
            UnityMockEvent(id = 2, eventType = 1, startTime = dayTime(10)),
            UnityMockEvent(id = 4, eventType = 1, startTime = dayTime(11)),
            UnityMockEvent(id = 6, eventType = 1, startTime = dayTime(12))
          )),
          // match events have lots significant events that should be ignored
          UnityMockSession(id = 2, startTime = 19, events = Array(
            UnityMockEvent(id = 1, eventType = 1, startTime = dayTime(20)),
            UnityMockEvent(id = 1, eventType = 1, startTime = dayTime(21)),
            UnityMockEvent(id = 1, eventType = 1, startTime = dayTime(22)),
            UnityMockEvent(id = 7, eventType = 1, startTime = dayTime(23)),
            UnityMockEvent(id = 3, eventType = 1, startTime = dayTime(24)),
            UnityMockEvent(id = 5, eventType = 1, startTime = dayTime(25)),
            UnityMockEvent(id = 1, eventType = 1, startTime = dayTime(26)),
            UnityMockEvent(id = 3, eventType = 1, startTime = dayTime(27)),
            UnityMockEvent(id = 7, eventType = 1, startTime = dayTime(28))
          )),
          UnityMockSession(id = 3, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 1, startTime = dayTime(30)),
            UnityMockEvent(id = 13, eventType = 1, startTime = dayTime(31)),
            UnityMockEvent(id = 15, eventType = 1, startTime = dayTime(32))
          ))
        ))
      )
    )
    miniToAlloy(Array(mv1))
  }

  it should "do super simple conversion funnel" in {
    StaticSweep = null // new B1DD228162F7747278E2BE0B740144A7F
    SerializeTraversal = false // true

    val source =
      """
        |    funnel 'cohort' conversion {
        |       step 1 when start of user timing on user.deviceModelId
        |       1
        |    } from schema unity
        |
        |    funnel 'measurement' transaction {
        |       step 2 when start of user.sessions
        |       2
        |    } from schema unity
        |
        |    select count(mf.paths.steps) as 'users',
        |           lastPathStepTime(cf) as chort,
        |           mf.paths.steps.id as id
        |    where lastPathIsComplete(cf) && mf.paths.steps.isLast
        |    from funnel 'cohort' as 'cf', funnel 'measurement' as mf, schema unity
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("chort")).asLong, row(names("users")).asLong)
      }.sortBy(_._1).sortBy(_._2).sortBy(_._3)
      r should equal(Array((2,3,3), (2,8,3)))
    })
  }

  it should "do almost conversion funnel" in {
    val source =
      """
        |    funnel 'cohort_f' conversion {
        |       step 1 when start of user timing on user.application.firstUse.sessionTime
        |       1
        |    } from schema unity
        |
        |    funnel 'measurement_f' transaction {
        |       step 2 when user.sessions.events.id in (2)
        |       step 3 when user.sessions.events.id in (4)
        |       step 4 when user.sessions.events.id in (6)
        |       step 5 when user.sessions.events.id in (8)
        |
        |       2 : (?:[^3]*): 3 : (?:[^4]*) : 4: (?:[^5]*) : 5
        |    } from schema unity
        |
        |    select unique(mf.paths.steps) as 'users',
        |           count(mf.paths.steps) as 'times',
        |           dayofyear(lastPathStepTime(cf)) as cohort,
        |           dayofyear(mf.paths.steps.time) - dayofyear(lastPathStepTime(cf)) as measure
        |    from funnel 'cohort_f' as 'cf', funnel 'measurement_f' as mf, schema unity
        |    where  lastPathIsComplete(cf) && mf.paths.steps.isLast
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("users")).asLong, row(names("times")).asLong, row(names("cohort")).asLong, row(names("measure")).asLong)
      }.sortBy(_._1).sortBy(_._2).sortBy(_._3).sortBy(_._4)
      r should equal(Array((1,1,4,8)))
    })
  }

  it should "do simple conversion funnel" in {
    val source =
      """
        |    funnel 'cohort_f' conversion {
        |       step 1 when start of user timing on user.application.firstUse.sessionTime
        |       1
        |    } from schema unity
        |
        |    funnel 'measurement_f' transaction {
        |       step 2 when user.sessions.events.id in (1, 2)
        |       step 3 when user.sessions.events.id in (3, 4)
        |       step 4 when user.sessions.events.id in (5, 6)
        |
        |       2 : (?:[^3]*): 3 : (?:[^4]*) : 4
        |    } from schema unity
        |
        |    select unique(mf.paths.steps) as 'users',
        |           count(mf.paths.steps) as 'times',
        |           dayofyear(lastPathStepTime(cf)) as cohort,
        |           dayofyear(mf.paths.steps.time) - dayofyear(lastPathStepTime(cf)) as measure
        |    from funnel 'cohort_f' as 'cf', funnel 'measurement_f' as mf, schema unity
        |    where dayofyear(mf.paths.steps.time) - dayofyear(lastPathStepTime(cf)) between -30 and 30 &&
        |          lastPathIsComplete(cf) && mf.paths.steps.isLast
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("users")).asLong, row(names("times")).asLong, row(names("cohort")).asLong, row(names("measure")).asLong)
      }.sortBy(_._1).sortBy(_._2).sortBy(_._3).sortBy(_._4)
      r should equal(Array((1,1,7,4), (1,1,4,7), (1,1,4,17), (1,1,7,17)))
    })
  }

  it should "do conversion funnel with parameter tests" in {
    val source =
      """
        | funnel 'tf' transaction limit 100000 {
        |    step 1 when (user.sessions.events.id > 0 && user.sessions.events.parameters['fl.ProductName'] is null  )  timing on user.sessions.events.startTime
        |    1
        | } from schema Unity
        |
        | select uniques(tf.paths.steps) as 'count' from schema Unity, funnel tf
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => row(names("count")).asLong
      }.sortBy(r => r)
      r should equal(Array(2))
    })
  }
}
