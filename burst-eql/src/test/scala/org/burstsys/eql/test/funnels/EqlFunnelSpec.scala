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
class EqlFunnelSpec extends EqlAlloyTestRunner {

  override protected lazy val localViews: Array[AlloyView] = {
    val mv = UnitMiniView(unitySchema, 99, 99,
      Array(
        UnityMockUser(id = s"User1", sessions = Array(
          UnityMockSession(id = 1, startTime = 9, events = Array(
            UnityMockEvent(id = 2, eventType = 1, startTime = 10),
            UnityMockEvent(id = 4, eventType = 1, startTime = 11),
            UnityMockEvent(id = 6, eventType = 1, startTime = 12)
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
        ))
      )
    )
    miniToAlloy(Array(mv))
  }

  it should "do path funnel " in {
    StaticSweep = null
    val source =
      """
        |funnel "pathFunnel" transaction {
        |step 1001 when start of user.sessions
        |step 1 when user.sessions.events.id == 24053254
        |1001:(1)
        |
        |} from schema Unity
        |select count(pf.paths.steps) as "total", pf.paths.steps.id as "stepId"
        |from schema Unity, funnel pathFunnel as pf where (user.sessions.startTime >= 1615878000000 && user.sessions.startTime <= 1618469999000)
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val _ = resultSet.rowSet.map {
        row => (row(names("total")).asLong, row(names("stepId")).asLong)
      }
    })
  }

  it should "do simple conversion funnel" in {
    val source =
      """
        |funnel test conversion {
        |   step 1 when start of user.sessions
        |   step 2 when user.sessions.events.id in (2, 1)
        |   2
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
      r should equal(Array((2,2)))
    })
  }

  it should "do simple transaction funnel" in {
    // staticSweep = new B6F478B1D9A6240F28AB36A7742BC60FA
    val source =
      """
        |funnel test transaction {
        |   step 1 when start of user.sessions
        |   step 2 when user.sessions.events.id in (2, 1)
        |   2
        |} from schema Unity
        |
        |select as query_test
        |     count(t.paths.steps) as num,
        |     t.paths.steps.id as id,
        |     t.paths.steps.time as 'time'
        |from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("time")).asLong, row(names("num")).asLong)
      }.sortBy(_._2).sortBy(_._1)
      r should equal(Array((2,10,2), (2,20,2)))
    })
  }

  it should "do simple restarting transaction funnel" in {
    // staticSweep = new B6F478B1D9A6240F28AB36A7742BC60FA
    val source =
      """
        |funnel test transaction {
        |   step 1 when start of user.sessions
        |   step 2 when user.sessions.events.id in (2, 1, 3)
        |   2
        |} from schema Unity
        |
        |select as query_test
        |     count(t.paths.steps) as num,
        |     t.paths.steps.id as id,
        |     t.paths.steps.time as 'time'
        |from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("time")).asLong, row(names("num")).asLong)
      }.sortBy(_._2).sortBy(_._1)
      r should equal(Array((2, 10, 2), (2, 20, 2), (2, 21, 2)))
    })
  }

  it should "do linear multi-step  funnel" in {
    val source =
      """
        |funnel test transaction {
        |   step 1 when start of user.sessions timing on user.sessions.startTime
        |   step 2 when user.sessions.events.id in (1, 2)
        |   step 3 when user.sessions.events.id in (3, 4)
        |   step 4 when user.sessions.events.id in (5, 6)
        |
        |    1 : 2 : 3 : 4
        |} from schema Unity
        |
        |select as query_test
        |     count(t.paths.steps) as num,
        |     t.paths.steps.id as id,
        |     t.paths.steps.time as 'time'
        |from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("time")).asLong, row(names("num")).asLong)
      }.sortBy(_._2).sortBy(_._1)
      r should equal(Array((1, 9, 2), (1, 19, 2), (1, 29, 2), (2, 10, 2), (2, 20, 2), (3, 11, 2), (3, 21, 2), (4, 12, 2), (4, 22, 2)))
    })
  }

  it should "do linear funnel with global where" in {
    StaticSweep = null
    val source =
      """
        |funnel test transaction {
        |   step 1 when start of user.sessions
        |   step 2 when user.sessions.events.id in (1, 2)
        |   step 3 when user.sessions.events.id in (3, 4)
        |   step 4 when user.sessions.events.id in (5, 6)
        |
        |    2 : 3 : 4
        |} from schema Unity
        |
        |select as query_test
        |     count(t.paths.steps) as num,
        |     t.paths.steps.id as id,
        |     t.paths.steps.time as 'time'
        |from schema Unity, funnel test as t
        |where user.sessions.events.id % 2 != 1
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("time")).asLong, row(names("num")).asLong)
      }.sortBy(_._2).sortBy(_._1)
      r should equal(Array((2, 10, 2), (3, 11, 2), (4, 12, 2)))
    })
  }

  it should "do co-opted date recording in tail step" in {
    StaticSweep = null
    val source =
      """
        |funnel test transaction {
        |   step 1 when start of user.sessions
        |   step 2 when user.sessions.events.id in (1, 2)
        |   step 3 when user.sessions.events.id in (3, 4)
        |   step 4 when user.sessions.events.id is not null timing on user.sessions.events.id
        |
        |    2 : 3 : 4
        |} from schema Unity
        |
        |select as query_test
        |     count(t.paths.steps) as num,
        |     t.paths.steps.id as id,
        |     t.paths.steps.time as 'time'
        |from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("time")).asLong, row(names("num")).asLong)
      }.sortBy(_._2).sortBy(_._1)
      r should equal(Array((2, 10, 2), (2, 20, 2), (3, 11, 2), (3, 21, 2), (4, 5, 2), (4, 6, 2)))
    })
  }

  it should "do co-opted date recording in lead step" in {
    StaticSweep = null
    val source =
      """
        |funnel test transaction {
        |   step 1 when start of user.sessions
        |   step 2 when user.sessions.events.id is not null timing on user.sessions.events.id
        |   step 3 when user.sessions.events.id in (3, 4)
        |   step 4 when user.sessions.events.id in (5, 6)
        |    2 : 3 : 4
        |} from schema Unity
        |
        |select as query_test
        |     count(t.paths.steps) as num,
        |     t.paths.steps.id as id,
        |     t.paths.steps.time as 'time'
        |from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("time")).asLong, row(names("num")).asLong)
      }.sortBy(_._2).sortBy(_._1)
      r should equal(Array((2,1,2), (2,2,2), (2,8,1), (2,11,2), (2,13,2), (2,15,2), (3,11,2), (3,21,2), (4,12,2), (4,22,2)))
    })
  }

  it should "do wildcard step" in {
    StaticSweep = null
    val source =
      """
        |funnel test transaction {
        |   step 1 when start of user.sessions
        |   step 2 when user.sessions.events.id is not null
        |   step 3 when user.sessions.events.id in (5, 6)
        |    2* : 3
        |} from schema Unity
        |
        |select as query_test
        |     count(t.paths.steps) as num,
        |     t.paths.steps.id as id,
        |     t.paths.steps.time as 'time'
        |from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("time")).asLong, row(names("num")).asLong)
      }.sortBy(_._2).sortBy(_._1)
      r should equal(Array((2,10,2), (2,11,2), (2,13,1), (2,20,2), (2,21,2), (2,30,2), (2,31,2), (2,32,2), (3,12,2), (3,22,2)))
    })
  }

  it should "do bounded wildcard step" in {
    StaticSweep = null
    val source =
      """
        |funnel test transaction {
        |   step 2 when user.sessions.events.id in (2)
        |   step 4 when user.sessions.events.id in (4)
        |   step 6 when user.sessions.events.id in (6)
        |   step 8 when user.sessions.events.id in (8)
        |    (2|4|6){1,3} : 8
        |} from schema Unity
        |
        |select as query_test
        |     count(t.paths.steps) as num,
        |     t.paths.steps.id as id,
        |     t.paths.steps.time as 'time'
        |from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("time")).asLong, row(names("num")).asLong)
      }.sortBy(_._2).sortBy(_._1)
      r should equal(Array((2,10,2), (4,11,2), (6,12,2), (8,13,1)))
    })
  }

  it should "do is complete test" in {
    StaticSweep = null
    val source =
      """
        |funnel test transaction {
        |   step 2 when user.sessions.events.id in (2)
        |   step 4 when user.sessions.events.id in (4)
        |   step 6 when user.sessions.events.id in (6)
        |   step 8 when user.sessions.events.id in (8)
        |    (2|4|6){1,3} : 8
        |} from schema Unity
        |
        |select as query_test
        |     count(t.paths.steps) as num,
        |     t.paths.steps.id as id,
        |     t.paths.steps.iscomplete as 'complete'
        |from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("complete")).asBoolean, row(names("num")).asLong)
      }.sortBy(_._2).sortBy(_._1)
      r should equal(Array((2,false,1), (2,true,1), (4,false,1), (4,true,1), (6,false,1), (6,true,1), (8,true,1)))
    })
  }

  it should "do or step" in {
    StaticSweep = null
    val source =
      """
        |funnel test transaction {
        |   step 99 when start of user.sessions
        |   step 1 when user.sessions.events.id == 1
        |   step 2 when user.sessions.events.id == 2
        |   step 3 when user.sessions.events.id == 3
        |   step 4 when user.sessions.events.id == 4
        |   step 10 when user.sessions.events.id in (5, 6)
        |    ((1 : 3) | (2 : 4)) : 10
        |} from schema Unity
        |
        |select as query_test
        |     count(t.paths.steps) as num,
        |     t.paths.steps.id as id,
        |     t.paths.steps.time as 'time'
        |from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("time")).asLong, row(names("num")).asLong)
      }.sortBy(_._2).sortBy(_._1)
      r should equal(Array((1, 20, 2), (2, 10, 2), (3, 21, 2), (4, 11, 2), (10, 12, 2), (10, 22, 2)))
    })
  }

  it should "do funnel with cubed steps" in {
    StaticSweep = null
    val source =
      """
        |funnel test transaction {
        |   step 1 when start of user.sessions
        |   step 2 when user.sessions.events.id in (1, 2)
        |   step 3 when user.sessions.events.id in (3, 4)
        |   step 4 when user.sessions.events.id in (5, 6)
        |
        |    1 : 2 : 3 : 4
        |} from schema Unity
        |
        |select as query_test count(user) as number,
        |                     t.paths.steps.id as ids
        |   from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("number")).asLong, row(names("ids")).asLong)
      }.sortBy(_._2)
      r should equal(Array((2,1), (2,2), (2,3), (2,4)))
    })
  }

  it should "do dimension expression aggregate of funnel steps" in {
    StaticSweep = null
    val source =
      """
        |funnel test transaction {
        |   step 1 when start of user.sessions
        |   step 2 when user.sessions.events.id in (1, 2)
        |   step 3 when user.sessions.events.id in (3, 4)
        |   step 4 when user.sessions.events.id in (5, 6)
        |
        |    1 : 2 : 3 : 4
        |} from schema Unity
        |
        |select as query_test
        |      count(user) as 'users',
        |      (count(t.paths.steps) where t.paths.steps.id == 4) as 'count'
        |   from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("users")).asLong, row(names("count")).asLong)
      }.sortBy(_._2)
      r should equal(Array((2,2)))
    })
  }

  it should "do expression aggregate in local where of funnel steps" in {
    StaticSweep = null
    val source =
      """
        |funnel test transaction {
        |   step 1 when start of user.sessions
        |   step 2 when user.sessions.events.id in (1, 2)
        |   step 3 when user.sessions.events.id in (3, 4)
        |   step 4 when user.sessions.events.id in (5, 6)
        |
        |    1 : 2 : 3 : 4
        |} from schema Unity
        |
        |select as query_test
        |  count(user) as number,
        |  t.paths.steps.id as ids
        |where (count(t.paths.steps) where t.paths.steps.id == 4) > 1
        |from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("number")).asLong, row(names("ids")).asLong)
      }.sortBy(_._2)
      r should equal(Array((2,1), (2,2), (2,3), (2,4)))
    })
  }

  it should "do expression aggregate in global where of funnel steps" in {
    StaticSweep = null
    val source =
      """
        |funnel test transaction {
        |   step 1 when start of user.sessions
        |   step 2 when user.sessions.events.id in (1, 2)
        |   step 3 when user.sessions.events.id in (3, 4)
        |   step 4 when user.sessions.events.id in (5, 6)
        |
        |    1 : 2 : 3 : 4
        |} from schema Unity
        |
        |select as query_test
        |  count(user) as number,
        |  t.paths.steps.id as ids
        |from schema Unity, funnel test as t
        |where (count(t.paths.steps) where t.paths.steps.id == 4) > 1
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("number")).asLong, row(names("ids")).asLong)
      }.sortBy(_._2)
      r should equal(Array((2,1), (2,2), (2,3), (2,4)))
    })
  }

  it should "do funnel with cubed paths and steps" in {
    StaticSweep = null
    val source =
      """
        |funnel test transaction {
        |   step 1 when start of user.sessions
        |   step 2 when user.sessions.events.id in (1, 2)
        |   step 3 when user.sessions.events.id in (3, 4)
        |   step 4 when user.sessions.events.id in (5, 6)
        |
        |    1 : 2 : 3 : 4
        |} from schema Unity
        |
        |select as query_test count(user) as number, count(t.paths) as paths,
        |                     t.paths.steps.id as ids
        |   from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("number")).asLong, row(names("paths")).asLong, row(names("ids")).asLong)
      }.sortBy(_._3).sortBy(_._2)
      r should equal(Array((2,2,0)))
    })
  }

  it should "end of session step funnel" in {
    StaticSweep = null
    val source =
      """
        |funnel test transaction {
        |   step 2 when user.sessions.events.id in (1, 2)
        |   step 1 when end of user.sessions
        |
        |    2 : 1
        |} from schema Unity
        |
        |select as query_test count(user) as number, count(t.paths.steps) as 'paths', t.paths.steps.id as id
        |   from schema Unity, funnel test as t where t.paths.steps.iscomplete && t.paths.steps.ordinal == 1;
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("number")).asLong, row(names("paths")).asLong, row(names("id")).asLong)
      }
      r should equal(Array((2,4,1)))
    })
  }

  it should "do complex funnel" in {
    StaticSweep = null
    val source =
      """
        |funnel test conversion {
        |   step 1 when start of user.sessions timing on user.sessions.startTime
        |   step 2 when user.sessions.events.id in (1, 2)
        |   step 3 when user.sessions.events.id in (3, 4)
        |   step 4 when user.sessions.events.id in (5, 6)
        |
        |    1 : (2|3)? : 2+ : 3 : 4*
        |} from schema Unity
        |
        |select as query_test count(user) as number, count(t.paths), t.paths.steps.id as paths
        |   from schema Unity, funnel test as t where t.paths.ordinal == 0
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("number")).asLong, row(names("paths")).asLong, row(names("id")).asLong)
      }
    })
  }

}
