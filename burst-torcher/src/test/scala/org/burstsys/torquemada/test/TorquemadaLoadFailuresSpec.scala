/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.torquemada.test

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.burstsys.catalog.model.domain._
import org.burstsys.fabric.wave.execution.model.execute.group.FabricGroupUid
import org.burstsys.fabric.wave.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.wave.execution.model.result.FabricExecuteResult
import org.burstsys.fabric.wave.metadata.model.over.FabricOver
import org.burstsys.torquemada.{Driver, Parameters}
import org.burstsys.vitals.threading
import org.apache.logging.log4j.Level
import org.burstsys.vitals.errors.VitalsException

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.util.{Failure, Success, Try}

class TorquemadaLoadFailuresSpec extends TorquemadaHelper {

  it must "log and count load failures" in {
    resetTestCounters()

    val params = Parameters.parseArguments(Array("-m", "localhost", "-v", "src/test/resources/primaryKeys.json"))
    if (params.isEmpty) fail("parser failed")

    val logCounter = new AtomicInteger()
    val messages = new mutable.ArrayBuffer[(Level, String)]
    val driver = Driver(params.get, agentClient, catalogClient)
    driver.addListener((level, message) => {
      messages += level -> message
      if (level == Level.ERROR)
        logCounter.incrementAndGet()
    }).start.run.stop

    assert(driver.runningTime >= TimeUnit.MILLISECONDS.toNanos(1))
    assert(logCounter.get() == 1)
    assert(driver.jobSettings.statTable.summaryStatistics.firstQueryFailures.getCount == 1)
    assert(driver.jobSettings.statTable.summaryStatistics.queryFailures.getCount == 0)
  }

  it must "support stop on fail" in {
    resetTestCounters()

    val params = Parameters.parseArguments(Array("-m", "localhost", "-v", "src/test/resources/primaryKeys-StopOnFail.json"))
    if (params.isEmpty) fail("parser failed")

    val logCounter = new AtomicInteger()
    val messages = new mutable.ArrayBuffer[(Level, String)]
    val driver = Driver(params.get, agentClient, catalogClient)
    driver.addListener((level, message) => {
      messages += level -> message
      if (level == Level.ERROR)
        logCounter.incrementAndGet()
    }).start.run.stop

    assert(driver.runningTime >= TimeUnit.MILLISECONDS.toNanos(1))
    assert(logCounter.get() == 1)
    assert(driver.jobSettings.statTable.summaryStatistics.firstQueryFailures.getCount == 1)
    assert(driver.jobSettings.statTable.summaryStatistics.queryFailures.getCount == 0)
  }

  it must "report load failures by project id if possible" in {
    resetTestCounters()

    val params = Parameters.parseArguments(Array("-m", "localhost", "-v", "src/test/resources/projectIds.json"))
    if (params.isEmpty) fail("parser failed")

    implicit val (catalogClient, agentClient) = Driver.openClients(params.get)
    // tag the canned domains with a "project_id" tag so they can be found
    catalogClient.allDomains() match {
      case Success(domains) =>
        for ((d, n) <- domains.zipWithIndex) {
          val labels = mutable.Map[String, String]()
          labels ++= d.labels.getOrElse(Map())
          labels("project_id") = (101 + n).toString
          // "burst.view.project.id"
          catalogClient.ensureDomain(d.copy(labels = Some(labels))) match {
            case Success(_) =>
            case Failure(e) =>
              fail(s"unable to add label to domain ${d.pk}- ${d.moniker}", e)
          }
        }
      case Failure(e) =>
        fail(s"unable to retrieve domains", e)
    }

    val logCounter = new AtomicInteger()
    val messages = new mutable.ArrayBuffer[(Level, String)]
    val driver = Driver(params.get, agentClient, catalogClient)
    driver.addListener((level, message) => {
      messages += level -> message
      if (level == Level.ERROR)
        logCounter.incrementAndGet()
    }).start.run.stop

    assert(driver.runningTime >= TimeUnit.MILLISECONDS.toNanos(1))
    assert(logCounter.get() == 4)
    assert(driver.jobSettings.statTable.summaryStatistics.firstQueryFailures.getCount == 4)
    assert(driver.jobSettings.statTable.summaryStatistics.queryFailures.getCount == 0)
  }

  final override def executeGroupAsWave(groupUid: FabricGroupUid, source: String, over: FabricOver, call: Option[FabricCall]): Future[FabricExecuteResult] =
    Promise[FabricExecuteResult]().failure(VitalsException("test failure").fillInStackTrace()).future

}
