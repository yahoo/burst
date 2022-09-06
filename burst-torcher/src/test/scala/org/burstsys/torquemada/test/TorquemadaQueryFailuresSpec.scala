/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.torquemada.test

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import org.burstsys.fabric.execution.model.execute.group.FabricGroupUid
import org.burstsys.fabric.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.execution.model.result.FabricExecuteResult
import org.burstsys.fabric.metadata.model.over.FabricOver
import org.burstsys.torquemada.{Driver, Parameters}

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}
import org.burstsys.tesla.thread.request._

class TorquemadaQueryFailuresSpec extends TorquemadaHelper {

  it must "query failures are counted and logged" in {
    resetTestCounters()

    val params = Parameters.parseArguments(Array("-m", "localhost", "-v", "src/test/resources/primaryKeys.json"))
    if (params.isEmpty) fail("parser failed")

    val driver = Driver(params.get, agentClient, catalogClient)
    driver.addListener((l, m) => log.log(l, m)).start.run.stop

    assert(driver.runningTime >= TimeUnit.MILLISECONDS.toNanos(1))
    assert(driver.jobSettings.statTable.summaryStatistics.firstQueryFailures.getCount == 0)
    assert(driver.jobSettings.statTable.summaryStatistics.queryFailures.getCount == 1)
  }


  var c = new AtomicInteger()

  final override
  def executeGroupAsWave(groupUid: FabricGroupUid, source: String, over: FabricOver, call: Option[FabricCall]): Future[FabricExecuteResult] =
    failQuery(groupUid, source, over, call)

  def failQuery(groupUid: FabricGroupUid, query: String, over: FabricOver, call: Option[FabricCall]): Future[FabricExecuteResult] = {
    val promise = Promise[FabricExecuteResult]()
    if (c.incrementAndGet() % 3 == 0)
      promise.failure(new RuntimeException("test failure"))
    else {
      super.executeGroupAsWave(groupUid, query, over) onComplete {
        case Failure(t) => promise.failure(t)
        case Success(r) => promise.success(r)
      }
    }
    promise.future
  }

}
