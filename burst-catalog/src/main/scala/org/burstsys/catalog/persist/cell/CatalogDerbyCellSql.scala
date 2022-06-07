/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.persist.cell

import org.burstsys.relate.TableCreateSql
import scalikejdbc._

trait CatalogDerbyCellSql {

  self: CatalogCellPersister =>

  def derbyCreateTableSql: TableCreateSql =
    sql"""
     CREATE TABLE  ${this.table} (
       ${this.column.pk} BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
       ${this.column.labels} VARCHAR(32672),
       ${this.column.moniker} VARCHAR(255) NOT NULL,
       ${this.column.siteFk} BIGINT NOT NULL,
       ${this.column.cellProperties} VARCHAR(32672),
       UNIQUE (${this.column.moniker})
     )
     """


}
