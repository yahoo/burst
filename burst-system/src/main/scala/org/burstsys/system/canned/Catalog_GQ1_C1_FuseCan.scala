/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.canned

import org.burstsys.alloy
import org.burstsys.catalog.canned
import org.burstsys.catalog.canned.CatalogCan
import org.burstsys.catalog.model.cell.CatalogCannedCell
import org.burstsys.catalog.model.domain.CatalogCannedDomain
import org.burstsys.catalog.model.site.CatalogCannedSite
import org.burstsys.catalog.model.view.CatalogCannedView
import org.burstsys.fabric.data.model.store.FabricStoreNameProperty

import scala.language.postfixOps

final class Catalog_GQ1_C1_FuseCan extends CatalogCan {

  override def sites: Array[CatalogCannedSite] = {
    Array(
      CatalogCannedSite("GQ1_Fuse")
    )
  }

  override def cells: Array[CatalogCannedCell] = {
    Array(
      CatalogCannedCell("GQ1_C1", "GQ1_Fuse")
    )
  }

  override def domains: Array[CatalogCannedDomain] = {
    Array(
      CatalogCannedDomain("Snapchat Android_233295"),
      CatalogCannedDomain("Independent Digital_Independent News_Android")
    )
  }


  override def views: Array[CatalogCannedView] = {
    Array(
      CatalogCannedView(
        "Snapchat Android_233295_default",
        "Snapchat Android_233295",
        -1,
        Map(FabricStoreNameProperty -> "fuse"),
        canned.defaultMotif,
        Map(
          alloy.store.AlloyViewDataPathProperty -> "57855/233295/data-r-02895"
        ),
        schemaName = "quo"
      ),
      CatalogCannedView(
        "Independent Digital_Independent News_Android_default",
        "Independent Digital_Independent News_Android",
        -1,
        Map(FabricStoreNameProperty -> "fuse"),
        canned.defaultMotif,
        Map(
          alloy.store.AlloyViewDataPathProperty -> "58137/94467/data-r-363"
        ),
        schemaName = "quo"
      )
    )
  }

}
