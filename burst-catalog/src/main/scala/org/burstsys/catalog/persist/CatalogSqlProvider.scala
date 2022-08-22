/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.persist

import org.burstsys.catalog.CatalogSqlConsumer
import org.burstsys.catalog.persist.account.CatalogAccountPersister
import org.burstsys.catalog.persist.domain.CatalogDomainPersister
import org.burstsys.catalog.persist.query.CatalogQueryPersister
import org.burstsys.catalog.persist.view.CatalogViewPersister
import org.burstsys.relate.dialect.RelateDialect
import org.burstsys.relate.provider.RelateProvider
import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.properties._
import scalikejdbc.DBSession

final case
class CatalogSqlProvider(service: CatalogSqlConsumer) extends RelateProvider {
  override def modality: VitalsServiceModality = service.modality

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATE
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  val accounts: CatalogAccountPersister = CatalogAccountPersister(this)

  val queries: CatalogQueryPersister = CatalogQueryPersister(this)

  val domains: CatalogDomainPersister = CatalogDomainPersister(this)

  val views: CatalogViewPersister = CatalogViewPersister(this)

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  override def dialect: RelateDialect = service.dialect

  override def dbHost: String = service.dbHost

  override def dbPort: Int = service.dbPort

  override def dbName: String = service.dbName

  override def dbUser: String = service.dbUser

  override def dbPassword: String = service.dbPassword

  override def dbConnections: Int = service.dbConnections

  override def executeDDL: Boolean = service.executeDDL

  def deleteLabeledData(label: VitalsPropertyKey, value: Option[String])(implicit session: DBSession): this.type = {
    for (p <- persisters) p match {
      case cp: NamedCatalogEntityPersister[_] =>
        cp.deleteAllEntitiesWithLabel(label, value)
      case _ =>
    }
    this
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    ensureNotRunning
    super.start
    Array(
      accounts,
      queries,
      domains,
      views
    ).foreach(registerPersister)
    markRunning
    this
  }



}
