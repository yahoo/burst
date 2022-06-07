/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.test

import scala.util.{Failure, Success}

/**
  *
  */
class BurstSiteCatalogSpec extends BurstCatalogSpecSupport {


  "Burst Catalog Sites" should "be available in canned data" in {


    catalogServer.findSiteByMoniker("Site1") match {
      case Failure(t) => throw t
      case Success(entity) =>
        entity.moniker should equal("Site1")
        entity.siteProperties.size should equal(3)
        entity.siteProperties("site_k1") should equal("site_v1")
        entity.siteProperties("site_k2") should equal("site_v2")
        entity.siteProperties("site_k3") should equal("site_v3")

    }

  }

  // TODO add more tests

}
