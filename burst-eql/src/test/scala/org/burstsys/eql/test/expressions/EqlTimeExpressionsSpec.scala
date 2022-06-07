/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.expressions

import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.runtime.StaticSweep
import org.burstsys.vitals.errors.VitalsException

/**
  * Date time expression
  *
  */
final
class EqlTimeExpressionsSpec extends EqlAlloyTestRunner {

  it should "successfully do dimension time by secondofminute expression" in {
    StaticSweep = null
    val source = s"select count(user) as frequency, secondofminute(user.sessions.startTime) + 2 as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((4,2), (5,3), (4,4), (5,5), (5,6), (4,7), (5,8), (5,9), (4,10), (5,11), (4,12), (4,13), (5,14), (3,15)))
    })
  }

  it should "successfully do dimension time by minuteofhour expression" in {
    StaticSweep = null
    val source = s"select count(user) as frequency, minuteofhour(user.sessions.startTime) + 2 as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,3)))
    })
  }


  it should "successfully do dimension time by hourofday expression" in {
    StaticSweep = null
    val source = s"select count(user) as frequency, hourofday(user.sessions.startTime) + 2 as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,3)))
    })
  }

  it should "successfully do dimension time by dayofweek expression" in {
    val source = s"select count(user) as frequency, dayofweek(user.sessions.startTime) + 2 as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,3)))
    })
  }

  it should "successfully do dimension time by dayofmonth expression" in {
    val source = s"select count(user) as frequency, dayofmonth(user.sessions.startTime) + 2 as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,3)))
    })
  }

  it should "successfully do dimension time by dayofyear expression" in {
    val source = s"select count(user) as frequency, dayofyear(user.sessions.startTime) + 2 as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,3)))
    })
  }

  it should "successfully do dimension time by weekofyear expression" in {
    val source = s"select count(user) as frequency, weekofyear(user.sessions.startTime) + 2 as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,3)))
    })
  }

  it should "successfully do dimension time by monthofyear expression" in {
    val source = s"select count(user) as frequency, monthofyear(user.sessions.startTime) + 2 as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,3)))
    })
  }

  it should "successfully do dimension time by theyear expression" in {
    val source = s"select count(user) as frequency, theyear(user.sessions.startTime) + 2 as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,2020)))
    })
  }

  it should "successfully do dimension time by seconds expression" in {
    val source = s"select count(user) as frequency, seconds(user.sessions.events.id)+1 as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1001), (50,2001), (50,3001), (50,4001), (50,5001), (50,6001), (50,7001), (50,8001),
        (50,9001), (50,10001), (50,11001)))
    })
  }

  it should "successfully do dimension time by minutes expression" in {
    val source = s"select count(user) as frequency, minutes(user.sessions.events.id)+1 as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,60001), (50,120001), (50,180001), (50,240001), (50,300001), (50,360001), (50,420001),
        (50,480001), (50,540001), (50,600001), (50,660001)))
    })
  }

  it should "successfully do dimension time by hours expression" in {
    val source = s"select count(user) as frequency, hours(user.sessions.events.id)+1 as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,3600001), (50,7200001), (50,10800001), (50,14400001), (50,18000001), (50,21600001),
        (50,25200001), (50,28800001), (50,32400001), (50,36000001), (50,39600001)))
    })
  }

  it should "successfully do dimension time by days expression" in {
    val source = s"select count(user) as frequency, days(user.sessions.events.id)+1 as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,86400001), (50,172800001), (50,259200001), (50,345600001), (50,432000001),
        (50,518400001), (50,604800001), (50,691200001), (50,777600001), (50,864000001), (50,950400001)))
    })
  }

  it should "successfully do dimension time by weeks expression" in {
    val source = s"select count(user) as frequency, weeks(user.sessions.events.id)+1 as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,604800001), (50,1209600001), (50,1814400001), (50,2419200001L), (50,3024000001L),
        (50,3628800001L), (50,4233600001L), (50,4838400001L), (50,5443200001L), (50,6048000001L), (50,6652800001L)))
    })
  }

  it should "successfully do dimension time by second expression" in {
    val source = s"select count(user) as frequency, second(user.sessions.startTime)+1 as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((4,1514797260001L), (5,1514797261001L), (4,1514797262001L), (5,1514797263001L),
        (5,1514797264001L), (4,1514797265001L), (5,1514797266001L), (5,1514797267001L), (4,1514797268001L),
        (5,1514797269001L), (4,1514797270001L), (4,1514797271001L), (5,1514797272001L), (3,1514797273001L)))
    })
  }

  it should "successfully do dimension time by minute expression" in {
    val source = s"select count(user) as frequency, minute(user.sessions.startTime)+1 as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1514797260001L)))
    })
  }

  it should "successfully do dimension time by hour expression" in {
    val source = s"select count(user) as frequency, hour(user.sessions.startTime)+1 as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1514797200001L)))
    })
  }

  it should "successfully do dimension time by day expression" in {
    val source = s"select count(user) as frequency, day(user.sessions.startTime)+1 as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1514793600001L)))
    })
  }

  it should "successfully do dimension time by week expression" in {
    val source = s"select count(user) as frequency, week(user.sessions.startTime)+1 as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1514793600001L)))
    })
  }

  it should "successfully do dimension time by month expression" in {
    val source = s"select count(user) as frequency, month(user.sessions.startTime)+1 as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1514793600001L)))
    })
  }

  it should "successfully do dimension time by quarter expression" in {
    val source = s"select count(user) as frequency, quarter(user.sessions.startTime)+1 as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1514793600001L)))
    })
  }

  it should "successfully do dimension time by half expression" in {
    val source = s"select count(user) as frequency, half(user.sessions.startTime)+1 as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1514793600001L)))
    })
  }

  it should "successfully do dimension time by year expression" in {
    val source = s"select count(user) as frequency, year(user.sessions.startTime)+1 as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1514793600001L)))
    })
  }

  /* Generic testing and prep */
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
