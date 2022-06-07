/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.persist.view

import org.burstsys.relate.TableCreateSql
import scalikejdbc._

trait CatalogMySqlViewSql {

  self: CatalogViewPersister =>

  def mysqlCreateTableSql: TableCreateSql =
    sql"""
     CREATE TABLE  ${this.table} (
        ${this.column.pk} BIGINT NOT NULL AUTO_INCREMENT,
        ${this.column.moniker} VARCHAR(255) NOT NULL,
        ${this.column.labels} TEXT,
        ${this.column.domainFk} BIGINT,
        ${this.column.generationClock} BIGINT DEFAULT 0,
        ${this.column.udk} VARCHAR(255),
        ${this.column.schemaName} VARCHAR(255) NOT NULL,
        ${this.column.viewMotif} TEXT,
        ${this.column.storeProperties} TEXT,
        ${this.column.viewProperties} TEXT,
        ${this.column.modifyTimestamp} TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
        ${this.column.createTimestamp} TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        ${this.column.accessTimestamp} TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        PRIMARY KEY (${this.column.pk}),
        UNIQUE (${this.column.udk}, ${this.column.domainFk})
     ) ENGINE=InnoDb DEFAULT CHARSET=utf8
     """

}
