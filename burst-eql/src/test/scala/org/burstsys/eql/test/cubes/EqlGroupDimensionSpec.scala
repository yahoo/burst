/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.cubes

import org.burstsys.eql.test.support.EqlAlloyTestRunner

/**
  * Date time expression
  *
  */
final
class EqlGroupDimensionSpec extends EqlAlloyTestRunner {
  it should "dimension by long split with max integer" in {
    val source = s"select count(user.sessions) as 'session_count', split(user.sessions.duration, 3000, 60000, 180000, 600000, 1800000, 9223372036854775807) as 'ids' from schema Unity"
    runTest(source, 200, 200, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("ids")).asLong, row(names("session_count")).asLong)
      } sortBy( _._1)
      r should equal(Array((-9223372036854775808L,1250)))
    })
  }


  it should "dimension by long split" in {
    val source = s"select count(user.sessions) as 'session_count', split(user.sessions.id % 10, 3, 5, 9, 11) as 'ids' from schema Unity"
    runTest(source, 200, 200, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("ids")).asLong, row(names("session_count")).asLong)
      } sortBy( _._1)
      r should equal(Array((-9223372036854775808L,375), (3,250), (5,500), (9,125)))
    })
  }

  it should "dimension by long split again" in {
    val source = s"select count(user.sessions) as 'session_count', split(user.sessions.id % 10, 0, 3, 5) as 'ids' from schema Unity"
    runTest(source, 200, 200, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("ids")).asLong, row(names("session_count")).asLong)
      } sortBy( _._1)
      r should equal(Array((0,375), (3,250), (9223372036854775807L,625)))
    })
  }

  it should "dimension by long enum" in {
    val source = s"select count(user.sessions) as 'session_count', enum(user.sessions.id % 10, 0, 3, 5, 1000) as 'ids' from schema Unity"
    runTest(source, 200, 200, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("ids")).asLong, row(names("session_count")).asLong)
      } sortBy( _._1)
      r should equal(Array((0,125), (3,125), (5,125), (1000,875)))
    })
  }

  it should "dimension by long enum again" in {
    val source = s"select count(user.sessions) as 'session_count', enum(user.sessions.id % 10, 0, 3, 5, 9, 10, 11, 1000) as 'ids' from schema Unity"
    runTest(source, 200, 200, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => (row(names("ids")).asLong, row(names("session_count")).asLong)
      } sortBy( _._1)
      r should equal(Array((0,125), (3,125), (5,125), (9,125), (1000,750)))
    })
  }
}
