/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.besides

import org.burstsys.brio.types.BrioTypes
import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.vitals.errors.VitalsException

final
class EqlBesideSpec extends EqlAlloyTestRunner {

  it should "successfully do a simple parallel metadata query" in {
    val source =
      s"""
         | select count(user.sessions.events) as eventFrequency,
         |               user.sessions.events.id as eventId
         | beside select count(user.sessions) as sessions,
         |               user.sessions.originMethodTypeId as originMethodType
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

      // all the besides should return a result sert
      result.resultSets.keys.size should be > 0

      {
        val names = result.resultSets(0).columnNames.zipWithIndex.toMap
        val r = result.resultSets(0).rowSet.map {
          row => (row(names("eventFrequency")).asLong, row(names("eventId")).asLong)
        }.sortBy(_._2)
        r should equal(Array((1136,1), (1137,2), (1137,3), (1137,4), (1137,5), (1136,6), (1136,7), (1136,8), (1136,9), (1136,10), (1136,11)))
      }
      {
        val names = result.resultSets(1).columnNames.zipWithIndex.toMap
        val r = result.resultSets(1).rowSet.map {
          row => (row(names("sessions")).asLong, row(names("originMethodType")).asLong)
        }.sortBy(_._2)
        r should equal(Array((156,12), (157,13), (157,14), (156,15), (156,16), (156,17), (156,18), (156,19)))
      }
    })
  }

  it should "apply row limits to parallel metadata query" in {
    val source =
      s"""
         | select count(user.sessions.events) as eventFrequency,
         |               user.sessions.events.id as eventId limit 2
         | beside select count(user.sessions) as sessions,
         |               user.sessions.originMethodTypeId as originMethodType
         | from schema Unity limit 1
       """.stripMargin

    runTest(source, 100, 200, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited != 2)
        throw VitalsException(s"not all queries were limited")
      if (result.groupMetrics.executionMetrics.rowCount <= 0)
        throw VitalsException(s"execution row count mismatch: expected rows got ${result.groupMetrics.executionMetrics.rowCount}")

      // all the besides should return a result sert
      result.resultSets.keys.size should be > 0

      result.resultSets(0).rowSet.length should equal(2)
      result.resultSets(1).rowSet.length should equal(1)
    })
  }

  it should "successfully generate a less simple parallel metadata query" in {
    val source =
      s"""
         | select count(user.sessions.events) as one,
         |               user.sessions.events.id as two
         | beside select count(user.sessions) as one,
         |               user.sessions.mappedOriginId as two
         | beside select count(user.sessions) as one,
         |               user.sessions.originMethodTypeId as two
         | beside select count(user.sessions) as one,
         |               user.sessions.originSourceTypeId as two
         | beside select count(user.sessions) as one,
         |               user.sessions.osVersionId as two
         | beside select count(user.sessions) as one,
         |               user.sessions.providedOrigin as two
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

      // all the besides should return a result sert
      result.resultSets.keys.size should be > 0
      val expected = Array(
        Array((1, 1136), (2, 1137), (3, 1137), (4, 1137), (5, 1137), (6, 1136),
          (7, 1136), (8, 1136), (9, 1136), (10, 1136), (11, 1136)),
        Array((9876,416), (54321,417), (54329,417)),
        Array((12,156), (13,157), (14,157), (15,156), (16,156), (17,156), (18,156), (19,156)),
        Array((983,250), (984,250), (985,250), (986,250), (987,250)),
        Array((232323,325), (454545,300), (676767,325), (898989,300)),
        Array((-1202440757,250), (-1202440756,250), (-1202440755,250), (-1202440754,250), (-1202440753,250))
      )
      for (i <- 0 until result.resultSets.size)
      {
        val names = result.resultSets(i).columnNames.zipWithIndex.toMap
        val r = result.resultSets(i).rowSet.map {
          row =>
            row.cells(names("two")).bType match {
              case BrioTypes.BrioLongKey => (row(names("two")).asLong, row(names("one")).asLong)
              case BrioTypes.BrioStringKey => (row(names("two")).asString.hashCode.longValue, row(names("one")).asLong)
              case _ =>  throw VitalsException("not implemented")
            }
        }.sortBy(_._1)
        r should equal(expected(i))
      }
    })
  }

  it should "successfully do a parameterized parallel query" in {
    val source =
      """
        | select(p: long) count(user.sessions.events) as eventFrequency,
        |               user.sessions.events.id as eventId
        | beside select count(user.sessions) as sessions,
        |               user.sessions.originMethodTypeId as originMethodType
        |        where user.sessions.startTime > $p
        | from schema Unity
        |""".stripMargin

    runTest(source, 100, 200, { result =>
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

      {
        val names = result.resultSets(0).columnNames.zipWithIndex.toMap
        val r = result.resultSets(0).rowSet.map {
          row => (row(names("eventFrequency")).asLong, row(names("eventId")).asLong)
        }.sortBy(_._2)
        r should equal(Array((1136,1), (1137,2), (1137,3), (1137,4), (1137,5), (1136,6), (1136,7), (1136,8), (1136,9), (1136,10), (1136,11)))
      }
      {
        val names = result.resultSets(1).columnNames.zipWithIndex.toMap
        val r = result.resultSets(1).rowSet.map {
          row => (row(names("sessions")).asLong, row(names("originMethodType")).asLong)
        }.sortBy(_._2)
        r should equal(Array((156,12), (157,13), (157,14), (156,15), (156,16), (156,17), (156,18), (156,19)))
      }
    }, s"""{"p": 1}""")
  }

  it should "successfully do a another parameterized parallel query" in {
    val source =
      """
        | select(p: long) count(user.sessions.events) as eventFrequency,
        |               user.sessions.events.id as eventId
        | beside select count(user.sessions) as sessions,
        |               user.sessions.originMethodTypeId as originMethodType
        | from schema Unity
        | where user.sessions.startTime > $p
        |""".stripMargin

    runTest(source, 100, 200, { result =>
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

      {
        val names = result.resultSets(0).columnNames.zipWithIndex.toMap
        val r = result.resultSets(0).rowSet.map {
          row => (row(names("eventFrequency")).asLong, row(names("eventId")).asLong)
        }.sortBy(_._2)
        r should equal(Array((1136,1), (1137,2), (1137,3), (1137,4), (1137,5), (1136,6), (1136,7), (1136,8), (1136,9), (1136,10), (1136,11)))
      }
      {
        val names = result.resultSets(1).columnNames.zipWithIndex.toMap
        val r = result.resultSets(1).rowSet.map {
          row => (row(names("sessions")).asLong, row(names("originMethodType")).asLong)
        }.sortBy(_._2)
        r should equal(Array((156,12), (157,13), (157,14), (156,15), (156,16), (156,17), (156,18), (156,19)))
      }
    }, s"""{"p": 1}""")
  }

  it should "successfully do yet another parameterized parallel query" in {
    val source =
      """
        | select(p: long, q: long) count(user.sessions.events) as eventFrequency,
        |               user.sessions.events.id as eventId
        | beside select count(user.sessions) as sessions,
        |               user.sessions.originMethodTypeId as originMethodType
        |        where user.sessions.startTime > $q
        | from schema Unity
        | where user.sessions.startTime > $p
        |""".stripMargin

    runTest(source, 100, 200, { result =>
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

      {
        val names = result.resultSets(0).columnNames.zipWithIndex.toMap
        val r = result.resultSets(0).rowSet.map {
          row => (row(names("eventFrequency")).asLong, row(names("eventId")).asLong)
        }.sortBy(_._2)
        r should equal(Array((1136,1), (1137,2), (1137,3), (1137,4), (1137,5), (1136,6), (1136,7), (1136,8), (1136,9), (1136,10), (1136,11)))
      }
      {
        val names = result.resultSets(1).columnNames.zipWithIndex.toMap
        val r = result.resultSets(1).rowSet.map {
          row => (row(names("sessions")).asLong, row(names("originMethodType")).asLong)
        }.sortBy(_._2)
        r should equal(Array((156,12), (157,13), (157,14), (156,15), (156,16), (156,17), (156,18), (156,19)))
      }
    }, s"""{"p": 1, "f": 2}""")
  }

  it should "successfully generate a parallel metadata query" in {
    val source =
      s"""
         | select count(user.sessions) as one,
         |      user.sessions.appVersion.id as two
         | beside select count(user) as one,
         |               user.deviceModelId as two
         | beside select count(user.sessions.events) as one,
         |               user.sessions.events.id as two
         | beside select count(user.sessions.events) as one,
         |               user.sessions.events.id as two
         | beside select count(user.application) as one,
         |               user.application.firstUse.languageId as two
         | beside select count(user.sessions) as one,
         |               user.sessions.mappedOriginId as two
         | beside select count(user.sessions) as one,
         |               user.sessions.originMethodTypeId as two
         | beside select count(user.sessions) as one,
         |               user.sessions.originSourceTypeId as two
         | beside select count(user.sessions) as one,
         |               user.sessions.osVersionId as two
         | beside select count(user.sessions) as one,
         |               user.sessions.providedOrigin as two
         | from schema Unity
       """.stripMargin

    // goes in the event parameerKey query
    //|               user.sessions.events.parameters.key as eventParameterKey
    runTest(source, 100, 200, { result =>
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
      val expected: Array[Any] = Array(
        Array((12121212, 200), (13131313, 217), (101010101, 216), (1414141414, 200), (1515151515, 217), (1616161616, 200)),
        Array((555666,16), (666777,17), (888999,17)),
        Array((1,1136), (2,1137), (3,1137), (4,1137), (5,1137), (6,1136), (7,1136), (8,1136), (9,1136), (10,1136), (11,1136)),
        Array((1,1136), (2,1137), (3,1137), (4,1137), (5,1137), (6,1136), (7,1136), (8,1136), (9,1136), (10,1136), (11,1136)),
        Array((111222,10), (333444,10), (555666,10), (777888,10), (888999,10)),
        Array((9876,416), (54321,417), (54329,417)),
        Array((12,156), (13,157), (14,157), (15,156), (16,156), (17,156), (18,156), (19,156)),
        Array((983,250), (984,250), (985,250), (986,250), (987,250)),
        Array((232323,325), (454545,300), (676767,325), (898989,300)),
        Array((-1202440757,250), (-1202440756,250), (-1202440755,250), (-1202440754,250), (-1202440753,250))
      )
      for (i <- 0 until result.resultSets.size)
      {
        val names = result.resultSets(i).columnNames.zipWithIndex.toMap
        val r = result.resultSets(i).rowSet.map {
          row =>
            row.cells(names("two")).bType match {
              case BrioTypes.BrioLongKey => (row(names("two")).asLong, row(names("one")).asLong)
              case BrioTypes.BrioStringKey => (row(names("two")).asString.hashCode.longValue, row(names("one")).asLong)
              case _ =>  throw VitalsException("not implemented")
            }
        }.sortBy(_._1)
        r should equal(expected(i))
      }
    })
  }
}
