/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.persist.view

import org.burstsys.relate.TableCreateSql
import scalikejdbc._

trait CatalogDerbyViewSql {

  self: CatalogViewPersister =>

  def derbyCreateTableSql: TableCreateSql =
    sql"""
     CREATE TABLE  ${this.table} (
      ${this.column.pk} BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
      ${this.column.domainFk} BIGINT,
      ${this.column.generationClock} BIGINT DEFAULT 0,
      ${this.column.moniker} VARCHAR(255) NOT NULL UNIQUE,
      ${this.column.udk} VARCHAR(255),
      ${this.column.labels} VARCHAR(32672),
      ${this.column.schemaName} VARCHAR(255) NOT NULL,
      ${this.column.viewMotif} VARCHAR(32672),
      ${this.column.storeProperties} VARCHAR(32672),
      ${this.column.viewProperties} VARCHAR(32672),
      ${this.column.modifyTimestamp} TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      ${this.column.createTimestamp} TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      ${this.column.accessTimestamp} TIMESTAMP,

      UNIQUE (${this.column.udk}, ${this.column.domainFk})
    )
    """

}
