/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.cubes

import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.vitals.errors.VitalsException

final
class EqlMultiCubeQuerySpec extends EqlAlloyTestRunner {
  it should "double aggregate with dimensions" in {
    val source =
      s"""
         | select count(user) as users, count(user.sessions.events) as events, user.sessions.events.id as ids
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

      // all the besides should return a result set
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("users")).asLong, row(names("events")).asLong, row(names("ids")).asLong)
      }.sortBy(_._2)

      r should equal(Array((3,0,0), (1,4,1), (1,12,2)))
    })
  }


  it should "successfully generate aggregate query with map dimensions" in {
    val source =
      s"""
         | select count(user.sessions.events) as events, user.sessions.events.parameters.key as pkey
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

      // all the besides should return a result set
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("events")).asLong, if (row(names("pkey")).isNull) "(NULL)" else row(names("pkey")).asString)
      }.sortBy(_._2)

      r should equal(Array((15, "(NULL)"), (1, "one"), (1, "two")))
    })
   }

  it should "successfully generate aggregate query with map dimensions over a different dataset" in {
    val source =
      s"""
         | select count(user.sessions.events) as events, user.sessions.events.parameters.key as pkey
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
        row => (row(names("events")).asLong, if (row(names("pkey")).isNull) "(NULL)" else row(names("pkey")).asString)
      }.sortBy(_._2)

      r should equal(
        Array((8928,"EK1"), (8929,"EK2"), (8929,"EK3"), (8929,"EK4"), (8929,"EK5"), (8928,"EK6"), (8928,"EK7"))
      )
    })
  }

  it  should "successfully generate simple unique aggregate with dimension query" in {
    val source =
      s"""
         | select count(user.sessions.events) as events, dayofweek(datetime("2012-12-01T10:34:01Z") + days(user.sessions.events.startTime%7)) as d
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

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("d")).asLong,row(names("events")).asLong )
      }.sortBy(_._1)

      r should equal(
        Array((1,1785), (2,1786), (3,1786), (4,1786), (5,1786), (6,1786), (7,1785))
      )
    })
  }

  it  should "successfully generate simple scalar value traverse with dimension query" in {
    val source =
      s"""
         | select count(user.application) as projects,
         |               user.application.firstUse.languageId as languageId
         | from schema Unity
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
        row => (row(names("languageId")).asLong,row(names("projects")).asLong )
      }.sortBy(_._1)

      r should equal( Array((111222, 10), (333444, 10), (555666, 10), (777888, 10), (888999, 10)))
    })
  }

  it  should "successfully generate simple aggregate with dimension query" in {
    val source =
      s"""
         | select count(user) as users, user.application.firstUse.osVersionId as osId
         | from schema Unity
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
        row => (row(names("osId")).asLong,row(names("users")).asLong )
      }.sortBy(_._1)

      r should equal( Array((454545, 25), (898989, 25)))
    })
  }
}
