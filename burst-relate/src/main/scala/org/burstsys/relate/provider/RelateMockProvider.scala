/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.relate.provider

import org.burstsys.relate.dialect.{RelateDerbyDialect, RelateDialect, RelateMySqlDialect}
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsStandaloneServer}
import org.burstsys.vitals.logging._
import org.apache.logging.log4j.Logger

/**
  * a mock provider for unit tests
  */
final case
class RelateMockProvider(useDerby: Boolean = true) extends RelateProvider {

  override def modality: VitalsServiceModality = VitalsStandaloneServer

  override def dbName: String = RelateMockDatabaseName

  override def dialect: RelateDialect = if (useDerby) RelateDerbyDialect else RelateMySqlDialect

  override def dbHost: String = if (useDerby) "" else "localhost"

  override def dbPort: Int = if (useDerby) 1527 else 3306

  override def dbUser: String = if (useDerby) "" else "burst"

  override def dbPassword: String = if (useDerby) "" else "burst"

  override def dbConnections: Int = 500

  override def executeDDL: Boolean = true

}
