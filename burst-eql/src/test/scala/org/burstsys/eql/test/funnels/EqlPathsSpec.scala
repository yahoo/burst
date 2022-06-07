/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.funnels

import org.burstsys.alloy.alloy.store.AlloyView
import org.burstsys.alloy.alloy.views.AlloyJsonUseCaseViews.miniToAlloy
import org.burstsys.alloy.views.UnitMiniView
import org.burstsys.alloy.views.unity.UnityUseCaseViews.unitySchema
import org.burstsys.brio.press.BrioPressInstance
import org.burstsys.eql.test.funnels.EqlPathsSpec.testView
import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.brio.flurry.provider.unity._

object EqlPathsSpec {
  val testView: Array[AlloyView] = {
    // generate path view where every user has sessions containing some partition of the total permutations of a sequence events
    // of a given length where the domain of events is a given set
    def generatePathView(events:  Array[Int], userCount: Int, sessionPathCount: Int, pathLength: Int): Array[BrioPressInstance] = {
      assert(events.length >= pathLength)
      val pathIterator: Iterator[Array[Int]] = new Iterator[Array[Int]]() {
        private val dials: Array[Int] = (0 until pathLength).map(_ => 0).toArray

        def hasNext: Boolean = true //always will produce a next

        def next(): Array[Int] = {
          val result = dials.map(d => events(d))
          var carry = 1
          dials.indices.foreach{ d =>
            dials(d) += carry
            if (dials(d) >= pathLength) {
              dials(d) = 0
              carry = 1
            } else
              carry = 0
          }
          result
        }
      }

      (1 to userCount).map{id =>
        var sessionId = 0
        var time = 0
        UnityMockUser(
          id = s"User$id",
          sessions = (0 until sessionPathCount).map(_ => UnityMockSession(
            id = {sessionId += 1; sessionId},
            startTime = {time += 1; time},
            events = {
              val path = pathIterator.next()
              // System.err.println(s"u=$id s=$sessionId st=$time path=${path.mkString("-")}")
              path.map(eventId => UnityMockEvent(
                id = eventId,
                eventType = 1,
                startTime = {time += 1; time}
              ))
            }
          )).toArray
        )
      }.toArray
    }
    val mv = UnitMiniView(unitySchema, 99, 99, generatePathView(Array(1,2,3,4), 20, 40, 4))

    miniToAlloy(Array(mv))
  }

}
final
class EqlPathsSpec extends EqlAlloyTestRunner {

  override protected lazy val localViews: Array[AlloyView] = testView

  it should "do find a simple path" in {

  val source =
      """
        |funnel test transaction {
        |   step 0 when start of user.sessions
        |   step 1 when user.sessions.events.id==1
        |   step 2 when user.sessions.events.id==2
        |   step 3 when user.sessions.events.id==3
        |   step 4 when user.sessions.events.id==4
        |
        |    0: 1 : 2 : 3 : 4
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
      r should equal(Array((0,3), (1,3), (2,3), (3,3), (4,3)))
    })
  }

  it should "do a simple path with an existential test from the global where" in {
    val source =
      """
        |funnel test transaction {
        |   step 0 when start of user.sessions
        |   step 1 when user.sessions.events.id==1
        |   step 2 when user.sessions.events.id==2
        |   step 3 when user.sessions.events.id==3
        |   step 4 when user.sessions.events.id==4
        |
        |    0: 1 : 2 : 3 : 4
        |} from schema Unity
        |
        |select as query_test t.paths.steps.id as id, t.paths.steps.time as time, count(t.paths.steps) as num
        |   from schema Unity, funnel test as t where user.sessions.startTime > 100 && user.sessions.startTime < 160
        |limit 1000
        |""".stripMargin

    runTest(source, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("time")).asLong, row(names("num")).asLong)
      }.sortBy(_._1).sortBy(_._2)
      r should equal(Array((0,101,20), (1,102,20), (2,103,10), (3,104,3), (4,105,1), (0,106,20), (0,111,20), (0,116,20), (0,121,20), (1,122,20), (0,126,20), (0,131,20), (0,136,20), (0,141,20), (1,142,20), (2,143,10), (3,144,2), (4,145,1), (0,146,20), (0,151,20), (0,156,20)))
    })
  }

  it should "do find consistency in related paths" in {
    val source1 =
      """
        |funnel test transaction {
        |   step 0 when start of user.sessions
        |   step 1 when user.sessions.events.id==1
        |   step 2 when user.sessions.events.id==2
        |   step 3 when user.sessions.events.id==3
        |   step 4 when user.sessions.events.id==4
        |
        |    0: 1
        |} from schema Unity
        |
        |select as query_test t.paths.steps.id as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test as t
        |""".stripMargin

    val source2 =
      """
        |funnel test transaction {
        |   step 0 when start of user.sessions
        |   step 1 when user.sessions.events.id==1
        |   step 2 when user.sessions.events.id==2
        |   step 3 when user.sessions.events.id==3
        |   step 4 when user.sessions.events.id==4
        |
        |    0: 1 : 2
        |} from schema Unity
        |
        |select as query_test t.paths.steps.id as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test as t
        |""".stripMargin

    val source3 =
      """
        |funnel test transaction {
        |   step 0 when start of user.sessions
        |   step 1 when user.sessions.events.id==1
        |   step 2 when user.sessions.events.id==2
        |   step 3 when user.sessions.events.id==3
        |   step 4 when user.sessions.events.id==4
        |
        |    0: 1 : 2: 3
        |} from schema Unity
        |
        |select as query_test t.paths.steps.id as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test as t
        |""".stripMargin

    val source4 =
      """
        |funnel test transaction {
        |   step 0 when start of user.sessions
        |   step 1 when user.sessions.events.id==1
        |   step 2 when user.sessions.events.id==2
        |   step 3 when user.sessions.events.id==3
        |   step 4 when user.sessions.events.id==4
        |
        |    0: 1 : 2: 3: 4
        |} from schema Unity
        |
        |select as query_test t.paths.steps.id as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test as t
        |""".stripMargin

    runTest(source1, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((0,800), (1,200)))
    })
    runTest(source2, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((0,800), (1,200), (2,50)))
    })
    runTest(source3, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((0,800), (1,200), (2,50), (3,12)))
    })
    runTest(source4, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((0,800), (1,200), (2,50), (3,12), (4, 3)))
    })
  }

  it should "do find consistency in related paths with global test" in {
    val source1 =
      """
        |funnel test transaction {
        |   step 0 when start of user.sessions
        |   step 1 when user.sessions.events.id==1
        |   step 2 when user.sessions.events.id==2
        |   step 3 when user.sessions.events.id==3
        |   step 4 when user.sessions.events.id==4
        |
        |    0: 1
        |} from schema Unity
        |
        |select as query_test t.paths.steps.id as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test as t where user.sessions.startTime > 100 && user.sessions.startTime < 160
        |""".stripMargin

    val source2 =
      """
        |funnel test transaction {
        |   step 0 when start of user.sessions
        |   step 1 when user.sessions.events.id==1
        |   step 2 when user.sessions.events.id==2
        |   step 3 when user.sessions.events.id==3
        |   step 4 when user.sessions.events.id==4
        |
        |    0: 1 : 2
        |} from schema Unity
        |
        |select as query_test t.paths.steps.id as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test as t where user.sessions.startTime > 100 && user.sessions.startTime < 160
        |""".stripMargin

    val source3 =
      """
        |funnel test transaction {
        |   step 0 when start of user.sessions
        |   step 1 when user.sessions.events.id==1
        |   step 2 when user.sessions.events.id==2
        |   step 3 when user.sessions.events.id==3
        |   step 4 when user.sessions.events.id==4
        |
        |    0: 1 : 2: 3
        |} from schema Unity
        |
        |select as query_test t.paths.steps.id as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test as t where user.sessions.startTime > 100 && user.sessions.startTime < 160
        |""".stripMargin

    val source4 =
      """
        |funnel test transaction {
        |   step 0 when start of user.sessions
        |   step 1 when user.sessions.events.id==1
        |   step 2 when user.sessions.events.id==2
        |   step 3 when user.sessions.events.id==3
        |   step 4 when user.sessions.events.id==4
        |
        |    0: 1 : 2: 3: 4
        |} from schema Unity
        |
        |select as query_test t.paths.steps.id as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test as t where user.sessions.startTime > 100 && user.sessions.startTime < 160
        |""".stripMargin

    runTest(source1, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((0,240), (1,60)))
    })
    runTest(source2, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((0,240), (1,60), (2,20)))
    })
    runTest(source3, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((0,240), (1,60), (2,20), (3,5)))
    })
    runTest(source4, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((0,240), (1,60), (2,20), (3,5), (4, 2)))
    })
  }

  it should "do find consistency in user paths with global test" in {
    val source1 =
      """
        |funnel test transaction {
        |   step 0 when start of user.sessions
        |   step 1 when user.sessions.events.id==1
        |   step 2 when user.sessions.events.id==2
        |   step 3 when user.sessions.events.id==3
        |   step 4 when user.sessions.events.id==4
        |
        |    0: [1 2 3 4]
        |} from schema Unity
        |
        |select as query_test t.paths.steps.id as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test as t where user.sessions.startTime > 100 && user.sessions.startTime < 160
        |""".stripMargin

    val source2 =
      """
        |funnel test transaction {
        |   step 0 when start of user.sessions
        |   step 10 when user.sessions.events.id==1
        |   step 1 when user.sessions.events.id==1
        |   step 2 when user.sessions.events.id==2
        |   step 3 when user.sessions.events.id==3
        |   step 4 when user.sessions.events.id==4
        |
        |    0: 10 : [1 2 3 4]
        |} from schema Unity
        |
        |select as query_test t.paths.steps.id as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test as t where user.sessions.startTime > 100 && user.sessions.startTime < 160
        |""".stripMargin

    val source3 =
      """
        |funnel test transaction {
        |   step 0 when start of user.sessions
        |   step 10 when user.sessions.events.id==1
        |   step 20 when user.sessions.events.id==2
        |   step 1 when user.sessions.events.id==1
        |   step 2 when user.sessions.events.id==2
        |   step 3 when user.sessions.events.id==3
        |   step 4 when user.sessions.events.id==4
        |
        |    0: 10 : 20 : [1 2 3 4]
        |} from schema Unity
        |
        |select as query_test t.paths.steps.id as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test as t where user.sessions.startTime > 100 && user.sessions.startTime < 160
        |""".stripMargin

    val source4 =
      """
        |funnel test transaction {
        |   step 0 when start of user.sessions
        |   step 10 when user.sessions.events.id==1
        |   step 20 when user.sessions.events.id==2
        |   step 30 when user.sessions.events.id==3
        |   step 1 when user.sessions.events.id==1
        |   step 2 when user.sessions.events.id==2
        |   step 3 when user.sessions.events.id==3
        |   step 4 when user.sessions.events.id==4
        |
        |    0: 10 : 20 : 30 : [1 2 3 4]
        |} from schema Unity
        |
        |select as query_test t.paths.steps.id as id, count(t.paths.steps) as num
        |   from schema Unity, funnel test as t where user.sessions.startTime > 100 && user.sessions.startTime < 160
        |""".stripMargin

    runTest(source1, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((0,240), (1,60), (2,60), (3,60), (4,60)))
    })
    runTest(source2, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((0,240), (1,10), (2,20), (3,10), (4,20), (10,60)))
    })
    runTest(source3, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((0,240), (1,5), (2,6), (3,5), (4,4), (10,60), (20,20)))
    })
    runTest(source4, 99, 99, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("id")).asLong, row(names("num")).asLong)
      }.sortBy(_._1)
      r should equal(Array((0,240), (1,1), (2,1), (3,1), (4,2), (10,60), (20,20), (30,5)))
    })
  }
}
