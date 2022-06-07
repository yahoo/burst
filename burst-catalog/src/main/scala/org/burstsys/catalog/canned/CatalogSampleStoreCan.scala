/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.canned

import org.burstsys.catalog.model.domain._
import org.burstsys.catalog.model.view.CatalogCannedView

final class CatalogSampleStoreCan extends CatalogCan {

  override def domains: Array[CatalogCannedDomain] = {
    Array(
      CatalogCannedDomain("Domain4"),
      CatalogCannedDomain("Domain - No Views")
    )
  }

  override def views: Array[CatalogCannedView] = {
    Array(
      CatalogCannedView("Domain4View1", "Domain4", 1,
        sampleStoreProperties, defaultMotif, schemaName = "quo"
      )
    )
  }

}
