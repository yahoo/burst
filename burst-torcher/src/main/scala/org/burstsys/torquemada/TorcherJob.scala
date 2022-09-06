/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.torquemada

import java.util
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

import org.burstsys.agent.AgentService
import org.burstsys.catalog.CatalogService
import org.burstsys.torquemada.Parameters.TorcherParameters
import org.burstsys.torquemada.TorcherJob.JobListener
import org.apache.logging.log4j.Level

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

object TorcherJob {
  type JobListener = (Level, String) => Unit
  type JobStatsListener = String => Unit
}

abstract class TorcherJob (torcherParameters: TorcherParameters )
                          (implicit val agentClient: AgentService, val catalogClient: CatalogService) extends TorcherJobBuilder
{
  // Datasets
  var datasets: Array[Dataset] = Array.empty

  // Timing control
  var queryDelay: Duration = _
  var loadDelay: Duration = _

  var concurrency: Int = _

  var duration:Duration = _

  var schemaName:String = "unity"

  var stopOnFail:Boolean = false

  // not actually in use yet
  var timeout:Duration = _

  //  Job tracking
  val counter = new AtomicInteger(0)
  val startNanos = new AtomicLong(0)
  val startTime = new AtomicLong(0)
  val endNanos = new AtomicLong(0)
  val endTime = new AtomicLong(0)

  val statTable = new DatasetStatisticsTable()

  def start: Long = {
    val d = Try(parseSource(torcherParameters)) match {
      case Success(p) => p
      case Failure(s) =>
        throw s
    }
    this.synchronized {
      datasets = d
    }
    startTime.set(System.currentTimeMillis())
    startNanos.set(System.nanoTime())
    endNanos.set(0)
    endTime.set(0)
    startNanos.get()
  }

  def stop: Long = {
    endNanos.set(System.nanoTime())
    endTime.set(System.currentTimeMillis())
    endNanos.get()
  }

  ///////////////////////////////
  // Job Status Notifications
  ///////////////////////////////
  // status subscribers
  def listeners: util.Collection[JobListener]

  def notifyListeners(level: Level, msg: String): Unit = {
    listeners.synchronized {
      for (l <- listeners.asScala)
        Try(l(level, msg))
    }
  }

  def runningTime: Long = { (if (endNanos.get() > 0) endNanos.get() else if (startNanos.get <= 0) 0 else System.nanoTime()) - startNanos.get()}

  def summaryStats: Array[Double] = {
    statTable.summaryStatistics.allStats
  }

  def intermediateStats(): String  = {
    val nanoRunTime = runningTime
    val durationText = if (duration.toNanos <= 0) s"until ${datasets.length} items complete" else
      if (this.duration.toHours > 1) s"${duration.toSeconds.toDouble/TimeUnit.HOURS.toSeconds(1)} ${TimeUnit.HOURS.name}"
    else s"${duration.toSeconds.toDouble/TimeUnit.MINUTES.toSeconds(1)} ${TimeUnit.MINUTES.name}"

    var (runtime, units) = (TimeUnit.NANOSECONDS.toSeconds(nanoRunTime).toDouble, TimeUnit.SECONDS)
    if (runtime > 3600) {
      runtime = runtime / 3600
      units = TimeUnit.HOURS
    } else if (runtime > 60) {
      runtime = runtime / 60
      units = TimeUnit.MINUTES
    }
    val LPS = statTable.summaryStatistics.firstQueryTimer.getCount.toDouble/TimeUnit.NANOSECONDS.toSeconds(nanoRunTime).toDouble
    val QPS = statTable.summaryStatistics.queryTimer.getCount.toDouble/TimeUnit.NANOSECONDS.toSeconds(nanoRunTime).toDouble
    // val OPS = statTable.summaryStatistics.objectCount.getCount.toDouble/TimeUnit.NANOSECONDS.toSeconds(nanoRunTime).toDouble
    f"""Time: $runtime%2.2f ${units.name} of $durationText Concurreny: $concurrency LPS: $LPS%2.3f QPS: $QPS%2.3f
       |${statTable.summaryStatistics.allStats(true)}""".stripMargin
  }

}

