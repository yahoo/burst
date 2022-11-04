/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test

import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.vitals.errors.VitalsException

final
class EqlSingleCaseSpec extends EqlAlloyTestRunner {
 ignore should "single sum aggregate expression dimension with scope (Hydra Bug)" in {
    // staticSweep = new  BDCC5E606C77D406DB338D22A6E6B7AA5
    val source =
      """
        |select count(user) as users,
        |       frequency(user.sessions, 'time') as 'session'
        |from schema unity
        |       where day(NOW) - day(user.sessions.startTime) > 0
        |
        |""".stripMargin

    runTest(source, 200, 200, { result =>
      checkResultGroup(result) should be > 0L

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("users")).asLong, row(names("session")).asString, row(names("session_frequency")).asLong)
      }

      r.sortBy(_._3).sortBy(_._2) should equal(Array((50, "test", 25)))
    })
  }

  def checkResultGroup(result: FabricResultGroup): Long = {
    if (!result.resultStatus.isSuccess)
      throw VitalsException(s"execution failed: ${result.resultStatus}")
    if (result.groupMetrics.executionMetrics.overflowed > 0)
      throw VitalsException(s"execution overflowed")
    if (result.groupMetrics.executionMetrics.limited > 0)
      throw VitalsException(s"execution limited")

    // all the besides should return a result set
    if (result.groupMetrics.executionMetrics.rowCount > 0)
      result.resultSets.keys.size should be > 0

    result.groupMetrics.executionMetrics.rowCount
  }

}
