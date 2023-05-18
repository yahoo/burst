/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.control

import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.wave.execution.model.result.set.FabricResultSet
import org.burstsys.vitals.errors.VitalsException

/**
  * Tbis is the EQL predicate tests
  *
  */

final
class EqlParameterizedWhereSpec extends EqlAlloyTestRunner {

  it should "successfully do an int parameter" in {
    val source =
      """
         | select(v:long)
         |     count(user.sessions.events) as frequency
         | from schema Unity
         | where user.sessions.startTime < now - days($v)
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("frequency")).asLong)
      }
      r should equal(Array(12500))
    }, s"""{"v": 100}""")
  }

  it should "successfully do another int parameter" in {
    val source =
      """
        | select(v:long)
        |     count(user.sessions.events) as frequency
        | from schema Unity
        | where user.sessions.startTime > now - days($v)
      """.stripMargin

    runTest(source, 200, 200, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("frequency")).asLong)
      }
      r should equal(Array(12500))
    }, s"""{"v": 100000}""")
  }

  it should "successfully do yet another int parameter" in {
    val source =
      """
        | select(v:long)
        |     count(user.sessions.events) as frequency
        | from schema Unity
        | where user.sessions.startTime > now - days($v)
      """.stripMargin

    runTest(source, 200, 200, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("frequency")).asInteger)
      }
      r should equal(Array())
    }, s"""{"v": 0}""")
  }

  it should "successfully do an integer typed parameters" in {
    val source =
      """
        | select(one:integer)
        |    $one as one
        | from schema Unity
      """.stripMargin

    runTest(source, 200, 200, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("one")).asInteger)
      }
      r should equal(Array(1000))
    },
      s"""
         |{ "one:integer": 1000
         |}
         |""".stripMargin)
  }

  it should "successfully do an double parameters" in {
    val source =
      """
        | select(one:double)
        |    $one as one
        | from schema Unity
      """.stripMargin

    runTest(source, 200, 200, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("one")).asDouble)
      }
      r should equal(Array(1000.0))
    },
      s"""
         |{ "one": 1000.0
         |}
         |""".stripMargin)
  }

  it should "successfully do an double typed parameters" in {
    val source =
      """
        | select(one:double)
        |    $one as one
        | from schema Unity
      """.stripMargin

    runTest(source, 200, 200, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("one")).asDouble)
      }
      r should equal(Array(1000.0))
    },
      s"""
         |{ "one:double": 1000
         |}
         |""".stripMargin)
  }

  // TODO STRING NOT IN DICTIONARY PROBLEM
  ignore should "successfully do an string parameter" in {
    val source =
      """
        | select(one:string)
        |    $one as one
        | from schema Unity
      """.stripMargin

    runTest(source, 200, 200, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("one")).asString)
      }
      r should equal(Array("hello"))
    },
      s"""
         |{ "one": "hello"
         |}
         |""".stripMargin)
  }

  // TODO STRING NOT IN DICTIONARY PROBLEM
  ignore should "successfully do an string typed parameter" in {
    val source =
      """
        | select(one:string)
        |    $one as one
        | from schema Unity
      """.stripMargin

    runTest(source, 200, 200, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("one")).asString)
      }
      r should equal(Array("hello"))
    },
      s"""
         |{ "one:string": "hello"
         |}
         |""".stripMargin)
  }

  // TODO STRING NOT IN DICTIONARY PROBLEM
  ignore should "successfully do multiple parameters (Hydra Bug ???)" in {
    val source =
      """
        | select(one:long, two: integer, three: string, four: double)
        |    $one as one, $two as two, $three as three, $four as four
        | from schema Unity
      """.stripMargin

    runTest(source, 200, 200, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("one")).asLong, row(names("two")).asInteger, row(names("three")).asString, row(names("four")).asDouble)
      }
      r should equal(Array((10, 20, "hello", 40.0)))
    },
      s"""
         |{
         |   "one": 10,
         |   "two : integer": 20,
         |   "three": "hello",
         |   "four": 40.0
         |}
         |""".stripMargin)
  }
}
