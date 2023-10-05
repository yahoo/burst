/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.api.{GlobalOpenTelemetry, OpenTelemetry}
import io.opentelemetry.context.propagation.{ContextPropagators, TextMapPropagator}
import io.opentelemetry.exporter.logging.LoggingMetricExporter
import io.opentelemetry.exporter.logging.otlp.{OtlpJsonLoggingMetricExporter, OtlpJsonLoggingSpanExporter}
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.`export`.{MetricReader, PeriodicMetricReader}
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.SimpleSpanProcessor
import org.burstsys.vitals.logging._
import org.burstsys.vitals.errors._

import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.language.postfixOps

package object reporter extends VitalsLogger {

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CONSTANTS
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * the default wait before reporter starts logging
   */
  private val defaultWaitPeriod: Duration = 1 minute

  /**
   * the default period after which absent new samples, readings become ''null''
   */
  private val defaultStalePeriod: Duration = 1 hour

  /**
   * the default duration in between reports
   */
  private val defaultReportPeriod: Duration = 5 minutes

  /**
   * the default duration in between samples
   */
  private val defaultSamplePeriod: Duration = 10 seconds

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[reporter] val _reporters = ConcurrentHashMap.newKeySet[VitalsReporter]

  private var _stalePeriod: Duration = _

  private var _waitPeriod: Duration = _

  private var _reportPeriod: Duration = _

  private var _samplePeriod: Duration = _

  private val _resource: Resource = Resource.getDefault

  private lazy val trekEnable: Boolean = configuration.vitalsEnableTrekProperty.get

  private lazy val _sdkTracerProvider: SdkTracerProvider = {
    val b = SdkTracerProvider.builder()
      .setResource(_resource)
      .addSpanProcessor(SimpleSpanProcessor.create(OtlpJsonLoggingSpanExporter.create()))
      //.addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
      .addSpanProcessor(TrekSpanProcessor.create(TrekLoggingSpanExporter.create()))
    b.build()
  }

  private val _metricReaders: mutable.Set[MetricReader] = mutable.Set()

  lazy private val _sdkMeterProvider: SdkMeterProvider = {
    _metricReaders.add(PeriodicMetricReader.builder(OtlpJsonLoggingMetricExporter.create()).build())

    val mpb = SdkMeterProvider.builder().setResource(_resource)

    _metricReaders.foreach(r => mpb.registerMetricReader(r))
    mpb.build()
  }

  def flushMetrics(): Unit = {
    _metricReaders.foreach(r => r.forceFlush())
  }

  // Access this if we want to start OT regardless of what is done externally
  lazy val startTelemetry: OpenTelemetry = {
    val ot = OpenTelemetrySdk.builder()
      .setTracerProvider(_sdkTracerProvider)
      .setMeterProvider(_sdkMeterProvider)
      .setPropagators(ContextPropagators.create(
        TextMapPropagator.composite(
          W3CTraceContextPropagator.getInstance(),
          W3CBaggagePropagator.getInstance()
        )
      ))
      .build()
    log info s"Starting Open Telemetry $ot"
    try {
      GlobalOpenTelemetry.set(ot)
    } catch safely {
      case _: Throwable =>
        // someone (probably the java agent) set open telemetry before us
        log warn s"OpenTelemetry setup externally"
    }
    GlobalOpenTelemetry.get()
  }


  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final def enabled: Boolean = configuration.burstVitalsEnableReporting.get

  final def enabled_=(value: Boolean): Unit = configuration.burstVitalsEnableReporting.set(value)

  final def register(reporter: VitalsReporter): Unit = {
    _reporters add reporter
  }

  final def startReporterSystem(
                                 waitPeriod: Duration = defaultWaitPeriod,
                                 reportPeriod: Duration = defaultReportPeriod,
                                 samplePeriod: Duration = defaultSamplePeriod,
                                 stalePeriod: Duration = defaultStalePeriod
                               ): Unit = {


    if (!enabled) {
      log info burstStdMsg(s"VITALS REPORTING DISABLED")
      return
    }
    log info burstStdMsg(
      s"START_REPORTER reportPeriod=$reportPeriod, samplePeriod=$samplePeriod, stalePeriod=$stalePeriod"
    )
    _waitPeriod = waitPeriod
    _reportPeriod = reportPeriod
    _samplePeriod = samplePeriod
    _stalePeriod = stalePeriod

    reflection.getPackageSubTypesOf(classOf[VitalsReporterSource]).asScala.foreach {
      source =>
        log info s"Loading reporters from ${source.getClass.getName.stripSuffix(".package$")}"
        source.reporters.foreach(register)
    }
  }
}
