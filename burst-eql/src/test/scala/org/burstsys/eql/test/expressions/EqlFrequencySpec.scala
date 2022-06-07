/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.expressions

import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.hydra.runtime.StaticSweep

/**
  * Frequency queries
  */

final
class EqlFrequencySpec extends EqlAlloyTestRunner {
  it should "successfully do simple expression frequency" in {
    val source =
      s"""
         | select count(user) as 'users',
         |       frequency(user.sessions, (user.sessions.startTime/100)%10) as 'session'
         |from schema unity
         |       where day(NOW) - day(user.sessions.startTime) > 0
         |limit 100
         |
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => (row(names("users")).asLong, row(names("session")).asLong, row(names("session_frequency")).asLong)
      }

      r.sortBy(_._3).sortBy(_._2) should equal(Array(
        (1,0,2), (1,0,3), (1,0,4), (1,0,5), (1,0,6), (1,0,7), (10,0,9), (1,0,10), (1,1,2), (1,1,3), (1,1,4), (1,1,5),
        (1,1,6), (1,1,7), (9,1,9), (2,1,10), (1,2,2), (2,2,3), (1,2,4), (1,2,5), (2,2,6), (1,2,7), (8,2,9), (2,2,10),
        (1,3,2), (2,3,3), (1,3,4), (1,3,5), (2,3,6), (1,3,7), (8,3,9), (2,3,10), (1,4,2), (2,4,3), (1,4,4), (1,4,5),
        (2,4,6), (1,4,7), (9,4,9), (1,4,10), (1,5,2), (1,5,3), (2,5,4), (2,5,5), (1,5,6), (1,5,7), (9,5,9), (1,5,10),
        (1,6,2), (1,6,3), (2,6,4), (2,6,5), (1,6,6), (1,6,7), (9,6,9), (1,6,10), (1,7,2), (1,7,3), (2,7,4), (1,7,5),
        (1,7,6), (1,7,7), (9,7,9), (1,7,10), (2,8,2), (1,8,3), (1,8,4), (1,8,5), (1,8,6), (2,8,7), (8,8,9), (1,8,10),
        (2,9,2), (1,9,3), (1,9,4), (1,9,5), (1,9,6), (2,9,7), (8,9,9), (1,9,10)))
    })
  }

  // TODO STRING NOT IN DICTIONARY PROBLEM
  ignore should "successfully do a simple string frequency" in {
    StaticSweep = null
    val source = """
         |select count(user) as users,
         |       frequency(user.sessions, 'time') as 'session'
         |from schema unity
         |       where day(NOW) - day(user.sessions.startTime) > 0
         |
         |""".stripMargin

    runTest(source, 200, 200, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("session")).asString, row(names("session_frequency")).asLong, row(names("users")).asLong)
      }
      r should equal(Array(("time", 25, 50)))
    })
  }

  ignore should "successfully do a calculated frequency" in {
    StaticSweep = null
    val source = """
                   |select count(user) as users,
                   |       100 + frequency(user.sessions, 'time') as 'session'
                   |from schema unity
                   |       where day(NOW) - day(user.sessions.startTime) > 0
                   |
                   |""".stripMargin

    runTest(source, 200, 200, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("session")).asString, row(names("session_frequency")).asLong, row(names("users")).asLong)
      }
      r should equal(Array(("time", 125, 50)))
    })
  }

  it should "successfully do frequency with additional split grouping" in {
    StaticSweep = null
    val source =
      s"""
         | select count(user) as 'users',
         |       split(frequency(user.sessions, (user.sessions.startTime/100)%10), 0, 3, 11) as 'session'
         |from schema unity
         |       where day(NOW) - day(user.sessions.startTime) > 0
         |limit 100
         |
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("session")).asLong, row(names("session_frequency")).asLong, row(names("users")).asLong)
      }
      r.sortBy(_._3).sortBy(_._2) should equal(
        Array(
          (4,0,1), (7,0,1), (2,0,1), (5,0,1), (0,0,1), (3,0,1), (6,0,1), (1,0,1), (8,0,2), (9,0,2), (8,3,15),
          (9,3,15), (1,3,16), (7,3,16), (0,3,16), (4,3,17), (2,3,17), (5,3,17), (3,3,17), (6,3,17)))
    })
  }

  it should "successfully do frequency with additional enum grouping" in {
    StaticSweep = null
    val source =
      s"""
         | select count(user) as 'users',
         |       enum(frequency(user.sessions, (user.sessions.startTime/100)%10), 0, 3, 1000) as 'session'
         |from schema unity
         |       where day(NOW) - day(user.sessions.startTime) > 0
         |limit 100
         |
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("session")).asLong, row(names("session_frequency")).asLong, row(names("users")).asLong)
      }
      r.sortBy(_._3).sortBy(_._2) should equal(
        Array(
          (1,3,1), (7,3,1), (5,3,1), (8,3,1), (0,3,1), (6,3,1), (9,3,1), (4,3,2), (2,3,2), (3,3,2), (0,1000,16),
          (3,1000,16), (9,1000,16), (1,1000,16), (4,1000,16), (7,1000,16), (2,1000,16), (8,1000,16), (6,1000,17),
          (5,1000,17)))
    })
  }
}
