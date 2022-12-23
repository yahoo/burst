/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.test.support

import org.burstsys.alloy.alloy.views.AlloyJsonUseCaseViews
import org.burstsys.catalog.canned
import org.burstsys.catalog.canned.{CatalogCan, defaultMotif}
import org.burstsys.catalog.model.domain.CatalogCannedDomain
import org.burstsys.catalog.model.view.CatalogCannedView

final class SupervisorDeploySpecCan extends CatalogCan {

  override def domains: Array[CatalogCannedDomain] = {
    Array(CatalogCannedDomain("BurstSupervisorTestDomain"))
  }

  override def views: Array[CatalogCannedView] = {
//    val jsonSampleStoreProperties = canned.sampleStoreProperties + ("burst.samplestore.source.name" -> "JsonBrio")

    Array(
      CatalogCannedView("BurstSupervisorTestView1", "BurstSupervisorTestDomain", 0,
        storeProperties = AlloyJsonUseCaseViews.quoSpecialView.view.storeProperties,
        viewMotif = defaultMotif,
        viewProperties = AlloyJsonUseCaseViews.quoSpecialView.view.viewProperties,
        schemaName = "quo"
      ),

      CatalogCannedView("BurstSupervisorTestView2", "BurstSupervisorTestDomain", 0,
        storeProperties = AlloyJsonUseCaseViews.quoSpecialView.view.storeProperties,
        viewMotif = defaultMotif,
        viewProperties = AlloyJsonUseCaseViews.quoSpecialView.view.viewProperties,
        schemaName = "quo"
      ),

      CatalogCannedView("BurstSupervisorTestView3", "BurstSupervisorTestDomain", 0,
        canned.sampleStoreProperties,
        canned.defaultMotif,
        schemaName = "quo"
      ),

      CatalogCannedView("BurstSupervisorTestView4", "BurstSupervisorTestDomain", 0,
        canned.sampleStoreProperties,
        canned.defaultMotif,
        schemaName = "quo"
      )
    )
  }

}
