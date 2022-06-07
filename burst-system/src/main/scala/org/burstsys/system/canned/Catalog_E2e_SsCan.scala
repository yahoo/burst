/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.canned

import org.burstsys.catalog.canned
import org.burstsys.catalog.canned.CatalogCan
import org.burstsys.catalog.model.cell._
import org.burstsys.catalog.model.domain._
import org.burstsys.catalog.model.site.CatalogCannedSite
import org.burstsys.catalog.model.view.CatalogCannedView
import org.burstsys.fabric.data.model.store.FabricStoreNameProperty
import org.burstsys.samplestore.api.SampleStoreSourceNameProperty
import org.burstsys.samplestore.api.SampleStoreSourceVersionProperty

final class Catalog_E2e_SsCan extends CatalogCan {

  val storeProperties = Map(
    FabricStoreNameProperty -> "sample",
    SampleStoreSourceNameProperty -> "AppEventsBrio",
    SampleStoreSourceVersionProperty -> "0.0"
  )

  override def sites: Array[CatalogCannedSite] = {
    Array(
      CatalogCannedSite("E2E")
    )
  }

  override def cells: Array[CatalogCannedCell] = {
    Array(
      CatalogCannedCell("E2E", "E2E")
    )
  }

  override def domains: Array[CatalogCannedDomain] = {
    Array(
      CatalogCannedDomain("Subway Surfers_E2E_181662",
        Map("beast.domain.project.id" -> "181662")
      ),
      CatalogCannedDomain("Video Poker Jackpot!_E2E_227056",
        Map("beast.domain.project.id" -> "227056")
      ),
      CatalogCannedDomain("Pink Pad Pro iPhone PROD_E2E_11044",
        Map("beast.domain.project.id" -> "11044")
      ),
      CatalogCannedDomain("TestDataset_Ytest0_E2E_29735",
        Map("beast.domain.project.id" -> "29735")
      )
    )
  }

  override def views: Array[CatalogCannedView] = {
    Array(
      CatalogCannedView(
        "Subway Surfers_E2E_181662_default",
        "Subway Surfers_E2E_181662",
        -1,
        storeProperties,
        canned.defaultMotif,
        schemaName = "quo"
      ),

      CatalogCannedView(
        "Video Poker Jackpot!_E2E_227056_default",
        "Video Poker Jackpot!_E2E_227056",
        -1,
        storeProperties,
        canned.defaultMotif,
        schemaName = "quo"
      ),

      CatalogCannedView(
        "Pink Pad Pro iPhone PROD_E2E_11044_default",
        "Pink Pad Pro iPhone PROD_E2E_11044",
        -1,
        storeProperties,
        canned.defaultMotif,
        schemaName = "quo"
      ),

      CatalogCannedView(
        "TestDataset_Ytest0_E2E_29735_default",
        "TestDataset_Ytest0_E2E_29735",
        -1,
        storeProperties,
        canned.defaultMotif,
        schemaName = "quo"
      )
    )
  }

}
