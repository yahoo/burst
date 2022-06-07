/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging._
import org.burstsys.vitals.strings._

import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps

package object reporter extends VitalsLogger {

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CONSTANTS
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final val maxCharsPerPublish = 5000

  /**
   * the default wait before reporter starts logging
   */
  final val defaultWaitPeriod: Duration = 1 minute

  /**
   * the default period after which absent new samples, readings become ''null''
   */
  final val defaultStalePeriod: Duration = 1 hour

  /**
   * the default duration in between reports
   */
  final val defaultReportPeriod: Duration = 5 minutes

  /**
   * the default duration in between samples
   */
  final val defaultSamplePeriod: Duration = 10 seconds

  final val SecToNs = 1e9

  final val MsToSec = 1e6

  final val NsToMs = 1e3

  final val NsToSec = 1e9

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[reporter]
  val _reporters: mutable.Set[VitalsReporter] = ConcurrentHashMap.newKeySet[VitalsReporter].asScala

  private
  var _stalePeriod: Duration = _

  private
  var _waitPeriod: Duration = _

  private
  var _reportPeriod: Duration = _

  private
  var _samplePeriod: Duration = _

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final def enabled: Boolean = configuration.burstVitalsEnableReporting.getOrThrow

  final def enabled_=(value: Boolean): Unit = configuration.burstVitalsEnableReporting.set(value)

  final def register(reporter: VitalsReporter): Unit = {
    _reporters += reporter
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

    reflection.getPackageSubTypesOf(classOf[VitalsReporterSource]).asScala foreach { source =>
      log info(s"Loading reporters from ${source.getClass.getName.stripSuffix(".package$")}")
      source.reporters.foreach(register)
    }

    _reporterFunction.startIfNotAlreadyStarted
    _samplerFunction.startIfNotAlreadyStarted
  }

  /**
   * the wait before reporter starts logging
   */
  final def waitPeriod: Duration = if (_waitPeriod != null) _waitPeriod else defaultWaitPeriod

  /**
   * the duration in between reports
   */
  final def reportPeriod: Duration = if (_reportPeriod != null) _reportPeriod else defaultReportPeriod

  /**
   * the duration in between samples (this is used to scale rates in some instances)
   */
  final def samplePeriod: Duration = if (_samplePeriod != null) _samplePeriod else defaultSamplePeriod

  /**
   * the period after which absent new samples, readings become ''null''
   */
  final def staleAfterPeriod: Duration = if (_stalePeriod != null) _stalePeriod else defaultStalePeriod

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // sampling
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private lazy val _samplerFunction = new VitalsBackgroundFunction(
    name = "vitals-reporter-sampler", startWait = _samplePeriod, period = _samplePeriod, {
      doSamples()
    }).start

  private def doSamples(): Unit = {
    if (!enabled) return
    _reporters foreach {
      reporter =>
        try {
          reporter.sample(_samplePeriod.toMillis)
        } catch safely {
          case t: Throwable => log error burstStdMsg(s"sample() function for reporter ${reporter.dName} failed", t)
        }
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // reporting
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private lazy val _reporterFunction = new VitalsBackgroundFunction(
    name = "vitals-reporter", startWait = waitPeriod, period = _reportPeriod, {
      doReports()
    }).start

  private def doReports(): Unit = {
    if (!enabled || _reporters.isEmpty) return
    val reporters = _reporters.toList.sortBy(_.dName)
    val buffer = new StringBuilder
    var part = 0
    reporters.foreach {
      reporter =>
        val name = reporter.dName.toUpperCase
        val report = if (!reporter.nullData) {
          val report = try {
            reporter.report
          } catch safely {
            case t: Throwable =>
              log error burstStdMsg(s"error in report '$name'", t)
          }
          s"${name.asBanner} \n $report"
        } else s"${s"$name [NO_DATA]".asBanner} \n"
        buffer ++= report
        if (buffer.size > maxCharsPerPublish) {
          publishReport(part, buffer.toString)
          part += 1
          buffer.clear
        }
    }
    if (buffer.nonEmpty) publishReport(part, buffer.toString)
  }

  private def publishReport(part: Int, string: String): Unit = {
    val parms = s"(samplePeriod=${_samplePeriod}, reportPeriod=${_reportPeriod})"
    log info
      s"""|
          |${s"VITALS_REPORT PART_${part}_BEGIN $parms".asBanner(sym = "#", width = 80)}
          |$string${s"VITALS_REPORT PART_${part}_END".asBanner(sym = "#", width = 80)}""".stripMargin
  }
}
