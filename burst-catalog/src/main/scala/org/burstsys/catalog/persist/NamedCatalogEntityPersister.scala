/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.persist

import org.burstsys.catalog.BurstMoniker
import org.burstsys.relate.RelatePersister
import scalikejdbc._


/**
  * Functions for searching generic catalog entities (which all have a moniker and labels)
  */
abstract class NamedCatalogEntityPersister[E <: NamedCatalogEntity] extends RelatePersister[E] {

  final def findEntityByMoniker(moniker: BurstMoniker)(implicit session: DBSession): Option[E] = {
    sql"SELECT * FROM ${this.table} WHERE ${this.column.moniker} = {moniker}".bindByName(
      'moniker -> moniker
    ).map(resultToEntity).single().apply()
  }

  final def deleteAllEntitiesWithLabel(label: String, value: Option[String])(implicit session: DBSession): Unit = {
    sql"DELETE FROM ${this.table} WHERE ${this.column.labels} LIKE {val}".bindByName(
      'val -> likeLabel(label, value)
    ).update().apply()
  }

  final def searchEntitiesByMoniker(descriptor: String, limit: Option[Int])(implicit session: DBSession): List[E] = {
    sql"""SELECT * FROM ${this.table}
         WHERE LOWER(${this.column.moniker}) LIKE LOWER({descriptor}) ${service.dialect.limitClause(limit)}
      """.bindByName('descriptor -> s"%$descriptor%").map(resultToEntity).list().apply()
  }

  final def searchEntitiesByLabel(label: String, value: Option[String], limit: Option[Int])(implicit session: DBSession): List[E] = {
    sql"""SELECT * FROM ${this.table}
         WHERE ${this.column.labels} LIKE {label} ${service.dialect.limitClause(limit)}
      """.bindByName('label -> likeLabel(label, value)).map(resultToEntity).list().apply()
  }

  private def likeLabel(label: String, value: Option[String]): String = s"%$label=${value.getOrElse("%")};%"
}


