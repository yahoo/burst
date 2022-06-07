/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.model

import org.burstsys.catalog.persist.NamedCatalogEntity
import org.burstsys.vitals.properties.VitalsPropertyMap

import scala.language.implicitConversions

package object account {

  type CatalogAccount = CatalogSqlAccount

  final case class CatalogSqlAccount(pk: Long, moniker: String, hashedPassword: String, salt: String, labels: Option[VitalsPropertyMap])
    extends NamedCatalogEntity

  final case class CatalogCannedAccount(username: String, password: String)

}
