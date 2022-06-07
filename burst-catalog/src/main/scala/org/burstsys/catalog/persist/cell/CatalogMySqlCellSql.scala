/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.persist.cell

import org.burstsys.relate.TableCreateSql
import scalikejdbc._

trait CatalogMySqlCellSql {

  self: CatalogCellPersister =>

  def mysqlCreateTableSql: TableCreateSql =
    sql"""
     CREATE TABLE  ${this.table} (
       ${this.column.pk} BIGINT NOT NULL AUTO_INCREMENT,
       ${this.column.labels} TEXT,
       ${this.column.moniker} VARCHAR(255) NOT NULL UNIQUE,
       ${this.column.siteFk} BIGINT NOT NULL,
       ${this.column.cellProperties} TEXT,
       PRIMARY KEY (${this.column.pk}),
       UNIQUE (${this.column.moniker})
     )
     """


}
