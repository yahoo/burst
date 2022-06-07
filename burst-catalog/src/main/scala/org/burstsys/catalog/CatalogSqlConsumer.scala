/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog

import org.burstsys.catalog.configuration.CatalogSqlProperties
import org.burstsys.relate.dialect.RelateDialect
import org.burstsys.vitals.VitalsService.VitalsServiceModality

/**
  * All the information for the SQL persistence part of the catalog
  */
trait CatalogSqlConsumer extends Any with CatalogSqlProperties {
  def modality: VitalsServiceModality
  def dialect: RelateDialect
  def executeDDL:  Boolean
}

