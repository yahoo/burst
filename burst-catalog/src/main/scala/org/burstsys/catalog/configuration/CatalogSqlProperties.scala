/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.configuration

import org.burstsys.catalog.configuration

trait CatalogSqlProperties extends Any {

  final def dbName: String = configuration.burstCatalogDbNameProperty.get

  final def dbUser: String = configuration.burstCatalogDbUserProperty.get

  final def dbPassword: String = configuration.burstCatalogDbPasswordProperty.get

  final def dbHost: String = configuration.burstCatalogDbHostProperty.get

  final def dbPort: Int = configuration.burstCatalogDbPortProperty.get

  final def dbConnections: Int = configuration.burstCatalogDbConnectionsProperty.get

}
