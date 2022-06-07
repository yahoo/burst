/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.configuration

import org.burstsys.catalog.configuration
import org.burstsys.vitals.configuration.{SslGlobalProperties, burstCellNameProperty, burstSiteNameProperty}
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostPort}

import scala.concurrent.duration.Duration

trait CatalogApiProperties extends Any with SslGlobalProperties with CatalogSqlProperties {

  /**
    * Site name this catalog resides in
    * @return
    */
  final def siteName: String = burstSiteNameProperty.getOrThrow

  /**
    * Cell name this catalog resides in
    * @return
    */
  final def cellName: String = burstCellNameProperty.getOrThrow

  final def apiHost: VitalsHostAddress = configuration.burstCatalogApiHostProperty.getOrThrow

  final def apiPort: VitalsHostPort = configuration.burstCatalogApiPortProperty.getOrThrow

  final def enableSsl: Boolean = configuration.burstCatalogApiSslEnableProperty.getOrThrow

  final def maxConnectionIdleTime: Duration = burstCatalogServerConnectionIdleDuration

  final def maxConnectionLifeTime: Duration = burstCatalogServerConnectionLifeDuration

  final def requestTimeout: Duration = burstCatalogApiTimeoutDuration

}
