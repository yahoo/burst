/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.cubes

import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.vitals.errors.VitalsException

final
class EqlValueVectorQuerySpec extends EqlAlloyTestRunner {

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

  it should "successfully generate query with map keys and values" in {
    val source =
      s"""
         | select count(user.sessions.events) as events,
         | user.sessions.events.parameters.key as pkey,
         | user.sessions.events.parameters.value as pvalue
         | from schema Unity where user.sessions.events.parameters.value is not null
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
        row => (
          row(names("events")).asLong,
          if (row(names("pkey")).isNull) "(NULL)" else row(names("pkey")).asString,
          if (row(names("pvalue")).isNull) "(NULL)" else row(names("pvalue")).asString
        )
      }.sortBy(_._2)

      r should equal(Array((1,"one","1"), (1,"two","2")))
    })
  }

 it should "successfully generate aggregate query with map dimension lookup and test (Hydra Bug https://git.ouroath.com/burst/burst/issues/1674)" in {
    val source =
      s"""
         | select count(user) as events, user.sessions.events.parameters['one'] as pkey
         | from schema Unity where user.sessions.events.parameters['one'] is not null
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

      r should equal(Array((1, "1")))
    })
  }

  it should "successfully generate an aggregate query with dimension over scalar vector" in {
    val source =
      s"""
         | select count(user) as userCount, user.interests.value as interests from schema Unity
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

      // all the besides should return a result set
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val rs = result.resultSets(0).rowSet(0)

      val found =  result.resultSets(0).rowSet.map {
        row => (row.cells(0).asLong, row.cells(1).asLong)
      }.sortBy(_._1)
      found should equal(
        Array((1,10), (2,10), (3,10), (4,10), (5,10), (6,10), (7,10), (8,10), (9,10), (10,10))
      )
    })
  }

}
