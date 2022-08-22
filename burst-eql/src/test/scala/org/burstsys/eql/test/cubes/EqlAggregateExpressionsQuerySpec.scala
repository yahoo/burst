/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.cubes

import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.motif.common.ParseException
import org.burstsys.vitals.errors.VitalsException

final
class EqlAggregateExpressionsQuerySpec extends EqlAlloyTestRunner {
  it should "single count aggregate expression dimension with scope above eval level" in {
    val source =
      s"""
         | select count(user) as 'one',
         |        (count(user.sessions.events) scope user.sessions where user.sessions.id % 2 == 0) as 'two'
         | from schema Unity
       """.stripMargin

    runTest(source, 200, 200, { result =>
      checkResultGroup(result) should be > 0L

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("one")).asLong, row(names("two")).asLong)
      }

      r should contain theSameElementsAs Array((50, 0), (50, 10))
    })
  }

  it should "calculated double count aggregate expression" in {
    val source =
      s"""
         |select
         |  count(user) as one,
         |  count(user.sessions) as two,
         |  count(user.sessions.events) as three,
         |  (count(user.sessions.events) + count(user.sessions) + 1) as four
         |from schema unity
       """.stripMargin

    runTest(source, 200, 200, { result =>
      checkResultGroup(result) should be > 0L

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("one")).asLong, row(names("four")).asLong)
      }

      r should contain theSameElementsAs Array((50, 276))
    })
  }

  it should "calculated summed single count" in {
    val source =
      s"""
         |select
         |  count(user) as one,
         |  count(user.sessions) as two,
         |  count(user.sessions.events) as three,
         |  sum(count(user.sessions.events)) as four
         |from schema unity
       """.stripMargin

    runTest(source, 200, 200, { result =>
      checkResultGroup(result) should be > 0L

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => {
          row(names("three")).asLong should equal(row(names("four")).asLong)
          (row(names("one")).asLong, row(names("four")).asLong)
        }
      }

      r should contain theSameElementsAs Array((50, 12500))
    })
  }

  it should "calculated summed count aggregate expression" in {
    val source =
      s"""
         |select
         |  count(user) as one,
         |  count(user.sessions) as two,
         |  count(user.sessions.events) as three,
         |  sum(count(user.sessions.events) + count(user.sessions) + 1) as four
         |from schema unity
       """.stripMargin

    runTest(source, 200, 200, { result =>
      checkResultGroup(result) should be > 0L

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => {
          (row(names("three")).asLong + row(names("two")).asLong + row(names("one")).asLong) should equal(row(names("four")).asLong)
          (row(names("one")).asLong, row(names("four")).asLong)
        }
      }

      r should contain theSameElementsAs Array((50, 13800))
    })
  }

  it should "single count aggregate expression dimension with scope at eval level" in {
    val source =
      s"""
         | select count(user) as 'one',
         |        (count(user.sessions.events) scope user.sessions where user.sessions.events.id % 2 == 0) as 'two'
         | from schema Unity
       """.stripMargin

    runTest(source, 200, 200, { result =>
      checkResultGroup(result) should be > 0L

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("one")).asLong, row(names("two")).asLong)
      }

      r should contain theSameElementsAs Array((50, 4), (50, 5))
    })
  }

  it should "fail a count aggregate expression dimension with scope too low" in {
    val source =
      s"""
         | select count(user) as 'one',
         |        (count(user) scope user.sessions where user.sessions.id % 2 == 0) as 'two'
         | from schema Unity
       """.stripMargin

    intercept[ParseException] {
      runTest(source, 200, 200, { result =>
        checkResultGroup(result) should be > 0L

        val names = result.resultSets(0).columnNames.zipWithIndex.toMap
        val r = result.resultSets(0).rowSet.map {
          row => (row(names("one")).asLong, row(names("two")).asLong)
        }

        r should contain theSameElementsAs Array((50, 0), (50, 10))
      })
    }
  }

 ignore should "single sum aggregate expression dimension with scope (Hydra Bug)" in {
    val source =
      s"""
         | select sum(2) as 'one',
         |        (sum(1 + 2) scope user.sessions where user.sessions.id % 2 == 0) as 'two'
         | from schema Unity
       """.stripMargin

    runTest(source, 200, 200, { result =>
      checkResultGroup(result) should be > 0L

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("one")).asByte, row(names("two")).asLong)
      }

      r should contain theSameElementsAs Array((100, 0), (100, 3))
    })
  }

  /*
  s"""
     | select count(user) as 'one',
     |        (count(user.sessions) scope user.sessions) as 'two'
     | from schema Unity where user.sessions.id % 2 == 1
       """.stripMargin
   */
  it should "count expression dimension with scope with where" in {
    val source =
      s"""
         | select count(user) as 'one',
         |        (count(user.sessions) scope user.sessions) as 'two'
         | from schema Unity where user.sessions.id % 2 == 1
       """.stripMargin

    runTest(source, 200, 200, { result =>
      checkResultGroup(result) should be > 0L

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("one")).asLong, row(names("two")).asLong)
      }

      r should contain theSameElementsAs Array((50, 1))
    })
  }

  it should "multiple aggregate expression dimension with scope" in {
    val source =
      s"""
         | select count(user) as 'one',
         |        (count(user.sessions.events) scope user.sessions where user.sessions.id % 2 == 0) as 'two',
         |        (count(user.sessions.events.parameters) scope user.sessions.events where user.sessions.id % 2 == 0) as 'three'
         | from schema Unity
       """.stripMargin

    runTest(source, 200, 200, { result =>
      checkResultGroup(result) should be > 0L

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("one")).asLong, row(names("two")).asLong, row(names("three")).asLong)
      }

      r.sortBy(_._3).sortBy(_._2) should equal(Array((50, 0, 0), (50, 10, 5)))
    })
  }

  it should "multiple aggregate expression dimension with scope and where with early evaluation" in {
    val source =
      s"""
         | select count(user) as 'one',
         |        (count(user.sessions.events) scope user.sessions where user.sessions.id % 2 == 0) as 'two',
         |        (count(user.sessions.events.parameters) scope user.sessions.events where user.sessions.id % 2 == 0) as 'three'
         | from schema Unity
         | where user.sessions.id % 2 == 0
       """.stripMargin

    runTest(source, 200, 200, { result =>
      checkResultGroup(result) should be > 0L

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("one")).asLong, row(names("two")).asLong, row(names("three")).asLong)
      }

      r.sortBy(_._3).sortBy(_._2) should equal(Array((50, 10, 5)))
    })
  }

  // TODO we need working control verbs for this to work
  ignore should "multiple aggregate expression dimension with scope and where with late evaluation (Hydra Feature: control verbs)" in {
    val source =
      s"""
         | select count(user) as 'one',
         |        (count(user.sessions.events) scope user.sessions where user.sessions.id % 2 == 0) as 'two',
         |        (count(user.sessions.events.parameters) scope user.sessions.events where user.sessions.id % 2 == 0) as 'three'
         | from schema Unity
         | where (count(user.sessions.events) scope user.sessions where user.sessions.id % 2 == 0) > 0
       """.stripMargin

    runTest(source, 200, 200, { result =>
      checkResultGroup(result) should be > 0L

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("one")).asLong, row(names("two")).asLong, row(names("three")).asLong)
      }

      r.sortBy(_._3).sortBy(_._2) should equal(Array((50, 10, 5)))
    })
  }

  it should "support single aggregate expression dimension" in {
    val source =
      s"""
         | select (count(user.sessions) where user.sessions.id % 2 == 0) as 'one',
         |        user.interests.value as 'two'
         | from schema Unity
       """.stripMargin

    runTest(source, 200, 200, { result =>
      checkResultGroup(result) should be > 0L

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("one")).asLong, row(names("two")).asLong)
      }

      r should contain theSameElementsAs Array(
        (13, 1), (12, 1), (13, 2), (12, 2), (13, 3), (12, 3), (13, 4), (12, 4), (12, 5), (13, 5),
        (12, 6), (13, 6), (12, 7), (13, 7), (12, 8), (13, 8), (12, 9), (13, 9), (12, 10), (13, 10)
      )
    })
  }

  it should "multiple aggregate expression dimmensions" in {
    val source =
      s"""
         | select (count(user.sessions) where user.sessions.id % 2 == 0) as 'one',
         |        (count(user.interests) where user.interests.value % 2 == 1) as 'two'
         | from schema Unity
       """.stripMargin

    runTest(source, 200, 200, { result =>
      checkResultGroup(result) should be > 0L

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("one")).asLong, row(names("two")).asLong)
      }

      r should contain theSameElementsAs Array((13, 1), (12, 1))
    })
  }

  it should "successfully generate where with complex and test" in {
    val source =
      s"""
         | select count(user) as 'users'
         | from schema Unity
         | where
         |       (count(user.sessions) where user.sessions.id % 2 == 0) > 0 and
         |       (count(user.interests) where user.interests.value % 2 == 0) > 0
       """.stripMargin

    runTest(source, 200, 200, { result =>
      checkResultGroup(result) should be > 0L

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => row(names("users")).asLong
      }

      r should equal(Array(50))
    })
  }

  it should "successfully generate where with and test" in {
    val source =
      s"""
         | select count(user) as 'users'
         | from schema Unity
         | where count(user.sessions) > 0 and count(user.interests) > 0
       """.stripMargin

    runTest(source, 200, 200, { result =>
      checkResultGroup(result) should be > 0L

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => row(names("users")).asLong
      }

      r should equal(Array(50))
    })
  }

  it should "successfully generate where with or test" in {
    val source =
      s"""
         | select count(user) as 'users'
         | from schema Unity
         | where count(user.sessions) > 0 or count(user.interests) > 0
       """.stripMargin

    runTest(source, 200, 200, { result =>
      checkResultGroup(result) should be > 0L

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => row(names("users")).asLong
      }

      r should equal(Array(50))
    })
  }

  it should "successfully generate where with positive aggregate test" in {
    val source =
      s"""
         | select count(user) as 'users'
         | from schema Unity
         | where count(user.sessions) > 0
       """.stripMargin

    runTest(source, 200, 200, { result =>
      checkResultGroup(result) should be > 0L

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => row(names("users")).asLong
      }

      r should equal(Array(50))
    })
  }

  it should "successfully generate where with negative aggregate test" in {
    val source =
      s"""
         | select count(user) as 'users'
         | from schema Unity
         | where count(user.sessions) < 0
       """.stripMargin

    runTest(source, 200, 200, { result =>
      checkResultGroup(result) should equal(0)
    })
  }

  it should "successfully generate aggregate expression at root with where clause" in {
    val source =
      s"""
         | select count(user) as 'users'
         | from schema Unity
         | where (count(user.sessions) where user.sessions.events.id > 0) > 0
       """.stripMargin

    runTest(source, 200, 200, { result =>
      checkResultGroup(result) should be > 0L

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => row(names("users")).asLong
      }

      r should equal(Array(50))
    })
  }

  it should "successfully generate aggregate expression in child with where clause" in {
    val source =
      s"""
         | select count(user) as 'users'
         | from schema Unity
         | where (count(user.sessions.events) where size(user.sessions.events.parameters) > 0) > 0
       """.stripMargin

    runTest(source, 200, 200, { result =>
      checkResultGroup(result) should be > 0L

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => row(names("users")).asLong
      }

      r should equal(Array(50))
    })
  }

  it should "successfully generate two level aggregate expression" in {
    val source =
      s"""
         | select count(user) as 'users'
         | from schema Unity
         | where (
         |    count(user.sessions) where (
         |       count(user.sessions.events) where size(user.sessions.events.parameters) > 1
         |    ) > 0
         | ) > 0
       """.stripMargin

    runTest(source, 200, 200, { result =>
      checkResultGroup(result) should be > 0L

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => row(names("users")).asLong
      }

      r should equal(Array(50))
    })
  }

  it should "successfully generate three level deep aggregate expression" in {
    val source =
      s"""
         | select count(user) as 'users'
         | from schema Unity
         | where (
         |    count(user.sessions) where (
         |       count(user.sessions.events) where (
         |          count(user.sessions.events.parameters) where user.sessions.events.parameters.key is not null
         |       ) > 1
         |    ) > 0
         | ) > 0
       """.stripMargin

    runTest(source, 200, 200, { result =>
      checkResultGroup(result) should be > 0L

      val names = result.resultSets(0).columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => row(names("users")).asLong
      }

      r should equal(Array(50))
    })
  }

  it should "successfully generate negated three level deep aggregate expression" in {
    val source =
      s"""
         | select count(user) as 'users'
         | from schema Unity
         | where (
         |    count(user.sessions) where (
         |       count(user.sessions.events) where (
         |          count(user.sessions.events.parameters) where user.sessions.events.parameters.key is null
         |       ) > 1
         |    ) > 0
         | ) > 0
       """.stripMargin

    runTest(source, 200, 200, { result =>
      checkResultGroup(result) should equal(0L)
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
