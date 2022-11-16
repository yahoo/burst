/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.torquemada

import org.apache.logging.log4j.Level
import org.burstsys.agent.AgentService
import org.burstsys.catalog.CatalogService
import org.burstsys.catalog.CatalogService.CatalogRemoteClientConfig
import org.burstsys.torquemada.Parameters.TorcherParameters
import org.burstsys.torquemada.TorcherJob.JobListener
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging._
import org.burstsys.vitals.threading.burstThreadGroupGlobal
import org.burstsys.{agent, catalog}

import java.util
import java.util.concurrent._
import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}
import scala.concurrent.duration._
import scala.language.postfixOps

object Driver  {
  def apply(parameters: TorcherParameters, agentClient: AgentService, catalogClient: CatalogService): Driver = {
    new Driver(parameters, agentClient, catalogClient)
  }

  /**
    * Open the clients to the supervisor catalog and agent
    */
  def openClients(torcherParameters: TorcherParameters): (CatalogService, AgentService) = {
    System.setProperty("java.net.preferIPv4Stack", true.toString)
    catalog.configuration.burstCatalogApiHostProperty.set(torcherParameters.supervisor)
    implicit val catalogClient: CatalogService = CatalogService(CatalogRemoteClientConfig).start

    agent.configuration.burstAgentApiHostProperty.set(torcherParameters.supervisor)
    val agentTimeout = if (torcherParameters.clientTimeout != null) torcherParameters.clientTimeout else Duration(10, TimeUnit.MINUTES)
    agent.configuration.burstAgentApiTimeoutMsProperty.set(agentTimeout.toMillis)
    implicit val agentClient: AgentService = AgentService().start
    (catalogClient, agentClient)
  }
}

class Driver(val torcherParameters: TorcherParameters, val agentClient: AgentService, val catalogClient: CatalogService)
  extends VitalsService {
  override def modality: VitalsServiceModality = agentClient.modality

  /**
    * Clean up any temporary items we may have created for torcher
    */
  def cleanup(): Unit = {
    jobSettings.cleanTemporaryViews()
  }

  def runningTime: Long = if (jobSettings != null) jobSettings.runningTime else 0

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // Threads
  //////////////////////////////////////////////////////////////////////////////////////////////////////
  private[torquemada] val torcherRunning = new AtomicBoolean(false)
  private[torquemada] val torcherStart = new AtomicLong()
  private[torquemada] var torcherThreadPool: ExecutorService = _

  var jobSettings: TorcherJob = _

  override
  def start: this.type = {
    assert(jobSettings == null)
    jobSettings = new TorcherJob(torcherParameters)(agentClient, catalogClient) {
      override def listeners: util.Collection[JobListener] = messageListeners
    }
    jobSettings.notifyListeners(Level.INFO, s"Torcher start request")

    this
  }

  override
  def stop: this.type = {
    jobSettings.notifyListeners(Level.INFO, s"Torcher stop request")
    if (torcherRunning.getAndSet(false)) {
      jobSettings.notifyListeners(Level.INFO, s"Torcher cleanup")
      cleanup()
      jobSettings.notifyListeners(Level.INFO, s"Torcher finished")
    } else {
      jobSettings.notifyListeners(Level.INFO, s"Torcher already stoppped")
    }
    this
  }

  def run: this.type = {
    if (torcherRunning.get()) {
      jobSettings.notifyListeners(Level.ERROR, burstStdMsg(s"Torcher already running"))
      return this
    }
    torcherRunning.set(true)

    try {
      jobSettings.start

      torcherThreadPool = Executors.newFixedThreadPool(jobSettings.concurrency, new ThreadFactory {
        private final val id = new AtomicLong

        override def newThread(r: Runnable): Thread = {
          val t = new Thread(burstThreadGroupGlobal, r, s"${id.incrementAndGet}")
          t setDaemon true
          t
        }
      })

      val maxQueryNumber: Int = if (jobSettings.datasets.isEmpty) 0 else jobSettings.datasets.map(_.queries.size).max

      // check that the load rate and query rate are compatible
      if (jobSettings.loadDelay.toNanos > 0 && jobSettings.queryDelay.toNanos > 0 &&
        jobSettings.loadDelay.toNanos < jobSettings.queryDelay.toNanos * maxQueryNumber) {
        jobSettings.notifyListeners(Level.ERROR, burstStdMsg(s"the load rate of ${jobSettings.loadDelay.toMillis}ms/load is too fast " +
          s"for $maxQueryNumber queries at ${jobSettings.queryDelay.toMillis}ms/query"))
        throw new RuntimeException(s"the load rate of ${jobSettings.loadDelay.toMillis}ms/load is too fast " +
          s"for $maxQueryNumber queries at ${jobSettings.queryDelay.toMillis}ms/query")
      }

      jobSettings.notifyListeners(Level.DEBUG, burstStdMsg(s"Setting up ${jobSettings.concurrency} Torcher executors"))
      val startDelay: Long = if (jobSettings.loadDelay.toNanos > 0) {
        // spread the threads evenly over the load delay
        jobSettings.loadDelay.toMillis / jobSettings.concurrency
      } else {
        // spread the loads over an expected load time of 15s
        (10E3 / jobSettings.concurrency).toLong
      }
      for (i <- 1 to jobSettings.concurrency) {
        torcherThreadPool.submit(org.burstsys.torquemada.Executor(this))
        if (i != jobSettings.concurrency)
          Thread.sleep(startDelay)
      }

      // await completion with some slack unless we don't have a duration then stop after a day
      jobSettings.notifyListeners(Level.DEBUG, burstStdMsg(s"Waiting for ${jobSettings.concurrency} Torcher executors"))
      val wait = if (jobSettings.duration.toNanos == 0) Duration(1, TimeUnit.DAYS)
      else jobSettings.duration.plus(5 minutes)
      torcherThreadPool.shutdown()
      torcherThreadPool.awaitTermination(wait.length, wait.unit)
    } catch safely {
      case t: Throwable =>
        jobSettings.notifyListeners(Level.ERROR, burstStdMsg(s"Torcher aborted with exception", t))

    } finally {
      // if we get to here make sure everything is stopped, even if there was an error
      jobSettings.stop

      jobSettings.notifyListeners(Level.DEBUG, burstStdMsg(s"Executors finished"))
    }


     this
  }

  def isTorcherRunning: Boolean = torcherRunning.get

  def intermediaryStats(): String = if (jobSettings != null) jobSettings.intermediateStats() else ""

  // Driver listeners
  val messageListeners: ConcurrentLinkedQueue[JobListener] = new ConcurrentLinkedQueue[JobListener]()

  def addListener(f: JobListener): this.type = {
    messageListeners.add(f)
    this
  }
}
