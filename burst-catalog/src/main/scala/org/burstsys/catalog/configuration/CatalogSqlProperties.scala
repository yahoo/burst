/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.configuration

import org.burstsys.catalog.configuration

trait CatalogSqlProperties extends Any {

  final def dbName: String = configuration.burstCatalogDbNameProperty.getOrThrow

  final def dbUser: String = configuration.burstCatalogDbUserProperty.getOrThrow

  final def dbPassword: String = configuration.burstCatalogDbPasswordProperty.getOrThrow

  final def dbHost: String = configuration.burstCatalogDbHostProperty.getOrThrow

  final def dbPort: Int = configuration.burstCatalogDbPortProperty.getOrThrow

  final def dbConnections: Int = configuration.burstCatalogDbConnectionsProperty.getOrThrow

}
