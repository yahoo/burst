/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.canned

import org.burstsys.catalog.model.domain._
import org.burstsys.catalog.model.view.CatalogCannedView

final class CatalogSearStoreCan extends CatalogCan {

  override def domains: Array[CatalogCannedDomain] = {
    Array(
      CatalogCannedDomain("SearDomain")
    )
  }

  override def views: Array[CatalogCannedView] = {
    Array(
      CatalogCannedView("SearView", "SearDomain", 1,
        searStoreProperties, defaultMotif, schemaName = "unity"
      )
    )
  }

}
