/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog

import org.burstsys.catalog.CatalogService.CatalogConfiguration
import org.burstsys.catalog.persist.CatalogSqlProvider
import org.burstsys.relate.dialect.{RelateDialect, RelateMySqlDialect}
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsStandaloneServer}

trait CatalogUtilManager extends Any {
  def createTables(dropTables: Boolean): Unit
  def cleanUp(): Unit
}

object CatalogUtilManager {
  def apply(mode: CatalogConfiguration): CatalogUtilManager = CatalogUtilContext(mode)
}

private[catalog] final case
class CatalogUtilContext(configuration: CatalogConfiguration) extends CatalogUtilManager with CatalogSqlConsumer {
  val catalog: CatalogSqlProvider = CatalogSqlProvider(this).start

  override def modality: VitalsServiceModality = VitalsStandaloneServer

  override def dialect: RelateDialect = RelateMySqlDialect

  override def executeDDL: Boolean = false

  def createTables(dropTables: Boolean): Unit = {
    catalog.connection localTx {
      implicit session => catalog.executeDdl(dropIfExists = dropTables)
    }
  }

  def cleanUp(): Unit = catalog.stop
}

