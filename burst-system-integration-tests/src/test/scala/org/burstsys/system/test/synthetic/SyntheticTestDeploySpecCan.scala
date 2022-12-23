/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.test.synthetic

import org.burstsys.catalog.canned
import org.burstsys.catalog.canned.CatalogCan
import org.burstsys.catalog.model.domain.CatalogCannedDomain
import org.burstsys.catalog.model.view.CatalogCannedView

final class SyntheticTestDeploySpecCan extends CatalogCan {
  override def domains: Array[CatalogCannedDomain] = {
    Array(
      CatalogCannedDomain("STDomain",
        domainProperties = Map(
          "synthetic.samplestore.use-localhost" -> "true",
          "synthetic.samplestore.press.dataset" -> "simple-unity"
        )
      )
    )
  }
  override def views: Array[CatalogCannedView] = {
    Array(
      CatalogCannedView("BurstSyntheticView", "STDomain", 0,
        Map(
          "synthetic.samplestore.press.item.count"->"5",
          "burst.samplestore.source.version"->"0.0",
          "burst.samplestore.source.name"->"synthetic-samplesource",
          "burst.store.name"->"sample"
         ),
        canned.defaultMotif,
        schemaName = "unity"
      )
    )
  }

}
