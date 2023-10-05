/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http.service.burnin

import com.twitter.logging
import org.burstsys.agent.AgentService
import org.burstsys.catalog.CatalogService
import org.burstsys.supervisor.http.service.burnin.BurnInRunBatch.BurnInLabel
import org.burstsys.supervisor.http.service.provider._
import org.burstsys.supervisor.trek.StartBurnInTrek
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.vitals
import org.burstsys.vitals.errors.{VitalsException, safely}
import org.jctools.queues.MpmcArrayQueue

import java.util.logging.Level

case class BurstWaveBurnInService(agent: AgentService, catalog: CatalogService)
  extends BurstWaveSupervisorBurnInService {

  private var _listeners = Array.empty[BurstWaveBurnInListener]

  private var _config: BurnInConfig = _

  //private val _events: ConcurrentLinkedQueue[BurnInEvent] = new ConcurrentLinkedQueue[BurnInEvent]()
  private val _events = new MpmcArrayQueue[BurnInEvent](1500)

  private var _run: BurnInRun = _


  // provide service-level implicits
  private implicit val _agent: AgentService = agent

  private implicit val _catalog: CatalogService = catalog

  private implicit val _registerEvent: BurnInEvent => Unit = this.registerEvent


  override def isRunning: Boolean = _run != null && _run.isRunning

  override def startBurnIn(config: BurnInConfig): Boolean = {
    StartBurnInTrek.begin() { stage =>
      if (_run != null && _run.isRunning) {
        registerLogEvent("Burn In already running!", Level.SEVERE)
        StartBurnInTrek.fail(stage, VitalsException("Burn In already running!"))
        return false
      }

      val (isValid, errors) = config.validate()
      if (!isValid) {
        registerLogEvent(s"Invalid config detected: ${errors.mkString("- ", "\n- ", "")}")
        StartBurnInTrek.fail(stage, VitalsException("Invalid config detected"))
        return false
      }
      TeslaRequestFuture {
        val run = BurnInRun(config)

        try {
          _events.clear()
          _config = config
          _run = run

          run.start()
          registerLogEvent("Starting burn-in")
          eachListener(l => l.burnInStarted(config))

          cleanupBurnInDatasets()

          for (batch <- run.batches) {
            batch.ensureDatasets()
            val stats = batch.run()
            run.recordBatchStats(stats)
            //      registerEvent(BurnInStatsEvent(stats))
            batch.cleanUp()
          }
          run.finalizeStats()
          run.stop()
          stopBurnIn()
        } catch safely {
          case t: Throwable =>
            registerLogEvent(s"Unhandled exception ${t.getMessage}@${vitals.errors.printStack(t)}", Level.SEVERE)
            run.stop()
            stopBurnIn()
        }
      }
      StartBurnInTrek.end(stage)
      true
    }
  }

  private def cleanupBurnInDatasets(): Unit = {
    registerLogEvent("Checking for existing burn-in datasets")
    val viewsFound = catalog.searchViewsByLabel(BurnInLabel).getOrElse(throw VitalsException("Failed to search catalog for extant burn-in datasets"))
    val domainsFound = catalog.searchDomainsByLabel(BurnInLabel).getOrElse(throw VitalsException("Failed to search catalog for extant burn-in datasets"))
    if (viewsFound.nonEmpty || domainsFound.nonEmpty) {
      val count = viewsFound.length + domainsFound.length
      for (view <- viewsFound) {
        catalog.deleteView(view.pk)
      }
      for (domain <- domainsFound) {
        catalog.deleteDomain(domain.pk)
      }
      registerLogEvent(s"Cleaned up $count orphaned datasets")
    } else {
      registerLogEvent("Nothing to clean up")
    }
  }

  override def stopBurnIn(): Boolean = {
    registerLogEvent("Shutting down burn-in")
    eachListener(l => l.burnInStopped())
    _run.stop()
    true
  }

  private def registerLogEvent(message: String, level: Level = Level.INFO): Unit = {
    level match {
      case _@Level.FINEST => log trace message
      case _@Level.FINER | _@Level.FINE => log debug message
      case _@Level.INFO => log info message
      case _@Level.WARNING => log warn message
      case _@Level.SEVERE => log error message
      case _ => log info s"$level: $message"
    }
    registerEvent(BurnInLogEvent(message, level))
  }

  override def getEvents: Array[BurnInEvent] = {
    _events.toArray(Array.empty[BurnInEvent])
  }

  override def getConfig: BurnInConfig = _config

  override def talksTo(listeners: BurstWaveBurnInListener*): Unit = {
    this._listeners ++= listeners
  }

  private def registerEvent(event: BurnInEvent): Unit = {
    while (!_events.offer(event)) {
      _events.poll()
    }
    eachListener(l => l.burnInEvent(event))
  }

  private def eachListener(work: BurstWaveBurnInListener => Unit): Unit = {
    _listeners foreach { l =>
      try {
        work(l)
      } catch safely {
        case _ =>
          log debug s"Burn-in listener callback failed $l"
      }
    }
  }
}
