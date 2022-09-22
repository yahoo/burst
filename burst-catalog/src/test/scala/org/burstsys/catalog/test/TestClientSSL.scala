/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.test

import org.burstsys.{catalog, vitals}
import org.burstsys.catalog.CatalogService
import org.burstsys.catalog.CatalogService.CatalogRemoteClientConfig
import org.burstsys.catalog.CatalogService.CatalogUnitTestClientConfig
import org.burstsys.catalog.CatalogSqlConsumer
import org.burstsys.catalog.persist.CatalogSqlProvider
import org.burstsys.relate.dialect
import org.burstsys.relate.dialect.RelateDialect
import org.burstsys.relate.dialect.RelateMySqlDialect
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.VitalsService.VitalsStandardClient
import org.burstsys.vitals.logging._
import org.burstsys.vitals.metrics.VitalsMetricsRegistry

import scala.util.{Failure, Success}

object TestClientSSL {
  def main(args: Array[String]): Unit = {
    VitalsLog.configureLogging("test-api-client", consoleOnly = true)

    val certsPath = "/tmp"
    catalog.configuration.burstCatalogApiHostProperty.set("localhost")
    catalog.configuration.burstCatalogApiSslEnableProperty.set(true)
    vitals.configuration.burstSslKeyPath.set(s"$certsPath/key.pem")
    vitals.configuration.burstSslCertPath.set(s"$certsPath/cert.pem")
    vitals.configuration.burstTrustedCaPath.set(s"$certsPath/rootCA.pem")
    val catalogClient: CatalogService = CatalogService(CatalogRemoteClientConfig)
    catalogClient.start
    VitalsMetricsRegistry.disable()
    catalogClient.registerAccount("burst", "burstomatic")
    catalogClient.allDomains() match {
      case Failure(exception) => log info s"Failed to get domains $exception"
      case Success(domains) => domains.foreach(d => log.info(s"${d.pk} - ${d.moniker}"))
    }
  }
}
