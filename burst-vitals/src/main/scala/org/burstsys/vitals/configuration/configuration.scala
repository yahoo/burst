/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import java.util.concurrent.TimeUnit

import org.burstsys.vitals.logging._
import org.burstsys.vitals.net.VitalsHostName
import org.burstsys.vitals.properties.{VitalsPropertyRegistry, VitalsPropertySpecification}
import org.burstsys.vitals.time.VitalsMs

import scala.concurrent.duration._
import scala.language.postfixOps


package object configuration extends VitalsLogger with VitalsPropertyRegistry {

  def configureForUnitTests(): Unit = {
    vitalsEnableTrekProperty.set(false)
    burstVitalsEnableReporting.set(false)
  }

  // ----------------------------- TOPOLOGY -----------------------------

  val burstCellSupervisorHostProperty: VitalsPropertySpecification[VitalsHostName] = VitalsPropertySpecification[VitalsHostName](
    key = "burst.cell.supervisor.host",
    description = "cell supervisor host",
    default = Some("localhost")
  )

  val burstHomeProperty: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.home",
    description = "burst install location",
    default = Some("/home/burst")
  )

  val burstCellNameProperty: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.cell.name",
    description = "cell environment",
    default = Some("local")
  )

  // ----------------------------- LOGGING -----------------------------

  val burstLog4j2FileProperty: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.log4j2.specification",
    description = "log4j config file",
    default = Some("burst-vitals-log4j2.xml")
  )

  val burstLog4j2NameProperty: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.log4j2.name",
    description = "log4j service name",
    default = Some("vitals")
  )

  // ----------------------------- TREKS -----------------------------

  val vitalsEnableTrekProperty: VitalsPropertySpecification[Boolean] = VitalsPropertySpecification[Boolean](
    key = "burst.vitals.trek.enable",
    description = "enable trek logging",
    default = Some(true)
  )

  // ----------------------------- HTTP METRICS -----------------------------

  val burstVitalsMetricsHttpPeriodMsProperty: VitalsPropertySpecification[VitalsMs] = VitalsPropertySpecification[VitalsMs](
    key = "burst.metrics.http.period.ms",
    description = "metrics http report frequency",
    default = Some((1 minute).toMillis)
  )

  def burstVitalsMetricsHttpPeriodDuration: Duration = Duration(burstVitalsMetricsHttpPeriodMsProperty.getOrThrow, TimeUnit.MILLISECONDS)

  val burstVitalsMetricsHttpUrlProperty: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.metrics.http.url",
    description = "The endpoint to send metrics",
    default = None
  )

  val burstVitalsMetricsHttpProxyHostProperty: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.metrics.http.proxy.host",
    description = "Hostname of the http proxy to use",
    default = None
  )

  val burstVitalsMetricsHttpProxyPortProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "burst.metrics.http.proxy.port",
    description = "Port to connect to the http proxy",
    default = None
  )

  // ----------------------------- SSL -----------------------------

  val burstSslCertPath: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.ssl.cert.path",
    description = "global SSL cert path",
    default = Some("")
  )

  val burstSslKeyPath: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.ssl.key.path",
    description = "global SSL key path",
    default = Some("")
  )

  val burstTrustedCaPath: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.ssl.ca.path",
    description = "global trusted CA cert path",
    default = Some("")
  )

  val burstEnableCompositeTrust: VitalsPropertySpecification[Boolean] = VitalsPropertySpecification[Boolean](
    key = "burst.ssl.composite.trustmanager",
    description = "Trust any certificate issued by an issuer in `burst.ssl.ca.path`, otherwise fall back to default certificate verification",
    default = Some(true)
  )

  // ----------------------------- REPORTERS  -----------------------------

  val burstVitalsEnableReporting: VitalsPropertySpecification[Boolean] = VitalsPropertySpecification[Boolean](
    key = "burst.vitals.reporting.enable",
    description = "enable vitals metrics reporting",
    default = Some(true)
  )

  // ----------------------------- HEALTH CHECK -----------------------------

  val burstVitalsHealthCheckPeriodMsProperty: VitalsPropertySpecification[VitalsMs] = VitalsPropertySpecification[VitalsMs](
    key = "burst.healthcheck.period",
    description = "The period the healthcheck queries components for status",
    default = Some((5 seconds).toMillis)
  )

  def burstVitalsHealthCheckPeriodDuration: Duration = Duration(burstVitalsHealthCheckPeriodMsProperty.getOrThrow, TimeUnit.MILLISECONDS)

  val burstVitalsHealthCheckPortProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "burst.healthcheck.http.port",
    description = "Port to expose the health check on",
    default = Some(37100)
  )

  val burstVitalsHealthCheckPathsProperty: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.healthcheck.http.paths",
    description = "The paths the health check responds to. This is a comma separated list",
    default = Some("/akamai,/status.html")
  )

  val burstVitalsReflectionScanPrefixProperty: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.reflections.scan.prefix",
    description = "An additional package prefix for reflection scans",
    default = None
  )

}
