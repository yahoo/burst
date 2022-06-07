/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.master.test.support

import org.burstsys.alloy.alloy.views.AlloyJsonUseCaseViews
import org.burstsys.catalog.canned
import org.burstsys.catalog.canned.{CatalogCan, defaultMotif}
import org.burstsys.catalog.model.domain.CatalogCannedDomain
import org.burstsys.catalog.model.view.CatalogCannedView

final class MasterDeploySpecCan extends CatalogCan {

  override def domains: Array[CatalogCannedDomain] = {
    Array(CatalogCannedDomain("BurstMasterTestDomain", udk = Some("BurstMasterTestDomain")))
  }

  override def views: Array[CatalogCannedView] = {
    Array(
      CatalogCannedView("BurstMasterTestView1", "BurstMasterTestDomain", 0,
        storeProperties = AlloyJsonUseCaseViews.quoSpecialView.view.storeProperties,
        viewMotif = defaultMotif,
        viewProperties = AlloyJsonUseCaseViews.quoSpecialView.view.viewProperties,
        schemaName = "unity",
        udk = Some("BurstMasterTestView1")
      ),

      CatalogCannedView("BurstMasterTestView2", "BurstMasterTestDomain", 0,
        storeProperties = AlloyJsonUseCaseViews.quoSpecialView.view.storeProperties,
        viewMotif = defaultMotif,
        viewProperties = AlloyJsonUseCaseViews.quoSpecialView.view.viewProperties,
        schemaName = "unity",
        udk = Some("BurstMasterTestView2")
      ),

      CatalogCannedView("BurstMasterTestView3", "BurstMasterTestDomain", 0,
        canned.sampleStoreProperties,
        canned.defaultMotif,
        schemaName = "unity",
        udk = Some("BurstMasterTestView3")
      ),

      CatalogCannedView("BurstMasterTestView4", "BurstMasterTestDomain", 0,
        canned.sampleStoreProperties,
        canned.defaultMotif,
        schemaName = "unity",
        udk = Some("BurstMasterTestView4")
      )
    )
  }

}
