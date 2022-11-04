/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.expressions

import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.wave.execution.model.result.set.FabricResultSet
import org.burstsys.hydra.runtime.StaticSweep
import org.burstsys.motif.common.ParseException
import org.burstsys.vitals.errors.VitalsException

/**
  * String literal expressions
  *
  */
final
class EqlFunctionalSpec extends EqlAlloyTestRunner {

  it should "successfully do a simple size function" in {
    StaticSweep = null
    val source = "select count(user) as frequency from schema Unity where size(user.sessions.events) > 2"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array(50))
    })
  }

  it should "successfully do a function as a dimension" in {
    StaticSweep = null
    val source = "select count(user) as c, size(user.sessions.events) as frequency from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array(10))
    })
  }

  it should "successfully do a length on a reference vector" in {
    StaticSweep = null
    val source = "select count(user) as frequency from schema Unity where size(user.sessions.events) > 2"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array(50))
    })
  }

  it should "successfully do a length on a value map" in {
    StaticSweep = null
    val source = "select count(user) as frequency from schema Unity where size(user.sessions.parameters) > 2"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array(50))
    })
  }

  it should "successfully do a length on a value vector" in {
    StaticSweep = null
    val source = "select count(user) as frequency from schema Unity where size(user.interests) < 10"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array(50))
    })
  }

  it should "successfully do a length on event parameters in dimension" in {
    StaticSweep = null
    val source =
      s"""
         | select count(user.sessions.events) as events, size(user.sessions.events.parameters) as parms
         | from schema Unity
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val resultSet = checkResults(result)

      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => Array(row(names("events")).asLong,row(names("parms")).asLong)
      }
      r should equal(Array(Array(12500, 5)))
    })
  }

  it should "successfully do a datetime conversion on a string literal" in {
    StaticSweep = null
    val source = "select datetime(\"2012-12-01T10:34:01Z\") as time from schema Unity"
    runTest(source, 200, 200, { result =>
      val resultSet = checkResults(result)

      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = resultSet.rowSet.map {
        row => row(names("time")).asLong
      }
      r should equal(Array(1354358041000L))
    })
  }

  it should "catch a malformed datetime conversion on a string literal" in {
    StaticSweep = null
    val source = "select datetime(\"2012-13-01T10:34:01Z\") as time from schema Unity"

    intercept[ParseException] {
      runTest(source, 200, 200, { result =>
        val resultSet = checkResults(result)

        val names = resultSet.columnNames.zipWithIndex.toMap
        val r = resultSet.rowSet.map {
          row => row(names("time")).asLong
        }
        r should equal(Array(1354358041000L))
      })
    }
  }

  /* Generic testing and prep */
  def prepResults(result: FabricResultGroup): Array[Long] = {
    val resultSet = checkResults(result)

    val names = resultSet.columnNames.zipWithIndex.toMap
    resultSet.rowSet.map {
      row => row(names("frequency")).asLong
    }
  }
}
