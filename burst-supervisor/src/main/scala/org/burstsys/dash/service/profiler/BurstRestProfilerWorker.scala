/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.service.profiler

import org.burstsys.fabric.execution.model.result.FabricExecuteResult
import org.burstsys.fabric.execution.model.result.status.FabricNotReadyResultStatus
import org.burstsys.tesla.thread.request.teslaRequestExecutor
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging.burstStdMsg
import org.burstsys.vitals.uid.newBurstUid

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.{postfixOps, reflectiveCalls}
import scala.util.{Failure, Success}

final case
class BurstRestProfilerWorker(index: Long, profiler: BurstRestProfiler) {

  var error: Option[Throwable] = None

  def start: Future[Unit] = {
    Future[Unit] {
      try {
        var queryNum = profiler.run.queriesTally.incrementAndGet
        while (profiler.running && queryNum < profiler.run.desiredQueryCount + 1) {
          if (profiler.run.queriesPerLoad > 0 && (queryNum % profiler.run.queriesPerLoad) == 0) {
            val message = s"incrementing generation for ${profiler.run.over}"
            log debug message
            profiler.catalog.updateViewGeneration(profiler.run.over.viewKey)
            profiler.addProfilerEvent(message)
          }
          val start = System.nanoTime()
          val execution = newExecution(queryNum) andThen { case _ =>
            profiler.run.scanCountTally.increment()
            profiler.run.scanTimeTally.add(System.nanoTime - start)
          }
          Await.ready(execution, 2 minutes)
          queryNum = profiler.run.queriesTally.incrementAndGet()
        }
      } catch safely {
        case t: Throwable =>
          profiler.addProfilerEvent(s"Worker $index encountered exception ${t.getLocalizedMessage}")
          profiler.run.failuresTally.incrementAndGet()
          log error burstStdMsg(s"", t)
          error = Some(t)
      }
    }
  }

  def newExecution(queryNum: Long): Future[_] = {
    log info s"newExecution($index, $queryNum)"
    profiler.agent.execute(profiler.run.source, profiler.run.over, newBurstUid) andThen {
      case Success(result) if result.resultStatus.isSuccess =>
        profiler.run.successesTally.incrementAndGet()
        processLoadMetrics(result)
      case Success(result) =>
        profiler.run.failuresTally.incrementAndGet()
        profiler.addProfilerEvent(s"ERROR: ${result.resultStatus}:\n\t${result.resultMessage}", isError = true)
        if (result.resultStatus == FabricNotReadyResultStatus) Thread.sleep(500)
      case Failure(throwable) =>
        profiler.run.failuresTally.incrementAndGet()
        profiler.addProfilerEvent(s"ERROR: ${printStack(throwable)}", isError = true)
    }
  }

  private def processLoadMetrics(q: FabricExecuteResult): Unit = {
    val metrics = q.resultGroup.get.groupMetrics.generationMetrics
    val genClk = metrics.generationKey.generationClock
    val currentClk = profiler.run.lastGenerationClock.get
    val isNewGeneration = currentClk != genClk
    if (isNewGeneration && profiler.run.lastGenerationClock.compareAndSet(currentClk, genClk)) {
      profiler.run.loadSizeTally.add(metrics.byteCount)
      profiler.run.loadTimeTally.add(q.resultGroup.get.groupMetrics.generationMetrics.coldLoadTook)
      profiler.run.loadCountTally.incrementAndGet()
    }
  }
}
