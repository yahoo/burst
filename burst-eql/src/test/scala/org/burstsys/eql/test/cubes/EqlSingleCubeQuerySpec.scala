/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.cubes

import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.vitals.errors.VitalsException

final
class EqlSingleCubeQuerySpec extends EqlAlloyTestRunner {

  // TODO Hydra bug
  it should "successfully do a single cube query with dimensions and aggregates at different levels query" in {
    val source =
      s"""
         |select count(user) as userCount, count(user.sessions.events) as eventCount,
         |  day(1) as 'day', 1 as 'dashboard'
         |from schema unity
         """.stripMargin

    runTest(source, 100, 200, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")
      if (result.groupMetrics.executionMetrics.rowCount <= 0)
        throw VitalsException(s"execution row count mismatch: expected rows got ${result.groupMetrics.executionMetrics.rowCount}")

      // all the besides should return a result set
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("userCount")).asLong, row(names("eventCount")).asLong, row(names("day")).asLong, row(names("dashboard")).asByte)
      }.sortBy(_._1)

      r should equal(Array((50,12500,-57600000,1)))
    })
  }

  it should "successfully do a top aggregate query" in {
    val source =
      s"""
         |select top[20](user.application) as frequency, user.application.firstUse.osVersionId as osIds where (
         |  user.sessions.startTime >= day(NOW - weeks(10000)) && user.sessions.sessionType != 1
         |) && user.application.firstUse.osVersionId is not null
         |limit 20
         |from schema unity
         """.stripMargin

    runTest(source, 100, 200, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")
      if (result.groupMetrics.executionMetrics.rowCount <= 0)
        throw VitalsException(s"execution row count mismatch: expected rows got ${result.groupMetrics.executionMetrics.rowCount}")

      // all the besides should return a result set
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val rs = result.resultSets(0).rowSet(0)
      val r = rs(names("osIds")).asLong

      r should not be 0
    })
  }

  it should "successfully generate a couple of aggregates query" in {
    val source =
      s"""
         | select count(user) as userCount, count(user.sessions) as sessionCount,
         |    count(user.sessions.events) as eventCount, min(user.sessions.startTime) as minSessionTime,
         |    max(user.sessions.startTime) as maxSessionTime from schema Unity
       """.stripMargin

    runTest(source, 100, 200, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")
      if (result.groupMetrics.executionMetrics.rowCount <= 0)
        throw VitalsException(s"execution row count mismatch: expected rows got ${result.groupMetrics.executionMetrics.rowCount}")

      // all the besides should return a result set
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val rs = result.resultSets(0).rowSet(0)
      val r = rs(names("minSessionTime")).asLong

      r should not be 0
    })
  }

  it should "successfully generate a second couple of aggregates query" in {
    val source =
      s"""
         | select count(user) as userCount, count(user.sessions) as sessionCount, count(user.sessions.events) as eventCount
         | beside select min(user.sessions.startTime) as minSessionTime, max(user.sessions.startTime) as maxSessionTime
         | from schema Unity
       """.stripMargin

    runTest(source, 100, 200, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")
      if (result.groupMetrics.executionMetrics.rowCount <= 0)
        throw VitalsException(s"execution row count mismatch: expected rows got ${result.groupMetrics.executionMetrics.rowCount}")

      // all the besides should return a result set
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(1).columnNames.zipWithIndex.toMap
      val rs = result.resultSets(1).rowSet(0)
      val r = rs(names("minSessionTime")).asLong

      r should not be 0
    })
  }

  it should "successfully generate another couple of aggregates query" in {
    val source =
      s"""
         | select min(user.sessions.startTime) as minSessionTime,
         |    max(user.sessions.startTime) as maxSessionTime from schema Unity
       """.stripMargin

    runTest(source, 100, 200, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")
      if (result.groupMetrics.executionMetrics.rowCount <= 0)
        throw VitalsException(s"execution row count mismatch: expected rows got ${result.groupMetrics.executionMetrics.rowCount}")

      // all the besides should return a result set
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val rs = result.resultSets(0).rowSet(0)
      val r = rs(names("minSessionTime")).asLong

      r should not be 0
    })
  }

  it should "successfully generate the most basic aggregate query" in {
    val source =
      s"""
         | select count(user) as users
         | from schema Unity
       """.stripMargin

    runTest(source, 100, 200, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")
      if (result.groupMetrics.executionMetrics.rowCount <= 0)
        throw VitalsException(s"execution row count mismatch: expected rows got ${result.groupMetrics.executionMetrics.rowCount}")

      // all the besides should return a result set
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val rs = result.resultSets(0).rowSet(0)
      val r = rs(names("users")).asLong

      r should equal(50)
    })
  }

  it should "successfully generate a unique query" in {
    val source =
      s"""
         | select unique(user.sessions.events) as users
         | from schema Unity
       """.stripMargin

    runTest(source, 100, 200, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")
      if (result.groupMetrics.executionMetrics.rowCount <= 0)
        throw VitalsException(s"execution row count mismatch: expected rows got ${result.groupMetrics.executionMetrics.rowCount}")

      // all the besides should return a result set
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val rs = result.resultSets(0).rowSet(0)
      val r = rs(names("users")).asLong

      r should equal(50)
    })
  }

it should "successfully generate a top aggregate query" in {
    val source =
      s"""
         | select top[8](user.sessions.events) as evnts, user.sessions.events.parameters.key as kys
         | from schema Unity
       """.stripMargin

    runTest(source, 100, 200, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")
      if (result.groupMetrics.executionMetrics.rowCount <= 0)
        throw VitalsException(s"execution row count mismatch: expected rows got ${result.groupMetrics.executionMetrics.rowCount}")

      // all the besides should return a result set
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("evnts")).asLong, row(names("kys")).asString.take(2))
      }.sortBy{r => r._1}.reverse

      r should equal(Array((8929, "EK"), (8929, "EK"), (8929, "EK"), (8929, "EK"),
        (8928, "EK"), (8928, "EK"), (8928, "EK")))
    })
  }

  it should "successfully generate simple aggregate query" in {
    val source =
      s"""
         | select count(user) as users, count(user.sessions) as sessions, count(user.sessions.events) as events
         | from schema Unity
       """.stripMargin

    runTest(source, 100, 100, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")
      if (result.groupMetrics.executionMetrics.rowCount <= 0)
        throw VitalsException(s"execution row count mismatch: expected rows got ${result.groupMetrics.executionMetrics.rowCount}")

      // all the besides should return a result set
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val rs = result.resultSets(0).rowSet(0)
      val r = Array(Array(rs(names("users")).asLong, rs(names("sessions")).asLong, rs(names("events")).asLong))

      r should equal(Array(Array(4, 10, 0)))
    })

    // over different dataset
    runTest(source, 100, 200, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")
      if (result.groupMetrics.executionMetrics.rowCount <= 0)
        throw VitalsException(s"execution row count mismatch: expected rows got ${result.groupMetrics.executionMetrics.rowCount}")

      // all the besides should return a result set
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val rs = result.resultSets(0).rowSet(0)
      val r = Array(Array(rs(names("users")).asLong, rs(names("sessions")).asLong, rs(names("events")).asLong))

      r should equal(Array(Array(50, 1250, 12500)))
    })
  }

  it  should "successfully generate simple unique aggregate with dimension query" in {
    val source =
      s"""
         | select count(user.sessions.events) as events, dayofweek(user.sessions.events.startTime) as d
         | from schema Unity
       """.stripMargin

    runTest(source, 200, 300, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")
      if (result.groupMetrics.executionMetrics.rowCount <= 0)
        throw VitalsException(s"execution row count mismatch: expected rows got ${result.groupMetrics.executionMetrics.rowCount}")

      // all the besides should return a result sert
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => Array(row(names("events")).asLong, row(names("d")).asLong)
      }.sortBy(_.head)

      r should equal(Array(Array(4, 1), Array(4, 2), Array(4, 3), Array(4, 4)))
    })
  }

  it  should "successfully generate simple scalar value traverse with dimension query" in {
    val source =
      s"""
         | select count(user.application) as projects,
         |               user.application.firstUse.languageId as languageId
         | from schema Unity
       """.stripMargin

    runTest(source, 200, 300, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")
      if (result.groupMetrics.executionMetrics.rowCount <= 0)
        throw VitalsException(s"execution row count mismatch: expected rows got ${result.groupMetrics.executionMetrics.rowCount}")

      // all the besides should return a result sert
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => Array(row(names("projects")).asLong, row(names("languageId")).asLong)
      }.sortBy(_.head)

      r should equal(Array(Array(4, 0)))
    })
  }

  it  should "successfully generate simple aggregate with dimension query" in {
    val source =
      s"""
         | select count(user) as users, user.application.firstUse.osVersionId as osId
         | from schema Unity
       """.stripMargin

    runTest(source, 100, 100, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")
      if (result.groupMetrics.executionMetrics.rowCount <= 0)
        throw VitalsException(s"execution row count mismatch: expected rows got ${result.groupMetrics.executionMetrics.rowCount}")

      // all the besides should return a result sert
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => Array(row(names("users")).asLong, row(names("osId")).asLong)
      }.sortBy(_.head)

      r should equal(Array(Array(4, 0)))
    })
  }

  it should "successfully generate simple query with single clause where" in {
    val source =
      s"""
         | select count(user) as users, count(user.sessions) as sessions, count(user.sessions.events) as events
         | from schema Unity
         | where user.sessions.startTime > now - days(2000)
       """.stripMargin

    runTest(source, 200, 200, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")
      if (result.groupMetrics.executionMetrics.rowCount <= 0)
        throw VitalsException(s"execution row count mismatch: expected rows got ${result.groupMetrics.executionMetrics.rowCount}")

      // all the besides should return a result sert
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => Array(row(names("users")).asLong, row(names("sessions")).asLong, row(names("events")).asLong)
      }.sortBy(_.head)

      r should equal(Array(Array(50, 1250, 12500)))
    })
  }

  it should "successfully generate simple query with multi-and clause where" in {
    val source =
      s"""
         | select count(user) as 'users', count(user.sessions) as 'sessions', count(user.sessions.events) as 'events'
         | from schema Unity
         | where user.sessions.startTime > now - days(2000) and user.sessions.duration > 0
       """.stripMargin

    runTest(source, 200, 200, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")
      if (result.groupMetrics.executionMetrics.rowCount <= 0)
        throw VitalsException(s"execution row count mismatch: expected rows got ${result.groupMetrics.executionMetrics.rowCount}")

      // all the besides should return a result sert
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => Array(row(names("users")).asLong, row(names("sessions")).asLong, row(names("events")).asLong)
      }.sortBy(_.head)

      r should equal(Array(Array(50, 1250, 12500)))
    })
  }
}
