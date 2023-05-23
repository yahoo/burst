/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.persist

import org.burstsys.relate.RelateExceptions.{BurstDuplicateKeyException, BurstUnknownPrimaryKeyException}
import org.burstsys.relate.dialect.SelectLockLevel
import org.burstsys.relate.dialect.SelectLockLevel.{NoLock, UpdateLock}
import org.burstsys.relate.{handleSqlException, throwMappedException}
import org.burstsys.vitals.errors.VitalsException
import scalikejdbc._

abstract class UdkCatalogEntityPersister[E <: UdkCatalogEntity] extends NamedCatalogEntityPersister[E] {

  final def findEntityByUdk(udk: String, lockLevel: SelectLockLevel = NoLock)(implicit session: DBSession): Option[E] = {
    sql"SELECT * FROM ${this.table} WHERE ${this.column.udk} = {udk} ${service.dialect.lockClause(lockLevel)}".bindByName(
      "udk" -> udk
    ).map(resultToEntity).single()
  }

  /**
    * Inserts or update the provided entity.
    *
    * In practice, pk is more important internally than to consumers. Consumers prefer
    * to use udks, as they tend to hold semantic meaning in downstream systems. Because
    * downstream systems prefer udks they can run into race conditions when creating entities
    * since udks must be unique.
    *
    * New entities should always have pk == 0, but pk == 0 does not imply that
    * the user intends to create the entity, since downstream systems often refer to objects by udk.
    *
    * @param entity the entity to save
    * @return a tuple containing the primary key of the entity (useful for inserts)
    *         and a flag indicating whether or not a write actually occurred
    */
  final def upsertEntity(entity: E)(implicit session: DBSession): (E, Boolean) = {
    try {
      if (entity.udk.isEmpty && entity.pk == 0) {
        // the catalog tab does not provide udks when creating entities.
        // and newly created entities will not have a pk
        (insertAndFetch(entity), true)

      } else {
        entity.pk match {
          // if the pk is provided it is trusted implicitly. Passing a pk allows clients to update a udk
          case pk if pk != 0 =>
            findEntityByPk(entity.pk, lockLevel = UpdateLock) match {
              // if the provided pk doesn't exist we can do nothing
              case None => throw BurstUnknownPrimaryKeyException(entity.pk)
              case Some(stored) => updateEntityIfChanged(entity, stored, updatesForEntityByPk(entity, stored))
            }

          // pk == 0 is the default case for objects passed in without a pk, since pk is `Long` not `Option[Long]`
          case pk if pk == 0 =>
            findEntityByUdk(entity.udk.get, lockLevel = UpdateLock) match {
              // if the provided udk doesn't exist, we are creating a new entity
              case None => (insertAndFetch(entity), true)
              // if the provided udk exists, we are updating an existing entity
              case Some(stored) => updateEntityIfChanged(entity, stored, updatesForEntityByUdk(entity, stored))
            }
        }
      }
    } catch handleSqlException(service.dialect) {
      // handle race conditions arising from concurrent attempts to create the same entity by udk
      case ex: BurstDuplicateKeyException =>
        if (entity.udk.isEmpty) {
          throw BurstDuplicateKeyException("Cannot update or insert entity", ex.cause)
        }
        try {
          findEntityByUdk(entity.udk.get, lockLevel = UpdateLock) match {
            case None => throw VitalsException("Cannot update or insert entity", ex)
            case Some(stored) => updateEntityIfChanged(entity, stored, updatesForEntityByUdk(entity, stored))
          }
        } catch throwMappedException(service.dialect)
    }
  }
}
