/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.cubes

import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.vitals.errors.VitalsException

final
class EqlDimensionOnlyCubeQuerySpec extends EqlAlloyTestRunner {

  it should "successfully generate a single dimension only query" in {
    val source =
      s"""
         | select user.sessions.events.parameters.key as id
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

      r should equal(Array("EK1", "EK2", "EK3", "EK4", "EK5", "EK6", "EK7") )
    })
  }

  it should "successfully generate a multiple dimension only query" in {
    val source =
      s"""
         | select user.sessions.events.parameters.key as ky, user.sessions.events.id as id
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
        row => (row(names("id")).asLong, row(names("ky")).asString)
      }.sortBy(_._1).sortBy(_._2).take(22)

      r should equal(Array((1,"EK1"), (2,"EK1"), (3,"EK1"), (4,"EK1"), (5,"EK1"), (6,"EK1"), (7,"EK1"), (8,"EK1"),
        (9,"EK1"), (10,"EK1"), (11,"EK1"), (1,"EK2"), (2,"EK2"), (3,"EK2"), (4,"EK2"), (5,"EK2"), (6,"EK2"), (7,"EK2"),
        (8,"EK2"), (9,"EK2"), (10,"EK2"), (11,"EK2")))
    })
  }

  it should "successfully generate a multiple dimension only with where query" in {
    val source =
      s"""
         | select user.sessions.events.parameters.key as ky, user.sessions.events.id as id
         | from schema Unity where user.sessions.events.id == 9
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
        row => (row(names("id")).asLong, row(names("ky")).asString)
      }.sortBy(_._2)

      r should equal(Array((9,"EK1"), (9,"EK2"), (9,"EK3"), (9,"EK4"), (9,"EK5"), (9,"EK6"), (9,"EK7")))
    })
  }

  ignore should "successfully query through a scalar references (Hydra Bug) " in {
    val source =
      s"""
         | select count(user) as 'count',
         | day(user.application.firstUse.sessionTime) as 'start',
         | day(user.application.lastUse.sessionTime) as 'end'
         | from schema Unity limit 20;
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
        row => (row(names("count")).asLong, row(names("start")).asLong, row(names("end")).asLong)
      }.sortBy(_._1).sortBy(_._2).sortBy(_._3)

      // TODO visit or traversal abandon need to get rid of 0 rows
      r should equal(Array((50,1483257600000L,1485849600000L)))
    })
  }

  it should "successfully generate a multiple dimension only joined with where query" in {
    val source =
      s"""
         | select user.sessions.events.parameters.key as eky,
         | user.sessions.events.id as id,
         | user.sessions.parameters.key as sky
         | from schema Unity where user.sessions.events.id == 9 && user.sessions.events.parameters.key == 'EK7'
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
        row => (row(names("id")).asLong, row(names("sky")).asString, row(names("eky")).asString)
      }.sortBy(_._1).sortBy(_._2).sortBy(_._3)

      // TODO visit or traversal abandon need to get rid of 0 rows
      r should equal(Array((0,"SK1",""), (0,"SK2",""), (0,"SK3",""), (0,"SK4",""), (0,"SK5",""), (0,"SK6",""), (0,"SK7",""),
        (9,"SK1","EK7"), (9,"SK2","EK7"), (9,"SK3","EK7"), (9,"SK4","EK7"), (9,"SK5","EK7"), (9,"SK6","EK7"),
        (9,"SK7","EK7")))
    })
  }

}
