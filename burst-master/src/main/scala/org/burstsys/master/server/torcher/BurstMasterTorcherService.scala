/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.master.server.torcher

import java.io.StringReader
import java.util.concurrent.ConcurrentHashMap

import org.burstsys.agent.AgentService
import org.burstsys.catalog.CatalogService
import org.burstsys.dash.provider.torcher._
import org.burstsys.tesla.thread.request.{TeslaRequestFuture, teslaRequestExecutor}
import org.burstsys.torquemada.Driver
import org.burstsys.torquemada.Parameters.TorcherParameters
import org.burstsys.vitals.logging._
import org.apache.logging.log4j.Level

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success}

/**
 * Glue torcher to the dash interface
 */
trait BurstMasterTorcherService extends BurstDashTorcherService

object BurstMasterTorcherService {
  def apply(agent: AgentService, catalog: CatalogService): BurstMasterTorcherService = BurstMasterTorcherProvider(agent, catalog)
}

private[torcher] final case
class BurstMasterTorcherProvider(agent: AgentService, catalog: CatalogService) extends BurstMasterTorcherService {

  protected var torcherDriver: Driver = _
  protected var source: Option[String] = None

  private final val bufferSize = 50
  private final val _messages = new ArrayBuffer[TorcherLogMessage](bufferSize)
  private final val listeners = ConcurrentHashMap.newKeySet[TorcherEventListener]().asScala

  def isTorcherRunning: Boolean = {
    torcherDriver != null && torcherDriver.isTorcherRunning
  }

  override def config: Option[String] = source

  override def messages: Array[TorcherLogMessage] = _messages.toArray

  override def getDatasetStats: Array[TorcherDatasetStatistics] = {
    if (torcherDriver != null && torcherDriver.jobSettings != null)
      torcherDriver.jobSettings.statTable.datasetStatistics.asScala.map(row => {
        val (key, dataset) = row
        TorcherDatasetStatistics(
          key.domainId, key.viewId, dataset.info,
          dataset.coldLoadAt.get,
          dataset.statistics.byteCount.getCount,
          dataset.statistics.firstQueryTimer.getSnapshot.get99thPercentile,
          dataset.statistics.coldLoadTimer.getSnapshot.get99thPercentile,
          dataset.statistics.coldLoadTimer.getCount,
          dataset.statistics.itemCount.getCount,
          dataset.statistics.limitCount.getCount,
          dataset.statistics.overflowCount.getCount,
          dataset.failureGid.get, dataset.successGid.get
        )
      }).toArray
    else
      Array.empty
  }

  override def status: TorcherStatus = {
    if (torcherDriver != null && torcherDriver.jobSettings != null) {
      val settings = torcherDriver.jobSettings
      val duration = if (torcherDriver.jobSettings.duration == null)
        "once through"
      else torcherDriver.jobSettings.duration.toString
      val elapsedTime = if (settings.endNanos.get < settings.startNanos.get)
        System.nanoTime - settings.startNanos.get
      else settings.endNanos.get - settings.startNanos.get

      val datasetCount = settings.datasets.length
      TorcherStatus(
        running = torcherDriver.isTorcherRunning,
        counter = settings.counter.get,
        schema = settings.schemaName,
        concurrency = settings.concurrency,
        startTimeMs = runStartTime,
        endTimeMs = runFinishTime,
        elapsedTime = elapsedTime,
        duration = duration,
        datasetCount = datasetCount,
        currentDatasetIndex = if (datasetCount != 0) settings.counter.get % datasetCount else -1,
        summary = if (settings.statTable.summaryStatistics != null) Some(torcherSummary) else None
      )
    } else TorcherStatus(running = false, -1, "", -1, -1, -1, -1, "", -1, -1, None)
  }

  private def torcherSummary: TorcherSummaryStats = {
    val stats = torcherDriver.jobSettings.statTable
    TorcherSummaryStats(
      stats.summaryStatistics.byteCount.getCount,
      stats.summaryStatistics.itemCount.getCount,
      stats.summaryStatistics.objectCount.getCount,
      stats.summaryStatistics.coldLoadTimer.getFiveMinuteRate,
      stats.summaryStatistics.coldLoadTimer.getFifteenMinuteRate,
      stats.summaryStatistics.coldLoadTimer.getMeanRate,
      stats.summaryStatistics.coldLoadTimer.getSnapshot.get99thPercentile,
      stats.summaryStatistics.coldLoadTimer.getSnapshot.getMax,
      stats.summaryStatistics.firstQueryTimer.getFiveMinuteRate,
      stats.summaryStatistics.firstQueryTimer.getFifteenMinuteRate,
      stats.summaryStatistics.firstQueryTimer.getMeanRate,
      stats.summaryStatistics.firstQueryTimer.getSnapshot.get99thPercentile,
      stats.summaryStatistics.firstQueryTimer.getSnapshot.getMax,
      stats.summaryStatistics.firstQueryFailures.getCount,
      stats.summaryStatistics.firstQueryNoDataFailures.getCount,
      stats.summaryStatistics.queryFailures.getCount,
      stats.summaryStatistics.limitCount.getCount,
      stats.summaryStatistics.overflowCount.getCount
    )
  }

  override def runStartTime: Long = torcherDriver.jobSettings.startTime.get()

  override def runFinishTime: Long = torcherDriver.jobSettings.endTime.get()

  override def startTorcher(source: String): Boolean = {
    if (torcherDriver != null && torcherDriver.isTorcherRunning) {
      broadcastMessage(Level.WARN, s"Torcher is already running")
      // do nothing
      return false
    }

    broadcastMessage(Level.INFO, s"Received request to start torcher")

    val torcherParameters = parseParameters(source)
    if (torcherParameters == null) {
      return false
    }
    this.source = Some(source)

    torcherDriver = new Driver(torcherParameters, agent, catalog)
    torcherDriver.addListener { (l, m) => broadcastMessage(l, m) } // send status to application

    broadcastMessage(Level.INFO, s"Torcher monitor starting")
    val driverFuture = TeslaRequestFuture {
      torcherDriver.start
      torcherDriver.run
    }
    listeners.foreach(_.torcherStarted(source))

    driverFuture andThen {
      case _ =>
        torcherDriver.stop
        listeners.foreach(_.torcherStopped())
    } onComplete  {
      case Success(_) =>
        broadcastMessage(Level.INFO, s"Torcher finished successfully")
        log info s"Torcher finished successfully: $status"
      case Failure(ex) =>
        broadcastMessage(Level.ERROR, burstStdMsg(s"Torcher thread failed", ex))
        log info s"Torcher thread failed: $status"
    }

    true
  }

  def parseParameters(source: String): TorcherParameters = {
    if (source.isEmpty) {
      broadcastMessage(Level.ERROR, s"could not parse torcher config")
      return null
    }

    TorcherParameters(duration = null, parallelism = 0, source = new StringReader(source))
  }

  override def stopTorcher(): Boolean = {
    if (torcherDriver == null || !torcherDriver.isTorcherRunning)
      return false

    torcherDriver.stop
    listeners.foreach(_.torcherStopped())

    true
  }

  override def talksTo(listener: TorcherEventListener): this.type = {
    listeners += listener
    this
  }

  def broadcastMessage(level: Level, text: String, record: Boolean = true): Unit = {
    val message = TorcherLogMessage(level, text)
    if (record) {
      recordStatusText(message)
    }
    listeners.foreach(_.torcherMessage(message))
  }

  def recordStatusText(message: TorcherLogMessage): Unit = {
    synchronized {
      _messages += message
      if (_messages.size > bufferSize)
        _messages.remove(0)
    }
    message.level match {
      case Level.FATAL => log fatal message.message
      case Level.ERROR => log error message.message
      case Level.WARN => log warn message.message
      case Level.INFO => log info message.message
      case Level.DEBUG => log debug message.message
    }
  }
}
