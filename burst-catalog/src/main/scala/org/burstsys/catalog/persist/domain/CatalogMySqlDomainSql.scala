/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.persist.domain

import org.burstsys.relate.TableCreateSql
import scalikejdbc._

trait CatalogMySqlDomainSql {

  self: CatalogDomainPersister =>

  def mysqlCreateTableSql: TableCreateSql =
    sql"""
     CREATE TABLE  ${this.table} (
        ${this.column.pk} BIGINT NOT NULL AUTO_INCREMENT,
        ${this.column.moniker} VARCHAR(255) NOT NULL UNIQUE,
        ${this.column.labels} TEXT,
        ${this.column.domainProperties} TEXT,
        ${this.column.udk} VARCHAR(255) UNIQUE,
        ${this.column.modifyTimestamp} TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        ${this.column.createTimestamp} TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        PRIMARY KEY (${this.column.pk}),
        UNIQUE (${this.column.udk})
     ) ENGINE=InnoDb DEFAULT CHARSET=utf8
     """
}
