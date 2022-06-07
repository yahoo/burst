/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.expressions

import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.runtime.StaticSweep
import org.burstsys.vitals.errors.VitalsException
import org.scalatest.Ignore

/**
  * String literal expressions
  *
  */
@Ignore
final
class EqlStringLitSpec extends EqlAlloyTestRunner {

  it should "successfully do simple single quote string" in {
    StaticSweep = null
    val source = "select 'foo' as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array("foo"))
    })
  }

  it should "successfully do simple single quote unicode string" in {
    StaticSweep = null
    val source = "select 'На берегу пустынных волн' as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array("На берегу пустынных волн"))
    })
  }

  it should "successfully do simple single quote string with double quote" in {
    StaticSweep = null
    val source = "select 'fo\"o\"\"' as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array("fo\"o\"\""))
    })
  }

  it should "successfully do simple double quote string with single quote" in {
    StaticSweep = null
    val source = "select \"fo'o''\" as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array("fo'o''"))
    })
  }

  it should "successfully do simple single quote string with escape" in {
    StaticSweep = null
    val source = """select "fo""o" as id from schema Unity"""
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array("fo\"o"))
    })
  }

  it should "successfully do simple single quote string with alternate escape" in {
    StaticSweep = null
    val source = "select 'fo\\'o' as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array("fo'o"))
    })
  }

  it should "successfully do simple double quote string" in {
    StaticSweep = null
    val source = "select " + '"' + "foo" + '"' + " as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array("foo"))
    })
  }

  it should "successfully do simple double quote string with escape" in {
    StaticSweep = null
    val source = "select \"fo\"\"o\" as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array("fo\"o"))
    })
  }

  it should "successfully do simple double quote string with alternate escape" in {
    StaticSweep = null
    val source = "select \"fo\\\"o\" as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array("fo\"o"))
    })
  }

  it should "successfully do simple double quote string with various escapes" in {
    StaticSweep = null
    val source = "select \"\tfo\no\rbar\" as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array("\tfo\no\rbar"))
    })
  }

  /* Generic testing and prep */
  def prepResults(result: FabricResultGroup): Array[String] = {
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
      row => row(names("id")).asString
    }
  }

}
