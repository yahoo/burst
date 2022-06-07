/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.persist.domain

import org.burstsys.relate.TableCreateSql
import scalikejdbc._

trait CatalogDerbyDomainSql {

  self: CatalogDomainPersister =>

  def derbyCreateTableSql: TableCreateSql =
    sql"""
     CREATE TABLE  ${this.table} (
        ${this.column.pk} BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
        ${this.column.moniker} VARCHAR(255) NOT NULL,
        ${this.column.labels} VARCHAR(32672),
        ${this.column.domainProperties} VARCHAR(32672),
        ${this.column.udk} VARCHAR(255),
        ${this.column.modifyTimestamp} TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        ${this.column.createTimestamp} TIMESTAMP,
        UNIQUE (${this.column.udk})
    )
    """
}
