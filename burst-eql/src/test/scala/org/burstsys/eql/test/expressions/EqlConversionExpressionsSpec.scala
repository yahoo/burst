/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.expressions

import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.vitals.errors.VitalsException
import org.scalatest.Ignore

final
class EqlConversionExpressionsSpec extends EqlAlloyTestRunner {

  ignore should "successfully convert long to string" in {
    val source =
      s"""
         | select cast(1 as string) as id
         | from schema Unity
       """.stripMargin

    runTest(source, 200, 200, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")

      // all the besides should return a result set
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => row(names("id")).asString
      }

      r should equal(Array("1"))
    })
  }

  // HYDRA bug
  ignore should "successfully convert string to long in an aggregate" in {
    val source =
      s"""
         | select sum(cast(user.sessions.events.parameters['friendId'] as double)) as '_a1_'
         | from schema Unity
       """.stripMargin

    runTest(source, 200, 200, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")

      // all the besides should return a result set
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => row(names("id")).asLong
      }

      r should equal(Array(12))
    })
  }

  it should "successfully convert string to long" in {
    val source =
      s"""
         |  select cast("12" as long) as id
         | from schema Unity
       """.stripMargin

    runTest(source, 200, 200, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")

      // all the besides should return a result set
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => row(names("id")).asLong
      }

      r should equal(Array(12))
    })
  }

  it should "successfully convert string to byte" in {
    val source =
      s"""
         | select cast("12" as byte) as id
         | from schema Unity
       """.stripMargin

    runTest(source, 200, 200, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")

      // all the besides should return a result set
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => row(names("id")).asByte
      }

      r should equal(Array(12))
    })
  }

  it should "successfully convert string to double" in {
    val source =
      s"""
         | select cast("12.0" as double) as id
         | from schema Unity
       """.stripMargin

    runTest(source, 200, 200, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")

      // all the besides should return a result set
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => row(names("id")).asDouble
      }

      r should equal(Array(12.0))
    })
  }

  it should "successfully convert long to double" in {
    val source =
      s"""
         | select cast(user.sessions.events.id as double) as id
         | from schema Unity
       """.stripMargin

    runTest(source, 200, 200, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")

      // all the besides should return a result set
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => row(names("id")).asDouble
      }

      r should equal(Array(1.0, 5.0, 11.0, 7.0, 2.0, 10.0, 3.0, 4.0, 9.0, 6.0, 8.0))
    })
  }

  it should "successfully convert long to byte" in {
    val source =
      s"""
         | select cast(user.sessions.events.id as byte) as id
         | from schema Unity
       """.stripMargin

    runTest(source, 200, 200, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")

      // all the besides should return a result set
      result.resultSets.keys.size should be > 0

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => row(names("id")).asByte
      }

      r should equal(Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11))
    })
  }
}
