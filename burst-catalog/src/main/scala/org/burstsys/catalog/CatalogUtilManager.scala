/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog

import org.burstsys.catalog.CatalogService.{CatalogConfiguration, CatalogWorkerConfig}
import org.burstsys.catalog.model.cell._
import org.burstsys.catalog.model.site._
import org.burstsys.catalog.persist.CatalogSqlProvider
import org.burstsys.relate
import org.burstsys.relate.dialect.{RelateDialect, RelateMySqlDialect}
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsStandaloneServer}

import scala.util.{Failure, Success, Try}

trait CatalogUtilManager extends Any {
  def createTables(dropTables: Boolean): Unit
  def createCell(cellName: String): Unit
  def cleanUp(): Unit
}

object CatalogUtilManager {
  def apply(mode: CatalogConfiguration): CatalogUtilManager = CatalogUtilContext(mode)

  def DefaultSiteMoniker = "default"
}

private[catalog] final case
class CatalogUtilContext(configuration: CatalogConfiguration) extends CatalogUtilManager with CatalogSqlConsumer {
  val catalog: CatalogSqlProvider = CatalogSqlProvider(this).start

  override def modality: VitalsServiceModality = VitalsStandaloneServer

  override def dialect: RelateDialect = RelateMySqlDialect

  override def executeDDL: Boolean = false

  def createTables(dropTables: Boolean): Unit = {
    catalog.connection localTx {
      implicit session => catalog.executeDdl(dropIfExists = dropTables)
    }
  }

  def createCell(cellName: String): Unit = {
    catalog.connection localTx {
      implicit session =>
      val sitePk: relate.RelatePk =
        catalog.sites.findEntityByMoniker(CatalogUtilManager.DefaultSiteMoniker) match {
          case Some(site) => site.pk
          case None =>
            // create the default site
            Try(catalog.sites.insertEntity(CatalogSite(0, CatalogUtilManager.DefaultSiteMoniker))) match {
              case Success(pk) => pk
              case Failure(sce) => throw sce
            }
        }
        Try(catalog.cells.insertEntity(CatalogCell(pk = 0, moniker = cellName, siteFk = sitePk))) match {
          case Success(pk) => Unit
          case Failure(e) => throw e
        }
    }
  }

  def cleanUp(): Unit = catalog.stop
}

