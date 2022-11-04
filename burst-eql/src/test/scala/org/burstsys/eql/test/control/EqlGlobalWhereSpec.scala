/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.control

import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.runtime.StaticSweep
import org.burstsys.vitals.errors.VitalsException

/**
  * These are the EQL predicate tests
  *
  */
final
class EqlGlobalWhereSpec extends EqlAlloyTestRunner {
  it should "successfully do predicate that fails" in {
    val source =
      s"""
         | select count(user.sessions.events) as frequency,
         |        user.sessions.events.id as id
         | from schema Unity
         | where user.sessions.startTime > now
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array())
    })
  }

  it should "successfully do an always true test with now" in {
    val source =
      s"""
         | select count(user.sessions.events) as frequency,
         |        user.sessions.events.id as id
         | from schema Unity
         | where user.sessions.startTime < now
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array((1136, 1), (1137, 2), (1137, 3), (1137, 4), (1137, 5), (1136, 6), (1136, 7), (1136, 8), (1136, 9), (1136, 10), (1136, 11)))
    })
  }

  it should "successfully do predicate on a dimension" in {
    val source =
      s"""
         | select count(user) as frequency,
         |        user.deviceModelId as id
         | from schema Unity
         | where user.deviceModelId == 555666
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((16, 555666)))
    })
  }

  it should "successfully do predicate on a parent" in {
    val source =
      s"""
         | select count(user) as frequency,
         |        user.sessions.events.id as id
         | from schema Unity
         | where user.deviceModelId == 555666
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((16, 1), (16, 2), (16, 3), (16, 4), (16, 5), (16, 6), (16, 7), (16, 8), (16, 9), (16, 10), (16, 11)))
    })
  }

  it should "successfully do predicate on same level as agg and dimension" in {
    val source =
      s"""
         | select count(user) as frequency,
         |        user.application.firstUse.languageId as id
         | from schema Unity
         | where user.deviceModelId == 555666
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((3, 111222), (3, 333444), (3, 555666), (4, 777888), (3, 888999)))
    })
  }

  it should "successfully do predicate off-axis test" in {
    val source =
      s"""
         | select count(user) as frequency,
         |        user.application.firstUse.languageId as id
         | from schema Unity
         | where user.sessions.events.id < 1
     """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array())
    })
  }


  it should "successfully do predicate with two blocks" in {
    val source =
      s"""
         | select count(user) as frequency,
         |        user.application.firstUse.languageId as id
         | from schema Unity
         | where user.deviceModelId == 555666 && user.application.firstUse.languageId == 555666
     """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array((3, 555666)))
    })
  }

  it should "successfully do predicate comparing two fields" in {
    val source =
      s"""
         | select count(user) as frequency,
         |        user.application.firstUse.languageId as id
         | from schema Unity
         | where user.deviceModelId == user.application.firstUse.languageId
     """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array((3, 555666), (3, 888999)))
    })
  }

  it should "successfully do predicate with or clause" in {
    val source =
      s"""
         | select count(user) as frequency,
         |        user.sessions.events.id as id
         | from schema Unity
         | where user.deviceModelId == 555666 || user.sessions.events.id == 2
     """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array((16, 1), (50, 2), (16, 3), (16, 4), (16, 5), (16, 6), (16, 7), (16, 8), (16, 9), (16, 10), (16, 11)))
    })
  }

  it should "successfully do predicate with multiple or clause" in {
    val source =
      s"""
         | select count(user) as frequency,
         |        user.sessions.events.id as id
         | from schema Unity
         | where user.sessions.events.id == 2 || user.sessions.events.id == 3 ||user.sessions.events.id == 6
     """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array((50, 2), (50, 3), (50, 6)))
    })
  }

  it should "successfully do predicate with or complex or and and clause" in {
    val source =
      s"""
         | select count(user) as frequency,
         |        user.sessions.events.id as id
         | from schema Unity
         | where (user.sessions.events.id == 2 || user.sessions.events.id == 3 ||user.sessions.events.id == 6) &&
         | user.deviceModelId == 555666
     """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array((16, 2), (16, 3), (16, 6)))
    })
  }

  it should "successfully do predicate with through scalar reference clause" in {
    val source3 =
      s"""
         | select count(user) as frequency,
         |        user.application.firstUse.languageId as id
         | from schema Unity
         | where user.application.firstUse.languageId != 555666
     """.stripMargin

    runTest(source3, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array((10, 111222), (10, 333444), (10, 777888), (10, 888999)))
    })
  }

  it should "successfully do another predicate with through scalar reference clause" in {
    val source3 =
      s"""
         | select count(user) as frequency,
         |        user.application.firstUse.languageId as id
         | from schema Unity
         | where user.application.firstUse.languageId == 555666
     """.stripMargin

    runTest(source3, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array((10, 555666)))
    })
  }

  it should "successfully do predicate with existence check in child" in {
    val source =
      s"""
         | select count(user) as frequency,
         |        user.sessions.id % 2 as id
         | from schema Unity
         | where user.sessions.events.id == 2
     """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array((50,0), (50,1)))
    })
  }

  it should "successfully do predicate with another existence check in child" in {
    val source =
      s"""
         | select count(user.sessions) as frequency,
         |        user.sessions.id % 2 as id
         | from schema Unity
         | where user.sessions.events.id == 2
     """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array((569,0), (568,1)))
    })
  }

  it should "successfully do predicate with negation tunneled existence check" in {
    // staticSweep = new B7D5F13682F7C47289983D55F4B4FED7E
    val source =
      s"""
         | select count(user) as frequency,
         |        user.sessions.events.id as id
         | from schema Unity
         | where user.application.firstUse.languageId != 555666
     """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array((40,1), (40,2), (40,3), (40,4), (40,5), (40,6), (40,7), (40,8), (40,9), (40,10), (40,11)))
    })
    StaticSweep = null
  }

  it should "successfully do predicate with equality tunneled check" in {
    val source =
      s"""
         | select count(user) as frequency,
         |        user.sessions.events.id as id
         | from schema Unity
         | where user.application.firstUse.languageId == 555666
     """.stripMargin

    runTest(source, 200, 200, { result =>
      val r = prepResults(result)

      r should equal(Array((10,1), (10,2), (10,3), (10,4), (10,5), (10,6), (10,7), (10,8), (10,9), (10,10), (10,11)))
    })
    StaticSweep = null
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
