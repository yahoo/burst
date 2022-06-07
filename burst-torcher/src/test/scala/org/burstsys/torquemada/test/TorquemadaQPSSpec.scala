/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.torquemada.test

import java.util.concurrent.TimeUnit

import org.burstsys.torquemada.{Driver, Parameters}
import org.scalatest.Ignore

@Ignore
class TorquemadaQPSSpec extends TorquemadaHelper {

  "single-threaded torcher" must "maintain a LPS" in {
    resetTestCounters()
    val params = Parameters.parseArguments(Array("-d", "10seconds", "-lr", "1.0", "src/test/resources/moniker.json"))
    if (params.isEmpty) fail("parser failed")

    implicit val (catalogClient, agentClient) = Driver.openClients(params.get)
    val driver = Driver(params.get, agentClient, catalogClient)
    driver.addListener((l, m) => log.log(l, m)).start.run.stop

    assert(driver.runningTime > TimeUnit.SECONDS.toNanos(10))
    assert(flushCount.get() >= 10)
    assert(flushCount.get() < 12)
    assert(queryCount.get() == flushCount.get() * 3)
  }

  it must "maintain a QPS" in {
    resetTestCounters()
    val params = Parameters.parseArguments(Array("-qr", "1.0", "src/test/resources/moniker-d.txt"))
    if (params.isEmpty) fail("parser failed")

    implicit val (catalogClient, agentClient) = Driver.openClients(params.get)
    val driver = Driver(params.get, agentClient, catalogClient)
    driver.addListener((l, m) => log.log(l, m)).start.run.stop

    assert(driver.runningTime > TimeUnit.SECONDS.toNanos(10))
    assert(flushCount.get() >= 5)
    assert(flushCount.get() <= 6)
    assert(queryCount.get() >= 15)
    assert(queryCount.get() <= 18)
  }

  it must "maintain a QPS and a LPS" in {
    resetTestCounters()
    val params = Parameters.parseArguments(Array("-d", "10seconds", "-v", "src/test/resources/moniker-lr-qr.txt"))
    if (params.isEmpty) fail("parser failed")

    implicit val (catalogClient, agentClient) = Driver.openClients(params.get)
    val driver = Driver(params.get, agentClient, catalogClient)
    driver.addListener((l, m) => log.log(l, m)).start.run.stop

    assert(driver.runningTime > TimeUnit.SECONDS.toNanos(10))
    assert(flushCount.get() == 5)
    assert(queryCount.get() == 25)
  }

}
