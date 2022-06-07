/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.canned

import org.burstsys.catalog.model.account
import org.burstsys.catalog.model.account.CatalogCannedAccount
import org.burstsys.catalog.model.cell._
import org.burstsys.catalog.model.domain.CatalogCannedDomain
import org.burstsys.catalog.model.master.CatalogCannedMaster
import org.burstsys.catalog.model.site.CatalogCannedSite
import org.burstsys.catalog.model.view.CatalogView.ViewLoadTimeoutMsProperty
import org.burstsys.catalog.model.view.{CatalogCannedView, _}
import org.burstsys.catalog.model.worker.CatalogCannedWorker
import org.burstsys.fabric
import org.burstsys.fabric.metadata
import org.burstsys.fabric.topology.model.node.UnknownFabricNodePort
import org.burstsys.vitals.net.{getPublicHostAddress, getPublicHostName}
import org.burstsys.vitals.properties._

import scala.concurrent.duration._
import scala.language.postfixOps


final class CatalogDefaultBaseTypesCan extends CatalogCan {

  override def accounts: Array[account.CatalogCannedAccount] = Array(
    CatalogCannedAccount("burst", "burstomatic")
  )

  override def sites: Array[CatalogCannedSite] = {
    Array(
      CatalogCannedSite("Site1",
        """
          |site_k1=site_v1;
          |site_k2=site_v2;
          |site_k3=site_v3;
        """.stripMargin)
    )
  }

  override def cells: Array[CatalogCannedCell] = {
    Array(
      CatalogCannedCell("Cell1", "Site1",
        """
          |cell_k1=cell_v1;
          |cell_k2=cell_v2;
          |cell_k3=cell_v3;
        """.stripMargin)
    )
  }

  private val exceptionDomain = "Domain3_Broken"

  override def domains: Array[CatalogCannedDomain] = {
    Array(
      CatalogCannedDomain("Domain1",
        """
          |domain1_k3=domain1_v3;
          |domain1_k1=domain1_v1;
          |domain1_k2=domain1_v2;
        """.stripMargin, Some("udk01")
      ),
      CatalogCannedDomain("Domain2",
        """
          |domain2_k3=domain2_v3;
          |domain2_k1=domain2_v1;
          |domain2_k2=domain2_v2;
        """.stripMargin, Some("udk02")
      ),
      CatalogCannedDomain(exceptionDomain, domainProperties = "", udk = Some("udk03"), labels = Some(Map.empty))
    )
  }


  override def views: Array[CatalogCannedView] = {
    val timingProperties = Map(
      fabric.metadata.ViewNextLoadStaleMsProperty -> (1 day).toMillis.toString,
      ViewLoadTimeoutMsProperty -> (10 minutes).toMillis.toString
    )
    Array(
      CatalogCannedView("Domain1View1", "Domain1", generationClock = 1, cannedStoreProperties, defaultMotif,
        viewProperties = timingProperties,
        schemaName = "quo", udk = Some("vudk01")
      ),

      CatalogCannedView("Domain1View2", "Domain1", generationClock = 1,
//        storeProperties = AlloyJsonUseCaseViews.quoSpecialView.view.storeProperties,
        viewMotif = defaultMotif,
//        viewProperties = AlloyJsonUseCaseViews.quoSpecialView.view.viewProperties ++ timingProperties,
        schemaName = "quo", udk = Some("vudk02") // unique in domain 1
      ),

      CatalogCannedView("domain2_default_view", "Domain2", generationClock = 1, cannedStoreProperties, defaultMotif,
        viewProperties = timingProperties,
        schemaName = "quo"
      ),

      CatalogCannedView("domain2_custom_view", "Domain2", generationClock = 1,
//        storeProperties = AlloyJsonUseCaseViews.quoSpecialView.view.storeProperties,
        viewMotif = defaultMotif,
//        viewProperties = AlloyJsonUseCaseViews.quoSpecialView.view.viewProperties ++ timingProperties,
        schemaName = "quo"
      )
    ) ++
      Array("Worker", "Master").flatMap(errorLocation =>
        Array("RuntimeException", "FabricException", "Timeout", "NoData").map(errorType =>
          CatalogCannedView(s"${exceptionDomain}_${errorLocation}_$errorType", exceptionDomain, 1,
            exceptionalStoreProperties + (
              "burst.store.exception.FailureMode" -> errorType,
              "burst.store.exception.FailureLocation" -> s"FailOn$errorLocation",
              "burst.store.exception.FailureRate" -> "1.0"
            ),
            defaultMotif, viewProperties = Map.empty, labels = Some(Map.empty), schemaName = "quo", udk = Some(s"${errorLocation}_$errorType")
          )
        )
      )
  }

  override def masters: Array[CatalogCannedMaster] = {
    Array(
      CatalogCannedMaster(
        moniker = getPublicHostAddress,
        nodeName = getPublicHostName,
        nodeAddress = getPublicHostAddress,
        masterPort = UnknownFabricNodePort,
        siteMoniker = "Site1",
        cellMoniker = Some("Cell1"),
        masterProperties = "local=true; master=true;"
      )
    )
  }

  override def workers: Array[CatalogCannedWorker] = {
    Array(
      CatalogCannedWorker(
        moniker = getPublicHostAddress,
        nodeName = getPublicHostName,
        nodeAddress = getPublicHostAddress,
        siteMoniker = "Site1",
        cellMoniker = Some("Cell1"),
        workerProperties = "local=true; worker=true;"
      )
    )
  }

}
