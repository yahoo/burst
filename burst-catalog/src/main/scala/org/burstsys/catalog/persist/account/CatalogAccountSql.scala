/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.persist.account

import org.burstsys.relate.TableCreateSql
import scalikejdbc._

trait CatalogAccountSql {

  self: CatalogAccountPersister =>

  def derbyCreateTableSql: TableCreateSql =
    sql"""
     CREATE TABLE  ${this.table} (
       ${this.column.pk} BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
       ${this.column.moniker} VARCHAR(255) NOT NULL,
       ${this.column.labels} VARCHAR(32672),
       ${this.column.hashedPassword} VARCHAR(255),
       ${this.column.salt} VARCHAR(255),
       UNIQUE (${this.column.moniker})
      )
     """

  def mysqlCreateTableSql: TableCreateSql =
    sql"""
     CREATE TABLE  ${this.table} (
        ${this.column.pk} BIGINT NOT NULL AUTO_INCREMENT,
        ${this.column.moniker} VARCHAR(255) NOT NULL,
        ${this.column.labels} TEXT,
        ${this.column.hashedPassword} VARCHAR(255),
        ${this.column.salt} VARCHAR(255),
        PRIMARY KEY (${this.column.pk}),
       UNIQUE (${this.column.moniker})
     ) ENGINE=InnoDb DEFAULT CHARSET=utf8
     """


}
