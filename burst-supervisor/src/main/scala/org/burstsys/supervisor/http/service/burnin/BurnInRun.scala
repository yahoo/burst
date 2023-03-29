/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http.service.burnin

import org.burstsys.agent.AgentService
import org.burstsys.catalog.CatalogService
import org.burstsys.supervisor.http.service.provider.{BurnInConfig, BurnInEvent, BurnInLogEvent}

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

object BurnInRun {
  def apply(config: BurnInConfig)(
    implicit agent: AgentService, catalog: CatalogService, registerEvent: BurnInEvent => Unit
  ): BurnInRun = {
    BurnInRun(config, System.currentTimeMillis())
  }
}

case class BurnInRun(
                      config: BurnInConfig,
                      startMillis: Long,
                    )(
                      implicit
                      agent: AgentService,
                      catalog: CatalogService,
                      registerEvent: BurnInEvent => Unit
                    ) {

  private val runFlag = new AtomicBoolean()

  private implicit val _shouldContinue: () => Boolean = this.shouldContinue


  def batches: Iterator[BurnInRunBatch] = new Iterator[BurnInRunBatch] {
    private val batchIdx = new AtomicInteger()

    override def hasNext: Boolean = batchIdx.get < config.batches.length && _shouldContinue()

    override def next(): BurnInRunBatch = {
      val index = batchIdx.getAndIncrement
      BurnInRunBatch(index, config.batches(index))
    }
  }

  def maxDurationExceeded: Boolean = config.maxDuration match {
    case Some(duration) => System.currentTimeMillis() > duration.toMillis + startMillis
    case None => false
  }

  def isRunning: Boolean = runFlag.get()

  def shouldContinue(): Boolean = isRunning && !maxDurationExceeded

  def start(): Unit = runFlag.set(true)

  def recordBatchStats(stats: BurnInRunBatchStats): Unit = {

  }

  def finalizeStats(): Unit = {
    registerEvent(BurnInLogEvent("Stats not finalized"))
  }

  def stop(): Unit = runFlag.set(false)

}
