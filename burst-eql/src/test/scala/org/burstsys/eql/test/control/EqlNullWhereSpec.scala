/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.control

import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.vitals.errors.VitalsException

/**
  *  Tbis is the EQL range predicate tests
  *
  */
final
class EqlNullWhereSpec extends EqlAlloyTestRunner {

  it should "successfully do a not null test" in {
    val source =
      s"""
         | select count(user) as frequency
         | from schema Unity
         | where user.id is not null
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array(50))
    })
  }

  it should "successfully do a null test on a non null value" in {
    val source =
      s"""
         | select count(user) as frequency
         | from schema Unity
         | where user.id is null
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array())
    })
  }

  it should "successfully do a null test on a null value" in {
    val source =
      s"""
         | select count(user) as frequency
         | from schema Unity
         | where user.application.firstUse.timeZone is null
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array(50))
    })
  }

  it should "successfully do a not null test on a reference that is null" in {
    val source =
      s"""
         | select count(user) as frequency
         | from schema Unity
         | where user.application.mostUse is not null
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array())
    })
  }

  it should "successfully do a not null test on a reference that is not null" in {
    val source =
      s"""
         | select count(user) as frequency
         | from schema Unity
         | where user.application.firstUse is not null
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array(50))
    })
  }

  def prepResults(result: FabricResultGroup): Array[Long] = {
    if (!result.resultStatus.isSuccess)
      throw VitalsException(s"execution failed: ${result.resultStatus}")
    if (result.groupMetrics.executionMetrics.overflowed > 0)
      throw VitalsException(s"execution overflowed")
    if (result.groupMetrics.executionMetrics.limited > 0)
      throw VitalsException(s"execution limited")

    // all the besides should return a result set
    result.resultSets.keys.size should be > 0

    val names = result.resultSets(0).columnNames.zipWithIndex.toMap
    result.resultSets(0).rowSet.map {
      row => row(names("frequency")).asLong
    }
  }
}
