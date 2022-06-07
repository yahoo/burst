/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.torquemada.test

import java.util.concurrent.TimeUnit

import org.burstsys.torquemada.{Driver, Parameters}
import org.scalatest.Ignore

@Ignore
class TorquemadaParallelQPSSpec extends TorquemadaHelper {

  "multi-threaded torcher" must "maintain a LPS" in {
    resetTestCounters()
    val params = Parameters.parseArguments(Array("-m", "localhost",
      "-d", "10seconds", "-lr", "1.0", "-p", "3", "src/test/resources/moniker.json"))
    if (params.isEmpty) fail("parser failed")

    implicit val (catalogClient, agentClient) = Driver.openClients(params.get)
    val driver = Driver(params.get, agentClient, catalogClient)
    driver.addListener((l, m) => log.log(l, m)).start.run.stop

    assert(driver.runningTime > TimeUnit.SECONDS.toNanos(10))
    assert(flushCount.get() >= 10)
    assert(flushCount.get() <= 11)
    assert(driver.jobSettings.statTable.summaryStatistics.firstQueryTimer.getCount.toDouble / TimeUnit.NANOSECONDS.toSeconds(driver.runningTime) <= 1.0)
    assert(queryCount.get() == flushCount.get() * 3)
  }

  it must "maintain a QPS" in {
    resetTestCounters()
    val params = Parameters.parseArguments(Array("-d", "10seconds", "-qr", "1.0", "-p", "4", "-lr", "0.0", "-v", "src/test/resources/moniker-lr-qr-p.txt"))
    if (params.isEmpty) fail("parser failed")

    implicit val (catalogClient, agentClient) = Driver.openClients(params.get)
    val driver = Driver(params.get, agentClient, catalogClient)
    driver.addListener((l, m) => log.log(l, m)).start.run.stop

    assert(driver.runningTime > TimeUnit.SECONDS.toNanos(10))
    assert(flushCount.get() >= 5)
    assert(flushCount.get() <= 8)
    assert(queryCount.get() >= 15)
    assert(queryCount.get() <= 22)
    val rate = driver.jobSettings.statTable.summaryStatistics.queryTimer.getCount.toDouble / TimeUnit.NANOSECONDS.toSeconds(driver.runningTime)
    assert(rate > 0.5 && rate < 1.5) // 1.0 ± 0.5
  }

  it must "maintain a QPS and a LPS" in {
    resetTestCounters()
    val params = Parameters.parseArguments(Array("-d", "10seconds", "src/test/resources/moniker-lr-qr-p.txt"))
    if (params.isEmpty) fail("parser failed")

    implicit val (catalogClient, agentClient) = Driver.openClients(params.get)
    val driver = Driver(params.get, agentClient, catalogClient)
    driver.addListener((l, m) => log.log(l, m)).start.run.stop

    assert(driver.runningTime > TimeUnit.SECONDS.toNanos(10))
    assert(flushCount.get() >= 5 && flushCount.get() <= 6)
    assert(queryCount.get() >= 25 && queryCount.get() <= 30)
    val loadRate = driver.jobSettings.statTable.summaryStatistics.firstQueryTimer.getCount.toDouble / TimeUnit.NANOSECONDS.toSeconds(driver.runningTime)
    assert(loadRate > 0.3 && loadRate < 0.7) // 0.5 ± 0.2
    val queryRate = driver.jobSettings.statTable.summaryStatistics.queryTimer.getCount.toDouble / TimeUnit.NANOSECONDS.toSeconds(driver.runningTime)
    assert(queryRate > 1.5 && queryRate < 2.5) // 2.0 ± 0.5
  }

}
