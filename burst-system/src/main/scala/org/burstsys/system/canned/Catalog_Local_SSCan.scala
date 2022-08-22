/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.canned

import org.burstsys.catalog.canned
import org.burstsys.catalog.canned.CatalogCan
import org.burstsys.catalog.model.domain.CatalogCannedDomain
import org.burstsys.catalog.model.view.CatalogCannedView
import org.burstsys.fabric.data.model.store.FabricStoreNameProperty
import org.burstsys.samplestore.api.{SampleStoreSourceNameProperty, SampleStoreSourceVersionProperty}

final class Catalog_Local_SSCan extends CatalogCan {

  val storeProperties = Map(
    FabricStoreNameProperty -> "sample",
    SampleStoreSourceNameProperty -> "AppEventsBrio",
    SampleStoreSourceVersionProperty -> "0.0"
  )

  override def domains: Array[CatalogCannedDomain] = {
    Array(
      CatalogCannedDomain("AppEventsSummarizerTest_Local_1234",
        Map("beast.domain.project.id" -> "1234")
      )
    )
  }

  override def views: Array[CatalogCannedView] = {
    Array(
      CatalogCannedView(
        "AppEventsSummarizerTest_Local_1234_default",
        "AppEventsSummarizerTest_Local_1234",
        -1,
        storeProperties,
        canned.defaultMotif,
        schemaName = "quo"
      )
    )
  }

}
