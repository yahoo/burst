/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.control

import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.motif.common.ParseException
import org.burstsys.vitals.errors.VitalsException

/**
  * These are the EQL predicate tests
  *
  */
final
class EqlHarderGlobalWhereSpec extends EqlAlloyTestRunner {

  it should "successfully do an off-axis value vector test" in {

    val source =
      s"""
         | select count(user.sessions.events) as frequency,
         |        user.sessions.events.id as id
         | from schema Unity
         | where user.sessions.startTime < now && user.interests.value == 1
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array((227,1), (228,2), (228,3), (228,4), (227,5), (227,6), (227,7), (227,8), (227,9), (227,10), (227,11)))
    })
  }

  it should "successfully do an off-axis reference vector test" in {

    val source =
      s"""
         | select count(user.sessions.events) as frequency,
         |        user.sessions.events.id as id
         | from schema Unity
         | where user.sessions.startTime < now && user.application.channels.campaignId == 22
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array((363,1), (364,2), (364,3), (364,4), (364,5), (363,6), (364,7), (363,8), (364,9), (363,10), (364,11)))
    })
  }

  it should "fail to do an off-axis disjunction with lower calculations" in {

    val source =
      s"""
         | select count(user.sessions.events) as frequency,
         |        user.sessions.events.id as id
         | from schema Unity
         | where user.sessions.startTime < now || user.application.channels.campaignId == 22
       """.stripMargin

    intercept[Exception]{
      runTest(source, 200, 200, { result =>
        prepResults(result)
      })
    }
  }

  it should "fail to do subclause with a conjunctive where clause referencing above scope" in {
    val source =
      s"""
         | select count(user.sessions.events) as frequency,
         |        user.sessions.events.id as id
         | from schema Unity
         | where (count(user.sessions) scope user.sessions where user.startTime < now && user.application.channels.campaignId == 22) > 1
       """.stripMargin

    intercept[ParseException]{
      runTest(source, 200, 200, { result =>
        prepResults(result)
      })
    }
  }

  it should "fail to do subclause with a disjuncttive where clause referencing above scope" in {
    val source =
      s"""
         | select count(user.sessions.events) as frequency,
         |        user.sessions.events.id as id
         | from schema Unity
         | where (count(user.sessions) scope user.sessions where user.startTime < now || user.application.channels.campaignId == 22) > 1
       """.stripMargin

    intercept[ParseException]{
      runTest(source, 200, 200, { result =>
        prepResults(result)
      })
    }
  }

  ignore should "do an off-axis disjunction with no lower calculations (Hydra Feature control verbs)" in {

    val source =
      s"""
         | select count(user.sessions.events) as frequency,
         |        user.sessions.events.id as id
         | from schema Unity
         | where user.interests.value == 1 || user.application.channels.campaignId == 22
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array((1136, 1), (1137, 2), (1137, 3), (1137, 4), (1137, 5), (1136, 6), (1136, 7), (1136, 8), (1136, 9), (1136, 10), (1136, 11)))
    })
  }

  ignore should "do an off-axis disjunction in where aggregate (Hydra Feature: control verbs)" in {
    // This would work if we detected that there were no calculations
    val source =
      s"""
         | select count(user.sessions.events) as frequency,
         |        user.sessions.events.id as id
         | from schema Unity
         | where user.interests.value == 1 || user.application.channels.campaignId == 22
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array((1136, 1), (1137, 2), (1137, 3), (1137, 4), (1137, 5), (1136, 6), (1136, 7), (1136, 8), (1136, 9), (1136, 10), (1136, 11)))
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
