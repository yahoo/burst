/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.relate

/**
  * A simple long value primary key persistent entity
  */
trait RelateEntity {

  /**
    * The primary key for this entity
    * @return
    */
  def pk: RelatePk

  /**
    * Used to determine if this version of the entity differs from the version in the database
    * to a sufficient degree that the database needs to be updated.
    * @param storedEntity the version of this entity that exists in the database
    * @return true if this version of the entity should be written to the database
    */
  def shouldUpdate(storedEntity: RelateEntity): Boolean = storedEntity.pk == pk
}
