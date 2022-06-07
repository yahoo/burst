/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.test.client

import org.burstsys.catalog
import org.burstsys.catalog.api.BurstCatalogApiQueryLanguageType
import org.burstsys.catalog.test.BurstCatalogSpecSupport

import scala.util.Failure
import scala.util.Success

/**
  *
  */
class BurstQueryCatalogSpec extends BurstCatalogSpecSupport {

  "Burst Catalog Queries" should "be available in canned data" in {

    catalogServer.findQueryByMoniker("EQL count of users, sessions, events") match {
      case Failure(t) => throw t
      case Success(entity) => entity.moniker should equal("EQL count of users, sessions, events")
    }

    catalogServer.allQueries() match {
      case Failure(t) => throw t
      case Success(queries) =>
        queries.map(_.languageType) should contain allOf(
          BurstCatalogApiQueryLanguageType.Eql,
          BurstCatalogApiQueryLanguageType.Hydra
        )
    }

    catalogServer.searchQueriesByLabel(catalog.cannedDataLabel) match {
      case Failure(t) => throw t
      case Success(entities) =>
        for (entity <- entities) {
          entity.labels.isDefined should equal(true)
          entity.labels.get.keys should contain(catalog.cannedDataLabel)
        }
    }
  }

}
