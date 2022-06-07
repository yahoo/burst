/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.service.profiler

import java.util.concurrent.atomic.AtomicBoolean

import org.burstsys.agent.AgentService
import org.burstsys.catalog.CatalogService
import org.burstsys.dash.provider.profiler._
import org.burstsys.fabric.metadata.model.over.FabricOver
import org.burstsys.tesla.thread.request.teslaRequestExecutor
import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.errors._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

case class BurstRestProfiler(agent: AgentService, catalog: CatalogService) extends BurstDashProfilerService {
  private var profilerStartedAt: Long = System.currentTimeMillis()
  private var profilerWorkers: Array[BurstRestProfilerWorker] = Array.empty
  private var _run: ProfilerRun = _

  private val listeners = new ArrayBuffer[BurstDashProfilerListener]
  private val profilerRunning = new AtomicBoolean(false)
  private val profilerEvents = new ArrayBuffer[ProfilerEvent]

  private val progressMarker = new VitalsBackgroundFunction(name = "profiler-progress", 5 seconds, 5 seconds, {
    if (profilerRunning.get())
      addProfilerEvent("progress")
  })
  progressMarker.start

  def running: Boolean = profilerRunning.get

  def run: ProfilerRun = _run

  override def startProfiler(source: String, domain: Long, view: Long, timezone: String, concurrency: Int, executions: Int, reloadEvery: Int): ProfilerRunResponse = {
    if (!profilerRunning.compareAndSet(false, true)) {
      addProfilerEvent("profiler already running", isError = true)
      return ProfilerRunResponse(success = false)
    }
    try {
      log info
        s"runProfiler(source=$source, domain=$domain, view=$view, timezone=$timezone, concurrency=$concurrency, executions=$executions, reloadEvery=$reloadEvery"
      profilerStartedAt = System.currentTimeMillis
      _run = ProfilerRun(source, executions, concurrency, reloadEvery, fetchDatasource(domain, view, timezone))
      profilerRunning.set(true)
      profilerEvents.clear()

      addProfilerEvent(s"Start:${_run.over} \n  ${_run.desiredQueryCount} scan(s) @ concurrency ${_run.concurrentQueries}")
      profilerWorkers = (0 until concurrency).indices.map(i => BurstRestProfilerWorker(i, this)).toArray
      Future.traverse(profilerWorkers.toList)(_.start) andThen { case _ =>
        profilerRunning.set(false)
      } onComplete {
        case Success(_) =>
          addProfilerEvent(s"Stop: \n ${_run.failuresTally.get()} failures(s)")
        case Failure(t) =>
          addProfilerEvent(s"Failure: ${t.getMessage} \n ${_run.failuresTally.get()} failures(s)", isError = true)
      }
      listeners.foreach(_.profilerStarted(config))

      ProfilerRunResponse(
        concurrency = _run.concurrentQueries,
        executions = _run.desiredQueryCount,
        loads = _run.queriesPerLoad
      )
    } catch safely {
      case _: Throwable =>
        ProfilerRunResponse(success = false)
    }

  }

  override def stopProfiler: ProfilerStopResponse = {
    if (profilerRunning.compareAndSet(true, false)) {
      addProfilerEvent(s"Stopping profiler")
      listeners.foreach(_.profilerStopped())
      ProfilerStopResponse(running = true)
    } else
      ProfilerStopResponse()

  }

  override def config: ProfilerConfig = if (_run == null) ProfilerConfig() else ProfilerConfig(
    _run.source,
    _run.over.domainKey, _run.over.viewKey, _run.over.locale.timezone,
    profilerRunning.get,
    _run.concurrentQueries, _run.desiredQueryCount, _run.queriesPerLoad
  )

  override def getEvents: Array[ProfilerEvent] = profilerEvents.toArray

  override def talksTo(listener: BurstDashProfilerListener): Unit = listeners += listener

  def addProfilerEvent(message: String, isError: Boolean = false): Unit = {
    synchronized {
      val event = ProfilerEvent(
        profilerRunning.get, isError,
        elapsed = System.currentTimeMillis - profilerStartedAt,
        success = _run.successesTally.get,
        failure = _run.failuresTally.get,
        scanCount = _run.scanCountTally.sum,
        scanTime = _run.scanTimeTally.sum,
        loadTime = _run.loadTimeTally.sum,
        loadCount = _run.loadCountTally.get,
        loadSize = _run.loadSizeTally.sum,
        message = message
      )
      profilerEvents += event
      listeners.foreach(_.profilerEvent(event))
    }
  }

  private def fetchDatasource(domainKey: Long, viewKey: Long, timezone: String): FabricOver = {
    catalog.findViewByPk(viewKey) match {
      case Success(view) =>
        if (view.domainFk != domainKey)
          throw VitalsException(s"view.domainFk=${view.domainFk} does not match domainKey=$domainKey")
        FabricOver(domainKey, viewKey)
      case Failure(t) => throw VitalsException(s"view for viewKey=$viewKey not found", t)
    }
  }

}
