/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.test.support

import org.burstsys.alloy.alloy.views.AlloyJsonUseCaseViews
import org.burstsys.catalog.canned
import org.burstsys.catalog.canned.{CatalogCan, defaultMotif}
import org.burstsys.catalog.model.domain.CatalogCannedDomain
import org.burstsys.catalog.model.view.CatalogCannedView

final class SupervisorDeploySpecCan extends CatalogCan {

  override def domains: Array[CatalogCannedDomain] = {
    Array(CatalogCannedDomain("BurstSupervisorTestDomain", udk = Some("BurstSupervisorTestDomain")))
  }

  override def views: Array[CatalogCannedView] = {
    Array(
      CatalogCannedView("BurstSupervisorTestView1", "BurstSupervisorTestDomain", 0,
        storeProperties = AlloyJsonUseCaseViews.quoSpecialView.view.storeProperties,
        viewMotif = defaultMotif,
        viewProperties = AlloyJsonUseCaseViews.quoSpecialView.view.viewProperties,
        schemaName = "unity",
        udk = Some("BurstSupervisorTestView1")
      ),

      CatalogCannedView("BurstSupervisorTestView2", "BurstSupervisorTestDomain", 0,
        storeProperties = AlloyJsonUseCaseViews.quoSpecialView.view.storeProperties,
        viewMotif = defaultMotif,
        viewProperties = AlloyJsonUseCaseViews.quoSpecialView.view.viewProperties,
        schemaName = "unity",
        udk = Some("BurstSupervisorTestView2")
      ),

      CatalogCannedView("BurstSupervisorTestView3", "BurstSupervisorTestDomain", 0,
        canned.sampleStoreProperties,
        canned.defaultMotif,
        schemaName = "unity",
        udk = Some("BurstSupervisorTestView3")
      ),

      CatalogCannedView("BurstSupervisorTestView4", "BurstSupervisorTestDomain", 0,
        canned.sampleStoreProperties,
        canned.defaultMotif,
        schemaName = "unity",
        udk = Some("BurstSupervisorTestView4")
      )
    )
  }

}
