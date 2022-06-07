/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.persist.site

import org.burstsys.relate.TableCreateSql
import scalikejdbc._

trait CatalogMySqlSiteSql {

  self: CatalogSitePersister =>

  def mysqlCreateTableSql: TableCreateSql =
    sql"""
     CREATE TABLE  ${this.table} (
       ${this.column.pk} BIGINT NOT NULL AUTO_INCREMENT,
       ${this.column.labels} TEXT,
       ${this.column.moniker} VARCHAR(255) NOT NULL UNIQUE,
       ${this.column.siteProperties} TEXT,
       PRIMARY KEY (${this.column.pk}),
       UNIQUE (${this.column.moniker})
     )
     """


}
