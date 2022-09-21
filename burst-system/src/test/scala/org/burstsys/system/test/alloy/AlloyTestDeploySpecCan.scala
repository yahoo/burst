/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.test.alloy

import org.burstsys.alloy.alloy.getResourceFile
import org.burstsys.catalog.canned
import org.burstsys.catalog.canned.CatalogCan
import org.burstsys.catalog.model.domain.CatalogCannedDomain
import org.burstsys.catalog.model.view.CatalogCannedView
import org.burstsys.json.samplestore.{JsonBrioSampleSourceName, JsonBrioSampleSourceVersion}

import java.nio.file.Path

final class AlloyTestDeploySpecCan extends CatalogCan {
  val alloyJsonFile: Path = getResourceFile("quo/quo-view.json.gz")

  override def domains: Array[CatalogCannedDomain] = {
    Array(CatalogCannedDomain("BurstAlloyTestDomain"))
  }

  override def views: Array[CatalogCannedView] = {
    Array(
      CatalogCannedView("BurstAlloyView", "BurstAlloyTestDomain", 0,
        Map(
          "burst.store.name" -> "sample",
          "burst.samplestore.source.name" -> JsonBrioSampleSourceName,
          "burst.samplestore.source.version" -> JsonBrioSampleSourceVersion,
          "json.samplestore.location" -> s"$alloyJsonFile"),
        canned.defaultMotif,
        schemaName = "quo"
      )
    )
  }

}
