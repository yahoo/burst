/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.cubes

import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.vitals.errors.VitalsException

/**
 * TEMP FILE TO FIND DOUBLE FREE BUG
 */
final
class EqlHydraBugQuerySpec extends EqlAlloyTestRunner {

  // fixed
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

      r should equal(Array((50,1483257600000L,1485849600000L)))
    })
  }

 ignore should "successfully query through a parameter keys with nulls" in {
    val source = s"""
                 |select count(user.sessions.events) as 'count', user.sessions.events.parameters.key as 'key'
                 |from schema unity
                 |where user.sessions.events.parameters.key is not null
                 |""".stripMargin


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
        row => (row(names("count")).asLong, row(names("key")).asString)
      }.sortBy(_._1).sortBy(_._2)

      // TODO visit or traversal abandon need to get rid of 0 rows
      r should equal(Array((50,1483257600000L,1485849600000L)))
    })
  }

}
