/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.funnels

import org.burstsys.alloy.alloy.store.AlloyView
import org.burstsys.alloy.alloy.views.AlloyJsonUseCaseViews.miniToAlloy
import org.burstsys.alloy.views.UnitMiniView
import org.burstsys.alloy.views.unity.UnityUseCaseViews.unitySchema
import org.burstsys.brio.flurry.provider.unity._
import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.hydra.runtime.SerializeTraversal
import org.joda.time.DateTimeZone
import org.joda.time.MutableDateTime

import scala.language.postfixOps


final
class EqlDiscrepencyFunnelSpec extends EqlAlloyTestRunner {
  val itemCount = 4

  val installTime: MutableDateTime = MutableDateTime.now(DateTimeZone.UTC)
  installTime.setMillis(0)
  private def dayTime(i: Integer) = {installTime.setDayOfYear(i); installTime.getMillis}

  override protected lazy val localViews: Array[AlloyView] = {
    val mv1 = {
      UnitMiniView(unitySchema, 97, 97,
        items = {
          val sesssions = Array(
            UnityMockSession(id = 1, startTime = 9, events = Array(
              UnityMockEvent(id = 1, eventType = 1, startTime = 10),
              UnityMockEvent(id = 3, eventType = 1, startTime = 11),
              UnityMockEvent(id = 7, eventType = 1, startTime = 12)
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
            )),
            UnityMockSession(id = 4, startTime = 79, events = Array(
              UnityMockEvent(id = 1, eventType = 1, startTime = 80),
              UnityMockEvent(id = 3, eventType = 1, startTime = 81),
              UnityMockEvent(id = 7, eventType = 1, startTime = 82)
            ))
          )
          (1 to itemCount) map { i =>
            UnityMockUser(id = s"User$i", sessions = sesssions,
              application =  UnityMockApplication(firstUse = UnityMockUse(sessionTime = dayTime(i))))
          }
        } toArray
      )
    }
    val mv3 = {
      UnitMiniView(unitySchema, 96, 96,
        items = {
          val sesssions = Array(
            UnityMockSession(id = 1, startTime = 9, events = Array(
              UnityMockEvent(id = 1, eventType = 1, startTime = 10),
              UnityMockEvent(id = 3, eventType = 1, startTime = 11),
              UnityMockEvent(id = 7, eventType = 1, startTime = 12)
            )),
            UnityMockSession(id = 2, startTime = 19, events = Array(
              UnityMockEvent(id = 1, eventType = 1, startTime = 20),
              UnityMockEvent(id = 5, eventType = 1, startTime = 122)
            )),
            UnityMockSession(id = 3, startTime = 29, events = Array(
              UnityMockEvent(id = 3, eventType = 1, startTime = 31),
              UnityMockEvent(id = 11, eventType = 1, startTime = 130),
              UnityMockEvent(id = 13, eventType = 1, startTime = 131),
              UnityMockEvent(id = 15, eventType = 1, startTime = 132)
            )),
            UnityMockSession(id = 4, startTime = 39, events = Array(
              UnityMockEvent(id = 5, eventType = 1, startTime = 42),
              UnityMockEvent(id = 1, eventType = 1, startTime = 140),
              UnityMockEvent(id = 3, eventType = 1, startTime = 141),
              UnityMockEvent(id = 7, eventType = 1, startTime = 142)
            ))
          )
          (1 to itemCount) map { i =>
            UnityMockUser(id = s"User$i", sessions = sesssions map {s =>
              if (s.id > i%4 + 1)
                s.copy(startTime = s.startTime + 1000 )
              else
                s
            })
          }
        } toArray
      )
    }
    miniToAlloy(Array(mv1, mv3))
  }

  it should "apply global predicate to entire funnel success" in {
    val source =
      s"""
        |funnel 'simpleFunnel' conversion limit 10000 {
        |   step 0 when user.sessions.events.id == 1
        |   step 1 when user.sessions.events.id == 3
        |   step 2 when user.sessions.events.id == 5
        |   0 : (?:[^1]*) : 1 : (?:[^2]*) : 2
        |} from schema unity
        |
        |select f.paths.steps.id as stepId, count(f.paths.steps) as occurrences
        |from schema unity, funnel 'simpleFunnel' as f
        |where user.application.firstUse.sessionTime >= ${dayTime(2)} and user.application.firstUse.sessionTime <= ${dayTime(3)}
        |""".stripMargin

    runTest(source, 97, 97, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("stepId")).asLong, row(names("occurrences")).asLong)
      }.sortBy(_._1)
      r should equal(Array((0,2), (1,2), (2,2)))
    })
  }

  it should "deal with segments and funnels in query" in {
    val source =
      s"""
      | funnel 'f1' conversion [[ LOOSE_MATCH ]] {
      |   step 1 when user.application.firstUse.sessionTime > 0  timing on user.application.firstUse.sessionTime
      |   step 2 when (user.sessions.events.id == 24216567 && user.sessions.events.parameters['Item'] in ('RobotRoom1','WorkshopRoom1','MissionRoom1','VaultRoom1','ResearchRoom1','WorkshopRoom2','RobotRoom2','MissionRoom2','VaultRoom2','ResearchRoom2','VaultRoom3','VaultRoom4') && user.sessions.sessionType == 1)  timing on user.sessions.events.startTime
      |   1:2
      |} from schema Unity
      |segment 'sgmnt' {
      |   segment 607365 when ((user.application.mostUse.countryId in (435670,435623)))
      |} from schema Unity
      |select count(f1.paths.steps) as 'count', f1.paths.steps.id as 'stepNo'
      |from schema Unity, funnel f1, segment sgmnt where sgmnt.members.id in (607365)
      |""".stripMargin

    runTest(source, 97, 97, { result =>
      val resultSet = checkResults(result)
      val _ = resultSet.columnNames.zipWithIndex.toMap
    })
  }

  it should "apply global predicate to session starts and ends" in {
    val source =
      """
        |funnel 'simpleFunnel' conversion limit 10000 {
        |   step 40 when start of user.sessions
        |   step 0 when user.sessions.events.id == 1
        |   step 1 when user.sessions.events.id == 3
        |   step 2 when user.sessions.events.id == 5
        |   step 41 when end of user.sessions
        |   40: (?:[^0]*) : 0 : (?:[^1]*) : 1 : (?:[^2]*) : 2 : (?:[^41]*) : 41
        |} from schema unity
        |
        |select f.paths.steps.id as stepId, count(f.paths.steps) as occurrences
        |from schema unity, funnel 'simpleFunnel' as f
        |where user.sessions.startTime >= 19 and user.sessions.startTime <= 100
        |""".stripMargin

    runTest(source, 97, 97, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("stepId")).asLong, row(names("occurrences")).asLong)
      }.sortBy(_._1)
      r should equal(Array((0,itemCount), (1,itemCount), (2,itemCount), (40,itemCount), (41,itemCount)))
    })
  }

  it should "two session starts" in {
    val source =
      """
        |funnel test conversion {
        |   step 1 when user.sessions.startTime > 0
        |   step 2 when user.sessions.startTime > 0
        |
        |    1  : (?:[^2]*) : 2
        |} from schema Unity
        |
        |select as query_test t.paths.steps.id as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 97, 97, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((1,itemCount), (2,itemCount)))
    })
  }

  it should "multiple session starts" in {
    val source =
      """
        |funnel test conversion {
        |   step 1 when user.sessions.startTime > 0
        |   step 2 when user.sessions.startTime > 0
        |   step 3 when user.sessions.startTime > 0
        |
        |    1  : (?:[^2]*) : 2 : (?:[^3]*) : 3
        |} from schema Unity
        |
        |select as query_test t.paths.steps.id as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 97, 97, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((1,itemCount)))
    })
  }

  it should "multiple reversed session starts" in {
    val source =
      """
        |funnel test conversion {
        |   step 3 when user.sessions.startTime > 0
        |   step 2 when user.sessions.startTime > 0
        |   step 1 when user.sessions.startTime > 0
        |
        |    1  : (?:[^2]*) : 2 : (?:[^3]*) : 3
        |} from schema Unity
        |
        |select as query_test t.paths.steps.ordinal as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 97, 97, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((0,itemCount)))
    })
  }

  it should "repeated same step session starts" in {
    val source =
      """
        |funnel test conversion {
        |   step 3 when user.sessions.startTime > 0
        |   step 2 when user.sessions.startTime > 0
        |   step 1 when user.sessions.startTime > 0
        |
        |    1  : 1 : 1
        |} from schema Unity
        |
        |select as query_test t.paths.steps.ordinal as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 97, 97, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((0,itemCount), (1,itemCount), (2,itemCount)))
    })
  }

  it should "match successive events except when global condition fails" in {
    val source =
      """
        |funnel 'simpleFunnel' conversion limit 10000 {
        |   step 0 when user.sessions.events.id == 1
        |   step 1 when user.sessions.events.id == 3
        |   step 2 when user.sessions.events.id == 5
        |   0 : (?:[^1]*) : 1 : (?:[^2]*) : 2
        |} from schema unity
        |
        |select f.paths.steps.id as stepId, count(f.paths.steps) as occurrences
        |from schema unity, funnel 'simpleFunnel' as f
        |where user.sessions.startTime >= 19 and user.sessions.startTime <= 100
        |""".stripMargin

    runTest(source, 97, 97, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("stepId")).asLong, row(names("occurrences")).asLong)
      }.sortBy(_._1)
      r should equal(Array((0,itemCount), (1,itemCount), (2,itemCount)))
    })
  }

  it should "match successive events except when local condition fails" in {
    SerializeTraversal = true // true
    val source =
      """
        |funnel 'simpleFunnel' conversion limit 10000 {
        |   step 0 when user.sessions.events.id == 1 and user.sessions.startTime >= 19 and user.sessions.startTime <= 100
        |   step 1 when user.sessions.events.id == 3 and user.sessions.startTime >= 19 and user.sessions.startTime <= 100
        |   step 2 when user.sessions.events.id == 5 and user.sessions.startTime >= 19 and user.sessions.startTime <= 100
        |   0 : (?:[^1]*) : 1 : (?:[^2]*) : 2
        |} from schema unity
        |
        |select f.paths.steps.id as stepId, count(f.paths.steps) as occurrences
        |from schema unity, funnel 'simpleFunnel' as f
        |""".stripMargin

    runTest(source, 97, 97, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("stepId")).asLong, row(names("occurrences")).asLong)
      }.sortBy(_._1)
      r should equal(Array((0,itemCount), (1,itemCount), (2,itemCount)))
    })
  }

  it should "match cross session events wth global condition" in {
    val source =
      """
        |funnel 'simpleFunnel' conversion limit 10000 {
        |   step 0 when user.sessions.events.id == 1 timing on user.sessions.startTime + user.sessions.events.startTime
        |   step 1 when user.sessions.events.id == 3 timing on user.sessions.startTime + user.sessions.events.startTime
        |   step 2 when user.sessions.events.id == 5 timing on user.sessions.startTime + user.sessions.events.startTime
        |   0 : (?:[^1]*) : 1 : (?:[^2]*) : 2
        |} from schema unity
        |
        |select f.paths.steps.id as stepId, count(f.paths.steps) as occurrences
        |from schema unity, funnel 'simpleFunnel' as f
        |where user.sessions.startTime >= 19 and user.sessions.startTime <= 100
        |""".stripMargin

    runTest(source, 96, 96, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("stepId")).asLong, row(names("occurrences")).asLong)
      }.sortBy(_._1)
      r should equal(Array((0,itemCount*3/4), (1,itemCount/2), (2,itemCount/4)))
    })
  }

  it should "match cross session events wth local step condition" in {
    val source =
      """
        |funnel 'simpleFunnel' conversion {
        |   step 0 when user.sessions.events.id == 1 and user.sessions.startTime >= 19 and user.sessions.startTime <= 100
        |   step 1 when user.sessions.events.id == 3 and user.sessions.startTime >= 19 and user.sessions.startTime <= 100
        |   step 2 when user.sessions.events.id == 5 and user.sessions.startTime >= 19 and user.sessions.startTime <= 100
        |   0 : (?:[^1]*) : 1 : (?:[^2]*) : 2
        |} from schema unity
        |
        |select f.paths.steps.id as stepId, count(f.paths.steps) as occurrences
        |from schema unity, funnel 'simpleFunnel' as f
        |""".stripMargin

    runTest(source, 96, 96, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("stepId")).asLong, row(names("occurrences")).asLong)
      }.sortBy(_._1)
      r should equal(Array((0,itemCount*3/4), (1,itemCount/2), (2,itemCount/4)))
    })
  }

  it should "match parametrized cross session events wth local step condition" in {
    val source =
      """
        |funnel 'simpleFunnel'(fst: long, fet: long, fs1: long, fs2: long, fs3: long) conversion {
        |   step 0 when user.sessions.events.id == $fs1 and user.sessions.startTime >= $fst and user.sessions.startTime <= $fet
        |   step 1 when user.sessions.events.id == $fs2 and user.sessions.startTime >= $fst and user.sessions.startTime <= $fet
        |   step 2 when user.sessions.events.id == $fs3 and user.sessions.startTime >= $fst and user.sessions.startTime <= $fet
        |   0 : (?:[^1]*) : 1 : (?:[^2]*) : 2
        |} from schema unity
        |
        |select (st: long, et: long, s1: long, s2: long, s3: long) f.paths.steps.id as stepId, f.paths.steps.ordinal as stepOrdinal, count(f.paths.steps) as occurrences
        |from schema unity, funnel 'simpleFunnel'($st, $et, $s1, $s2, $s3) as f
        |""".stripMargin

    runTest(source, 96, 96, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("stepId")).asLong, row(names("stepOrdinal")).asLong, row(names("occurrences")).asLong)
      }.sortBy(_._1)
      r should equal(Array((0, 0, itemCount*3/4), (1, 1,itemCount/2), (2, 2, itemCount/4)))
    }, s"""{"st": 19, "et": 100, "s1": 1, "s2": 3, "s3": 5}""" )
  }

  it should "match parametrized cross session events wth global condition" in {
    val source =
      """
        |funnel 'simpleFunnel'(fs1: long, fs2: long, fs3: long) conversion {
        |   step 0 when user.sessions.events.id == $fs1
        |   step 1 when user.sessions.events.id == $fs2
        |   step 2 when user.sessions.events.id == $fs3
        |   0 : (?:[^1]*) : 1 : (?:[^2]*) : 2
        |} from schema unity
        |
        |select (st: long, et: long, s1: long, s2: long, s3: long) f.paths.steps.id as stepId, f.paths.steps.ordinal as stepOrdinal, count(f.paths.steps) as occurrences
        |from schema unity, funnel 'simpleFunnel'($s1, $s2, $s3) as f where user.sessions.startTime >= $st and user.sessions.startTime <= $et
        |""".stripMargin

    runTest(source, 96, 96, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("stepId")).asLong, row(names("stepOrdinal")).asLong, row(names("occurrences")).asLong)
      }.sortBy(_._1)
      r should equal(Array((0, 0, itemCount*3/4), (1, 1,itemCount/2), (2, 2, itemCount/4)))
    }, s"""{"st": 19, "et": 100, "s1": 1, "s2": 3, "s3": 5}""" )
  }

  it should "match parametrized loose match cross session events wth global condition" in {
    val source =
      """
        |funnel 'simpleFunnel'(fs1: long, fs2: long, fs3: long) conversion [[ LOOSE_MATCH ]] {
        |   step 0 when user.sessions.events.id == $fs1
        |   step 1 when user.sessions.events.id == $fs2
        |   step 2 when user.sessions.events.id == $fs3
        |   0 : 1 : 2
        |} from schema unity
        |
        |select (st: long, et: long, s1: long, s2: long, s3: long) f.paths.steps.id as stepId, f.paths.steps.ordinal as stepOrdinal, count(f.paths.steps) as occurrences
        |from schema unity, funnel 'simpleFunnel'($s1, $s2, $s3) as f where user.sessions.startTime >= $st and user.sessions.startTime <= $et
        |""".stripMargin

    runTest(source, 96, 96, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("stepId")).asLong, row(names("stepOrdinal")).asLong, row(names("occurrences")).asLong)
      }.sortBy(_._1)
      r should equal(Array((0, 0, itemCount*3/4), (1, 1,itemCount/2), (2, 2, itemCount/4)))
    }, s"""{"st": 19, "et": 100, "s1": 1, "s2": 3, "s3": 5}""" )
  }
}
