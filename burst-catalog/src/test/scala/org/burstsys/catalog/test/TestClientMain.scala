/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.test

import org.burstsys.{catalog, vitals}
import org.burstsys.catalog.CatalogService
import org.burstsys.catalog.CatalogService.CatalogUnitTestClientConfig
import org.burstsys.vitals.logging._
import org.burstsys.vitals.metrics.VitalsMetricsRegistry

import scala.util.{Failure, Success}

object TestClientMain {
  def main(args: Array[String]): Unit = {
    VitalsLog.configureLogging("test-api-client", consoleOnly = true)

    catalog.configuration.burstCatalogApiHostProperty.set("localhost")
    catalog.configuration.burstCatalogApiSslEnableProperty.set(true)
    vitals.configuration.burstSslKeyPath.set("/tmp/key.pem")
    vitals.configuration.burstSslCertPath.set("/tmp/cert.pem")
    vitals.configuration.burstTrustedCaPath.set("/tmp/ca.cert.pem")


    val catalogClient: CatalogService = CatalogService(CatalogUnitTestClientConfig)
    catalogClient.start
    VitalsMetricsRegistry.disable()

    catalogClient.allDomains() match {
      case Failure(exception) => log info s"Failed to get domains $exception"
      case Success(domains) => domains.foreach(d => log.info(s"${d.pk} - ${d.moniker}"))
    }
  }
}
