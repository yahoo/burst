/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.control

import org.burstsys.eql.test.support.EqlAlloyTestRunner

/**
  * Tbis is the EQL predicate tests
  *
  */

final
class EqlAbortWhereSpec extends EqlAlloyTestRunner {

  it should "successfully do an abort test on an isolated select in a besides (HYDRA BUG)" in {
    val source =
      """
        |  select count(user) as '_selected_' where user.deviceModelId in (555666)
        | beside
        |  select  count(user) as '_total_'
        | from schema unity
       """.stripMargin

    runTest(source, 200, 200, { result =>
      checkResults(result)
      val rs1 = result.resultSets(0)
      var names1 = rs1.columnNames.zipWithIndex.toMap
      val r1 = rs1.rowSet.map {
        row => row(names1("_selected_")).asLong
      }
      r1.sorted should equal(Array(16))
      val rs2 = result.resultSets(1)
      var names2 = rs2.columnNames.zipWithIndex.toMap
      val r2 = rs2.rowSet.map {
        row => row(names2("_total_")).asLong
      }
      r2.sorted should equal(Array(50))
    })
  }

  it should "successfully do an abort test with in test on device id" in {
    val source =
      """
        | select user.deviceModelId as "theid"
        | from schema Unity
        | where user.deviceModelId in (555666, 888999) limit 4
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => row(names("theid")).asLong
      }
      r.sorted should equal(Array(555666, 888999))
    })
  }

  it should "successfully do an abort test with single equality on flurry id" in {
    val source =
      """
        | select user.id as "theid"
        | from schema Unity
        | where user.id in ("User32")
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => row(names("theid")).asString
      }
      r should equal(Array("User32"))
    })
  }

  it should "generate an abort without any dimensions" in {
    val source =
      """
        | select count(user.sessions) as "sessions", count(user.sessions.events) as "events"
        | from schema Unity
        | where user.id in ("User32")
       """.stripMargin

    val hydra = eql.eqlToHydra(Some("Unity"), source)
    assert(hydra.contains("abortRelation"))
  }


  it should "generate an abort without a user-level dimension" in {
    val source =
      """
        | select user.sessions.id as "sessionid", count(user.sessions.events) as "eventcount"
        | from schema Unity
        | where user.id in ("User32")
       """.stripMargin

    val hydra = eql.eqlToHydra(Some("Unity"), source)
    assert(hydra.contains("abortRelation"))
  }



  // TODO: HYDRA BUG #1875 in on multiple strings doesn't work
  ignore should "successfully do an abort test with in on flurry id" in {
    val source =
      """
        | select user.id as "theid"
        | from schema Unity
        | where user.id in ("User32", "User56")
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => row(names("theid")).asString
      }
      r should equal(Array("User56", "User32"))
    })
  }


  // TODO: HYDRA BUG #1875 in on multiple strings doesn't work
  ignore should "successfully do an abort test with or on flurry id" in {
    val source =
      """
         | select user.id as "theid"
         | from schema Unity
         | where user.id == "User32" || user.id == "User56"
       """.stripMargin

    runTest(source, 200, 200, { result =>
      val resultSet = checkResults(result)
      val names = resultSet.columnNames.zipWithIndex.toMap
      val r = result.resultSets(0).rowSet.map {
        row => row(names("theid")).asString
      }
      r should equal(Array("User56", "User32"))
    })
  }
}
