/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.canned

import org.burstsys.catalog.canned
import org.burstsys.catalog.canned.CatalogCan
import org.burstsys.catalog.model.cell._
import org.burstsys.catalog.model.domain._
import org.burstsys.catalog.model.site.CatalogCannedSite
import org.burstsys.catalog.model.view._
import org.burstsys.fabric.data.model.store.FabricStoreNameProperty
import org.burstsys.samplestore.api.SampleStoreSourceNameProperty
import org.burstsys.samplestore.api.SampleStoreSourceVersionProperty

final class Catalog_GQ1_C2_SSCan extends CatalogCan {

  val storeProperties = Map(
    FabricStoreNameProperty -> "sample",
    SampleStoreSourceNameProperty -> "AppEventsBrio",
    SampleStoreSourceVersionProperty -> "0.0"
  )

  override def sites: Array[CatalogCannedSite] = {
    Array(
      CatalogCannedSite("GQ1_SS")
    )
  }

  override def cells: Array[CatalogCannedCell] = {
    Array(
      CatalogCannedCell("GQ1_C2", "GQ1_SS")
    )
  }

  override def domains: Array[CatalogCannedDomain] = {
    Array(
      CatalogCannedDomain("Subway Surfers_GQ1_C2_181662",
        Map("beast.domain.project.id" -> "181662")
      ),
      CatalogCannedDomain("Video Poker Jackpot!_GQ1_C2_227056",
        Map("beast.domain.project.id" -> "227056")
      ),
      CatalogCannedDomain("Pink Pad Pro iPhone PROD_GQ1_C2_11044",
        Map("beast.domain.project.id" -> "11044")
      )
    )
  }

  override def views: Array[CatalogCannedView] = {
    Array(
      CatalogCannedView(
        "Subway Surfers_GQ1_C2_181662_default",
        "Subway Surfers_GQ1_C2_181662",
        -1,
        storeProperties,
        canned.defaultMotif,
        schemaName = "quo"
      ),

      CatalogCannedView(
        "Video Poker Jackpot!_GQ1_C2_227056_default",
        "Video Poker Jackpot!_GQ1_C2_227056",
        -1,
        storeProperties,
        canned.defaultMotif,
        schemaName = "quo"
      ),

      CatalogCannedView(
        "Pink Pad Pro iPhone PROD_GQ1_C2_11044_default",
        "Pink Pad Pro iPhone PROD_GQ1_C2_11044",
        -1,
        storeProperties,
        canned.defaultMotif,
        schemaName = "quo"
      )
    )
  }

}
