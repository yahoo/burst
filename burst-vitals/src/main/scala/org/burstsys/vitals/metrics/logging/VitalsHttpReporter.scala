/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.metrics.logging

import java.net.Proxy.Type
import java.net.{HttpURLConnection, InetSocketAddress, URL}
import java.util
import java.util.concurrent.TimeUnit

import com.codahale.metrics._
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.burstsys.vitals.configuration.{burstCellNameProperty, burstVitalsMetricsHttpUrlProperty, burstVitalsMetricsHttpProxyPortProperty, burstVitalsMetricsHttpProxyHostProperty}
import org.burstsys.vitals.metrics._
import org.burstsys.vitals.net
import org.burstsys.vitals.errors.safely

import scala.jdk.CollectionConverters._
import scala.collection.mutable
import scala.concurrent.duration.Duration

trait VitalsHttpReporter extends ScheduledReporter

object VitalsHttpReporter {
  def apply(registry: MetricRegistry, period: Duration): VitalsHttpReporter = {
    val reporter = VitalsHttpReporterContext(registry)
    reporter.start(period.toMillis, TimeUnit.MILLISECONDS)
    reporter
  }
}

private final case
class VitalsHttpReporterContext(
                                 registry: MetricRegistry,
                                 rateUnit: TimeUnit = TimeUnit.SECONDS,
                                 durationUnit: TimeUnit = TimeUnit.MILLISECONDS
                               )
  extends ScheduledReporter(registry, "burst-http-reporter", MetricFilter.ALL, rateUnit, durationUnit) with VitalsHttpReporter {

  val mapper: ObjectMapper = {
    val mapper = new ObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper
  }

  lazy val proxy: java.net.Proxy = {
    try {
      if (burstVitalsMetricsHttpProxyHostProperty.asOption.isDefined) {
        val host = burstVitalsMetricsHttpProxyHostProperty.get
        val port = burstVitalsMetricsHttpProxyPortProperty.get
        new java.net.Proxy(Type.HTTP, new InetSocketAddress(host, port))
      } else {
        java.net.Proxy.NO_PROXY
      }
    } catch safely {
      case t =>
        log warn(s"failed to configure proxy", t)
        java.net.Proxy.NO_PROXY
    }

  }

  override
  def report(
              gauges: util.SortedMap[String, Gauge[_]],
              counters: util.SortedMap[String, Counter],
              histograms: util.SortedMap[String, Histogram],
              meters: util.SortedMap[String, Meter],
              timers: util.SortedMap[String, Timer]
            ): Unit = {
    var payload = mutable.Map[String, Any]()
    payload += "application" -> "burst"
    payload += "dimensions" -> Map("host" -> net.getPublicHostName, "cell" -> burstCellNameProperty.get)
    payload += "timestamp" -> System.currentTimeMillis / 1000
    payload += "metrics" -> metrics(gauges, counters, histograms, meters, timers)

    val json = mapper.writeValueAsBytes(payload)
    val url = new URL(burstVitalsMetricsHttpUrlProperty.get)
    val connection = url.openConnection(proxy).asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("POST")
    connection.setRequestProperty("Content-Type", "application/json")
    connection.setRequestProperty("Content-Length", json.length.toString)
    connection.setDoOutput(true)
    connection.getOutputStream.write(json)

    connection.getResponseCode match {
      case 200 =>
        log.info("successfully reported metrics")
      case _ =>
        connection.getErrorStream
        log.warn(s"Failed to report metrics: ${io.Source.fromInputStream(connection.getErrorStream).mkString}")
    }
  }

  def metrics(
               gauges: util.SortedMap[String, Gauge[_]],
               counters: util.SortedMap[String, Counter],
               histograms: util.SortedMap[String, Histogram],
               meters: util.SortedMap[String, Meter],
               timers: util.SortedMap[String, Timer]
             ): Map[String, Number] = {
    var metrics = mutable.Map[String, Number]()

    counters.asScala.foreach({ kv => metrics += kv._1 -> kv._2.getCount })

    gauges.asScala.foreach({ kv =>
      kv._2.getValue match {
        case i: Int => metrics += kv._1 -> i
        case l: Long => metrics += kv._1 -> l
        case d: Double => metrics += kv._1 -> d
        case f: Float => metrics += kv._1 -> f
        case _ =>
      }
    })

    meters.asScala.foreach({ kv =>
      metrics += s"${kv._1}.count" -> kv._2.getCount
      metrics += s"${kv._1}.mean_rate" -> convertRate(kv._2.getMeanRate)
      metrics += s"${kv._1}.m1" -> convertRate(kv._2.getOneMinuteRate)
      metrics += s"${kv._1}.m5" -> convertRate(kv._2.getFiveMinuteRate)
      metrics += s"${kv._1}.m15" -> convertRate(kv._2.getFifteenMinuteRate)
    })

    histograms.asScala.foreach({ kv =>
      val snapshot = kv._2.getSnapshot
      metrics += s"${kv._1}.count" -> kv._2.getCount
      metrics += s"${kv._1}.min" -> snapshot.getMin
      metrics += s"${kv._1}.max" -> snapshot.getMax
      metrics += s"${kv._1}.mean" -> snapshot.getMean
      metrics += s"${kv._1}.stddev" -> snapshot.getStdDev
      metrics += s"${kv._1}.median" -> snapshot.getMedian
      metrics += s"${kv._1}.p75" -> snapshot.get75thPercentile
      metrics += s"${kv._1}.p95" -> snapshot.get95thPercentile
      metrics += s"${kv._1}.p98" -> snapshot.get98thPercentile
      metrics += s"${kv._1}.p99" -> snapshot.get99thPercentile
      metrics += s"${kv._1}.p999" -> snapshot.get999thPercentile
    })

    timers.asScala.foreach({ kv =>
      val snapshot = kv._2.getSnapshot
      metrics += s"${kv._1}.count" -> kv._2.getCount
      metrics += s"${kv._1}.min" -> convertDuration(snapshot.getMin.toDouble)
      metrics += s"${kv._1}.max" -> convertDuration(snapshot.getMax.toDouble)
      metrics += s"${kv._1}.mean" -> convertDuration(snapshot.getMean)
      metrics += s"${kv._1}.stddev" -> convertDuration(snapshot.getStdDev)
      metrics += s"${kv._1}.median" -> convertDuration(snapshot.getMedian)
      metrics += s"${kv._1}.p75" -> convertDuration(snapshot.get75thPercentile)
      metrics += s"${kv._1}.p95" -> convertDuration(snapshot.get95thPercentile)
      metrics += s"${kv._1}.p98" -> convertDuration(snapshot.get98thPercentile)
      metrics += s"${kv._1}.p99" -> convertDuration(snapshot.get99thPercentile)
      metrics += s"${kv._1}.p999" -> convertDuration(snapshot.get999thPercentile)
      metrics += s"${kv._1}.mean_rate" -> convertRate(kv._2.getMeanRate)
      metrics += s"${kv._1}.m1" -> convertRate(kv._2.getOneMinuteRate)
      metrics += s"${kv._1}.m5" -> convertRate(kv._2.getFiveMinuteRate)
      metrics += s"${kv._1}.m15" -> convertRate(kv._2.getFifteenMinuteRate)
    })

    metrics.toMap
  }
}
