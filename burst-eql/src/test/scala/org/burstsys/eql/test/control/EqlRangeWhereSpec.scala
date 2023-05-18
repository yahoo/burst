/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.control

import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.runtime.StaticSweep
import org.burstsys.vitals.errors.VitalsException

/**
  *  Tbis is the EQL range predicate tests
  *
  */
final
class EqlRangeWhereSpec extends EqlAlloyTestRunner {

  it should "successfully do a range test" in {
    val source =
      s"""
         | select count(user.sessions.events) as frequency,
         |        user.sessions.events.id as id
         | from schema Unity
         | where user.sessions.events.id  between 4 and 6+1
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array((1137,4), (1137,5), (1136,6), (1136,7)))
    })
  }

  it should "successfully do an expression range test" in {
    val source =
      s"""
         | select count(user.sessions.events) as frequency,
         |        user.sessions.events.id as id
         | from schema Unity
         | where user.sessions.events.id + 1  between 5 and 6+2
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array((1137,4), (1137,5), (1136,6), (1136,7)))
    })
  }

  it should "successfully do an exclusion range test" in {
    val source =
      s"""
         | select count(user.sessions.events) as frequency,
         |        user.sessions.events.id as id
         | from schema Unity
         | where user.sessions.events.id  not between 4 and 7
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array((1136,1), (1137,2), (1137,3), (1136,8), (1136,9), (1136,10), (1136,11)))
    })
  }

  it should "successfully do an exclusion expression range test" in {
    val source =
      s"""
         | select count(user.sessions.events) as frequency,
         |        user.sessions.events.id as id
         | from schema Unity
         | where user.sessions.events.id + 1  not between 5 and 8
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array((1136,1), (1137,2), (1137,3), (1136,8), (1136,9), (1136,10), (1136,11)))
    })
  }

  it should "successfully do a static inline vector test" in {
    val source =
      s"""
         | select count(user.sessions.events) as frequency,
         |        user.sessions.events.id as id
         | from schema Unity
         | where user.sessions.events.id in (4, 5, 6 + 1)
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array((1137,4), (1137,5), (1136,7)))
    })
  }

  it should "successfully do a static inline expression vector test" in {
    val source =
      s"""
         | select count(user.sessions.events) as frequency,
         |        user.sessions.events.id as id
         | from schema Unity
         | where user.sessions.events.id + 1 in (4 + 1, 5 + 1, 6 + 2)
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array((1137,4), (1137,5), (1136,7)))
    })
  }

  it should "successfully do a static exclusion inline vector test" in {
    val source =
      s"""
         | select count(user.sessions.events) as frequency,
         |        user.sessions.events.id as id
         | from schema Unity
         | where user.sessions.events.id not in (4, 5, 6 + 1)
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array((1136,1), (1137,2), (1137,3), (1136,6), (1136,8), (1136,9), (1136,10), (1136,11)))
    })
  }

  it should "successfully do a static negated inclusion inline vector test" in {
    val source =
      s"""
         | select count(user.sessions.events) as frequency,
         |        user.sessions.events.id as id
         | from schema Unity
         | where not(user.sessions.events.id in (4, 5, 6 + 1))
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array((1136,1), (1137,2), (1137,3), (1136,6), (1136,8), (1136,9), (1136,10), (1136,11)))
    })
  }

  it should "successfully do a static exclusion inline expression vector test" in {
    val source =
      s"""
         | select count(user.sessions.events) as frequency,
         |        user.sessions.events.id as id
         | from schema Unity
         | where user.sessions.events.id%100 not in (4, 5, 6 + 1)
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array((1136,1), (1137,2), (1137,3), (1136,6), (1136,8), (1136,9), (1136,10), (1136,11)))
    })
  }

  def prepResults(result: FabricResultGroup): Array[(Long, Long)] = {
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
      row => (row(names("frequency")).asLong, row(names("id")).asLong)
    }.sortBy(_._2)
  }

}
