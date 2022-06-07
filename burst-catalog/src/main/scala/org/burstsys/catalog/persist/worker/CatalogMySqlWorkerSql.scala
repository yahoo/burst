/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.persist.worker

import org.burstsys.relate.TableCreateSql
import scalikejdbc._

trait CatalogMySqlWorkerSql {

  self: CatalogWorkerPersister =>

  def mysqlCreateTableSql: TableCreateSql =
    sql"""
     CREATE TABLE  ${this.table} (
       ${this.column.pk} BIGINT NOT NULL AUTO_INCREMENT,
       ${this.column.labels} TEXT,
       ${this.column.nodeName} VARCHAR(255) NOT NULL,
       ${this.column.nodeAddress} VARCHAR(255) NOT NULL,
       ${this.column.siteFk} BIGINT NOT NULL,
       ${this.column.cellFk} BIGINT,
       ${this.column.moniker} VARCHAR(255) NOT NULL UNIQUE,
       ${this.column.workerProperties} TEXT,
       PRIMARY KEY (${this.column.pk}),
       UNIQUE (${this.column.moniker})
     )
     """


}
