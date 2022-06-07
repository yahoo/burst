/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.persist

import org.burstsys.catalog.BurstMoniker
import org.burstsys.relate.RelateEntity

/**
  * All items in the catalog have a moniker and labels
  */
trait NamedCatalogEntity extends RelateEntity {

  /**
    * The entity's name
    */
  def moniker: BurstMoniker

  /**
    * The entity's labels
    */
  def labels: Option[scala.collection.Map[String, String]]

}
