/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.configuration

import org.burstsys.catalog.configuration
import org.burstsys.vitals.configuration.SslGlobalProperties
import org.burstsys.vitals.net.VitalsHostAddress
import org.burstsys.vitals.net.VitalsHostPort

import scala.concurrent.duration.Duration

trait CatalogApiProperties extends Any with SslGlobalProperties with CatalogSqlProperties {

  final def apiHost: VitalsHostAddress = configuration.burstCatalogApiHostProperty.get

  final def apiPort: VitalsHostPort = configuration.burstCatalogApiPortProperty.get

  final def enableSsl: Boolean = configuration.burstCatalogApiSslEnableProperty.get

  final def maxConnectionIdleTime: Duration = burstCatalogServerConnectionIdleDuration

  final def maxConnectionLifeTime: Duration = burstCatalogServerConnectionLifeDuration

  final def requestTimeout: Duration = burstCatalogApiTimeoutDuration

}
