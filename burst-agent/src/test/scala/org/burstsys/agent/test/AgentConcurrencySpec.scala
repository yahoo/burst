/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.test

import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.burstsys.agent.configuration
import org.burstsys.fabric.wave.execution.model.execute.group.{FabricGroupKey, FabricGroupUid}
import org.burstsys.fabric.wave.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.wave.execution.model.result.FabricExecuteResult
import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.wave.execution.model.result.set.FabricResultSet
import org.burstsys.fabric.wave.execution.model.result.status.FabricNotReadyResultStatus
import org.burstsys.fabric.wave.metadata.model.over
import org.burstsys.fabric.wave.metadata.model.over.FabricOver
import org.burstsys.tesla.thread.request.{TeslaRequestFuture, _}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
 *
 */
final
class AgentConcurrencySpec extends AgentQuerySpecSupport {

  it should "exceed max concurrency" in {
    // set a very low level of concurrency
    configuration.burstAgentApiMaxConcurrencyProperty.set(2)

    val failCount = new CountDownLatch(1)

    // get a bunch going at once
    val futures = for (_ <- 0 until 5) yield {
      val future = agentClient.execute(source = "mock kjdhsg", over = over.FabricOver(), guid = "foo")
      future onComplete {
        case Failure(t) =>
          throw t
        case Success(r) =>
          r.resultStatus match {
            case FabricNotReadyResultStatus =>
              log info r.resultMessage
              failCount.countDown()
            case _ =>
          }
      }
      future
    }

    // make sure they are all done.
    Await.ready(Future.sequence(futures), 10 minutes)

    // make sure at least one failed with a not ready status because there were too many at once
    failCount.await(10, TimeUnit.SECONDS)
  }

  override
  def executeGroupAsWave(groupUid: FabricGroupUid, source: String, over: FabricOver, call: Option[FabricCall]): Future[FabricExecuteResult] = {
    Thread.sleep(1000)
    TeslaRequestFuture {
      FabricExecuteResult(
        resultGroup = Some(FabricResultGroup(groupKey = FabricGroupKey(groupName = "mock", groupUid = groupUid), resultSets = Map(0 -> FabricResultSet()), rowCount = 1))
      )
    }
  }


}
