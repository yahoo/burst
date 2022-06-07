/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.cubes

import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.runtime.StaticSweep
import org.burstsys.vitals.errors.VitalsException

/**
  * Date time expression
  *
  */
final
class EqlTimeDimensionSpec extends EqlAlloyTestRunner {

  it should "successfully do dimension time by secondofminute" in {
    StaticSweep = null
    val source = s"select count(user) as frequency, secondofminute(user.sessions.startTime) as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((4,0), (5,1), (4,2), (5,3), (5,4), (4,5), (5,6), (5,7), (4,8), (5,9), (4,10), (4,11), (5,12), (3,13)))
    })
  }

  it should "successfully do dimension time by minuteofhour" in {
    StaticSweep = null
    val source = s"select count(user) as frequency, minuteofhour(user.sessions.startTime) as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1)))
    })
  }


  it should "successfully do dimension time by hourofday" in {
    StaticSweep = null
    val source = s"select count(user) as frequency, hourofday(user.sessions.startTime) as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1)))
    })
  }

  it should "successfully do dimension time by dayofweek" in {
    val source = s"select count(user) as frequency, dayofweek(user.sessions.startTime) as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1)))
    })
  }

  it should "successfully do dimension time by dayofmonth" in {
    val source = s"select count(user) as frequency, dayofmonth(user.sessions.startTime) as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1)))
    })
  }

  it should "successfully do dimension time by dayofyear" in {
    val source = s"select count(user) as frequency, dayofyear(user.sessions.startTime) as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1)))
    })
  }

  it should "successfully do dimension time by weekofyear" in {
    val source = s"select count(user) as frequency, weekofyear(user.sessions.startTime) as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1)))
    })
  }

  it should "successfully do dimension time by monthofyear" in {
    val source = s"select count(user) as frequency, monthofyear(user.sessions.startTime) as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1)))
    })
  }

  it should "successfully do dimension time by theyear" in {
    val source = s"select count(user) as frequency, theyear(user.sessions.startTime) as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,2018)))
    })
  }

  it should "successfully do dimension time by seconds" in {
    val source = s"select count(user) as frequency, seconds(user.sessions.events.id) as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1000), (50,2000), (50,3000), (50,4000), (50,5000), (50,6000), (50,7000), (50,8000),
        (50,9000), (50,10000), (50,11000)))
    })
  }

  it should "successfully do dimension time by minutes" in {
    val source = s"select count(user) as frequency, minutes(user.sessions.events.id) as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,60000), (50,120000), (50,180000), (50,240000), (50,300000), (50,360000), (50,420000),
        (50,480000), (50,540000), (50,600000), (50,660000)))
    })
  }

  it should "successfully do dimension time by hours" in {
    val source = s"select count(user) as frequency, hours(user.sessions.events.id) as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,3600000), (50,7200000), (50,10800000), (50,14400000), (50,18000000), (50,21600000),
        (50,25200000), (50,28800000), (50,32400000), (50,36000000), (50,39600000)))
    })
  }

  it should "successfully do dimension time by days" in {
    val source = s"select count(user) as frequency, days(user.sessions.events.id) as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,86400000), (50,172800000), (50,259200000), (50,345600000), (50,432000000),
        (50,518400000), (50,604800000), (50,691200000), (50,777600000), (50,864000000), (50,950400000)))
    })
  }

  it should "successfully do dimension time by weeks" in {
    val source = s"select count(user) as frequency, weeks(user.sessions.events.id) as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,604800000), (50,1209600000), (50,1814400000), (50,2419200000L), (50,3024000000L),
        (50,3628800000L), (50,4233600000L), (50,4838400000L), (50,5443200000L), (50,6048000000L), (50,6652800000L)))
    })
  }

/* TODO BRIAN -- THESE THROW A NULL POINTER EXCEPTION BECAUSE IT IS NOT A REAL TEST AND DOES NOT GET INITIALIZED
  //TODO it should "successfully do dimension time by second" in  {
  an [RuntimeException] should be thrownBy {
    val source = s"select count(user) as frequency, second(user.sessions.startTime) as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1514797200000L)))
    })
  }

  //TODO  it should "successfully do dimension time by minute" in {
  an [RuntimeException] should be thrownBy {
    val source = s"select count(user) as frequency, minute(user.sessions.startTime) as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1514797200000L)))
    })
  }
*/

  it should "successfully do dimension time by hour" in {
    val source = s"select count(user) as frequency, hour(user.sessions.startTime) as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1514797200000L)))
    })
  }

  it should "successfully do dimension time by day" in {
    val source = s"select count(user) as frequency, day(user.sessions.startTime) as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1514793600000L)))
    })
  }

  it should "successfully do dimension time by week" in {
    val source = s"select count(user) as frequency, week(user.sessions.startTime) as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1514793600000L)))
    })
  }

  it should "successfully do dimension time by month" in {
    val source = s"select count(user) as frequency, month(user.sessions.startTime) as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1514793600000L)))
    })
  }

  it should "successfully do dimension time by quarter" in {
    val source = s"select count(user) as frequency, quarter(user.sessions.startTime) as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1514793600000L)))
    })
  }

  it should "successfully do dimension time by half" in {
    val source = s"select count(user) as frequency, half(user.sessions.startTime) as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1514793600000L)))
    })
  }

  it should "successfully do dimension time by year" in {
    val source = s"select count(user) as frequency, year(user.sessions.startTime) as id from schema Unity"
    runTest(source, 200, 200, { result =>
      val r = prepResults(result)
      r should equal(Array((50,1514793600000L)))
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
