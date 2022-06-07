/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.persist.query

import org.burstsys.relate.TableCreateSql
import scalikejdbc._

trait CatalogMySqlQuerySql {

  self: CatalogQueryPersister =>

  def mysqlCreateTableSql: TableCreateSql =
    sql"""
     CREATE TABLE  ${this.table} (
        ${this.column.pk} BIGINT NOT NULL AUTO_INCREMENT,
        ${this.column.moniker} VARCHAR(255) NOT NULL,
        ${this.column.labels} TEXT,
        ${this.column.languageType} VARCHAR(255),
        ${this.column.source} TEXT,
        PRIMARY KEY (${this.column.pk}),
        UNIQUE (${this.column.moniker})
     ) ENGINE=InnoDb DEFAULT CHARSET=utf8
     """


}
