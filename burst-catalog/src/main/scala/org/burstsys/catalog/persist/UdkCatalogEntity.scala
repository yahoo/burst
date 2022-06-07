/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.persist

import org.burstsys.relate.RelateEntity
import org.burstsys.vitals.errors.VitalsException

/**
  * Some items in the catalog are also identifiable by a user defined key (UDK)
  */
trait UdkCatalogEntity extends NamedCatalogEntity {
  /**
    * The entity's UDK
    */
  def udk: Option[String]

  /**
    * Super permissive should update algorithm. Will update if:
    * - entity from user has no pk, but udk matches
    * - entity from user has pk that matches pk from database
    * TODO: Consider complaining loudly if pks are mismatched as this should never happen
    * @param storedEntity the version of this entity that exists in the database
    * @return true if this version of the entity should be written tot the database
    */
  override def shouldUpdate(storedEntity: RelateEntity): Boolean = {
    storedEntity match {
      case fromDb: UdkCatalogEntity => (pk == 0 && fromDb.udk == udk) || fromDb.pk == pk
      case _ => throw VitalsException(s"Something has gone horribly wrong. Expected a ${this.getClass} but found a ${storedEntity.getClass}")
    }
  }
}
