/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.test.client

import org.burstsys.catalog
import org.burstsys.catalog.model.domain._
import org.burstsys.catalog.model.view._
import org.burstsys.catalog.test.BurstCatalogSpecSupport
import org.joda.time.DateTime

import scala.util.{Failure, Success}

/**
  *
  */
class BurstDomainCatalogSpec extends BurstCatalogSpecSupport {

  it should "have canned data" in {

    catalogServer.findDomainByUdk("udk04") match {
      case Failure(t) => if (!t.toString.contains("udk04 not found")) throw new IllegalStateException("Wrong error msg: " + t.toString)
      case Success(entity) => throw new IllegalStateException("Should not have found udk04")
    }

    catalogServer.findDomainByUdk("udk01") match {
      case Failure(t) => throw t
      case Success(entity) =>
        entity.moniker should equal("Domain1")
        entity.createTimestamp.isDefined should equal(true)
        new DateTime(entity.createTimestamp.get).dayOfYear should equal(DateTime.now.dayOfYear)
        entity.modifyTimestamp.isDefined should equal(true)
        new DateTime(entity.modifyTimestamp.get).dayOfYear should equal(DateTime.now.dayOfYear)
        entity.domainProperties.size should equal(3)
        entity.domainProperties("domain1_k1") should equal("domain1_v1")
        entity.domainProperties("domain1_k2") should equal("domain1_v2")
        entity.domainProperties("domain1_k3") should equal("domain1_v3")
    }

    catalogServer.findDomainByUdk("udk02") match {
      case Failure(t) => throw t
      case Success(entity) =>
        entity.moniker should equal("Domain2")
        entity.createTimestamp.isDefined should equal(true)
        new DateTime(entity.createTimestamp.get).dayOfYear should equal(DateTime.now.dayOfYear)
        entity.modifyTimestamp.isDefined should equal(true)
        new DateTime(entity.modifyTimestamp.get).dayOfYear should equal(DateTime.now.dayOfYear)
        entity.domainProperties.size should equal(3)
        entity.domainProperties("domain2_k1") should equal("domain2_v1")
        entity.domainProperties("domain2_k2") should equal("domain2_v2")
        entity.domainProperties("domain2_k3") should equal("domain2_v3")
    }

    catalogServer.findDomainByMoniker("Domain1") match {
      case Failure(t) => throw t
      case Success(entity) =>
        entity.moniker should equal("Domain1")
        entity.createTimestamp.isDefined should equal(true)
        new DateTime(entity.createTimestamp.get).dayOfYear should equal(DateTime.now.dayOfYear)
        entity.modifyTimestamp.isDefined should equal(true)
        new DateTime(entity.modifyTimestamp.get).dayOfYear should equal(DateTime.now.dayOfYear)
        entity.domainProperties.size should equal(3)
        entity.domainProperties("domain1_k1") should equal("domain1_v1")
        entity.domainProperties("domain1_k2") should equal("domain1_v2")
        entity.domainProperties("domain1_k3") should equal("domain1_v3")

    }
    catalogServer.findDomainByMoniker("Domain2") match {
      case Failure(t) => throw t
      case Success(entity) =>
        entity.moniker should equal("Domain2")
        entity.createTimestamp.isDefined should equal(true)
        new DateTime(entity.createTimestamp.get).dayOfYear should equal(DateTime.now.dayOfYear)
        entity.modifyTimestamp.isDefined should equal(true)
        new DateTime(entity.modifyTimestamp.get).dayOfYear should equal(DateTime.now.dayOfYear)
        entity.domainProperties.size should equal(3)
        entity.domainProperties("domain2_k1") should equal("domain2_v1")
        entity.domainProperties("domain2_k2") should equal("domain2_v2")
        entity.domainProperties("domain2_k3") should equal("domain2_v3")

    }

    catalogServer.searchDomainsByLabel(catalog.cannedDataLabel) match {
      case Failure(t) => throw t
      case Success(entities) =>
        entities.length should be > 0
        for (entity <- entities) {
          entity.labels.isDefined should equal(true)
          entity.labels.get.keys should contain(catalog.cannedDataLabel)
        }
    }

    catalogServer.searchDomainsByLabel(catalog.cannedDataLabel, Some("true")) match {
      case Failure(t) => throw t
      case Success(entities) =>
        entities.length should be > 0
        for (entity <- entities) {
          entity.labels.isDefined should equal(true)
          entity.labels.get.keys should contain(catalog.cannedDataLabel)
          entity.labels.get.values should contain("true")
        }
    }

    catalogServer.searchDomainsByLabel(catalog.cannedDataLabel, Some("false")) match {
      case Failure(t) => throw t
      case Success(entities) =>
        entities.length should equal(0)
    }
  }

  it should "match domain returned by get all views for domain" in {
    val domainId =
      catalogServer.findDomainByMoniker("Domain1") match {
        case Failure(t) => throw t
        case Success(entity) => entity.pk
      }

    catalogServer.allViewsForDomain(domainId) match {
      case Failure(t) => throw t
      case Success(views) =>
        views.length should be > 0
        assert(views.forall(_.domainFk == domainId))
    }
  }

  it should "delete child views" in {

    val domainPk = catalogServer.findDomainByMoniker("Domain1") match {
      case Failure(t) => throw t
      case Success(domain) => domain.pk
    }

    catalogServer.deleteDomain(domainPk) match {
      case Failure(t) => throw t
      case Success(ret) =>
        ret should equal(domainPk)
    }

    catalogServer.allViewsForDomain(domainPk) match {
      case Failure(_) => fail("shoud return empty list of views")
      case Success(views) => views.length should equal(0)
    }
  }

  it should "insert new domain" in {
    val newDomain = CatalogDomain(0, "test domain")
    val domainPk = catalogServer.ensureDomain(newDomain) match {
      case Success(pk) => pk
      case Failure(_) => fail("should insert new domain")
    }


    val domain = catalogServer.findDomainByPk(domainPk) match {
      case Success(d) => d
      case Failure(fe) => throw fe
    }

    domain.pk should equal(domainPk)
  }

  it should "only update the generation clock on views if the domain changes" in {
    val newDomain = CatalogDomain(0, "test domain")
    val domainPk = catalogServer.ensureDomain(newDomain) match {
      case Success(pk) => pk
      case Failure(_) => fail("should insert new domain")
    }


    val domain = catalogServer.findDomainByPk(domainPk) match {
      case Success(d) => d
      case Failure(cause) => throw cause
    }

    domain.pk should equal(domainPk)

    val newView = CatalogView(0, "test view", domainPk, "unity")
    val viewPk = catalogServer.insertView(newView) match {
      case Success(pk) => pk
      case Failure(_) => fail("should insert new view")
    }

    val view = catalogServer.findViewByPk(viewPk) match {
      case Success(v) => v
      case Failure(_) => fail("could not retrieve existing view")
    }

    catalogServer.ensureDomain(domain) match {
      case Success(_) =>
      case Failure(_) => fail("should upsert domain")
    }

    val viewPreUpdate = catalogServer.findViewByPk(viewPk) match {
      case Success(v) => v
      case Failure(_) => fail("could not retrieve existing view")
    }

    // generation clock unchanged because the domain is unchanged
    view.generationClock should equal(viewPreUpdate.generationClock)

    val moniker = "new moniker"
    catalogServer.ensureDomain(domain.copy(moniker = moniker)) match {
      case Success(_) =>
      case Failure(_) => fail("should upsert domain")
    }

    val updatedDomain = catalogServer.findDomainByPk(domainPk) match {
      case Success(d) => d
      case Failure(cause) => throw cause
    }

    updatedDomain.moniker should equal(moniker)

    val viewPostUpdate = catalogServer.findViewByPk(viewPk) match {
      case Success(v) => v
      case Failure(_) => fail("could not retrieve existing view")
    }

    // generation clock changed because the domain changed
    view.generationClock should not equal viewPostUpdate.generationClock
  }

  "ensureDomain" should "preserve unspecified attributes" in {
    val domainUdk = "testEnsureDomainUdk"
    val domainMoniker = "testEnsureDomainMoniker"

    val domain1 = CatalogDomain(0, domainMoniker, Map("domainProp" -> "foo"), Some(domainUdk))
    val domain1Pk = catalogServer.ensureDomain(domain1) match {
      case Success(pk) => pk
      case Failure(_) => fail("should insert new domain")
    }

    val domain2 = CatalogDomain(0, "", udk = Some(domainUdk))
    val domain2Pk = catalogServer.ensureDomain(domain2) match {
      case Success(pk) => pk
      case Failure(_) => fail("should ensure domain still exists")
    }

    domain1Pk should equal(domain2Pk)

    val fetched = catalogServer.findDomainByPk(domain1Pk) match {
      case Failure(_) => fail("should look up domain by pk")
      case Success(domain) => domain
    }

    fetched.moniker should equal(domainMoniker)
    fetched.domainProperties should contain ("domainProp" -> "foo")
  }

  it should "merge property maps" in {
    val domainUdk = "testEnsureDomain2Udk"
    val domainMoniker = "testEnsureDomain2Moniker"

    val domain1 = CatalogDomain(0, domainMoniker, Map("a" -> "foo", "b" -> "bar"), Some(domainUdk))
    val domain1Pk = catalogServer.ensureDomain(domain1) match {
      case Success(pk) => pk
      case Failure(_) => fail("should insert new domain")
    }

    val domain2 = CatalogDomain(0, "", Map("a" -> "bar", "c" -> "baz"), udk = Some(domainUdk))
    val domain2Pk = catalogServer.ensureDomain(domain2) match {
      case Success(pk) => pk
      case Failure(_) => fail("should ensure domain still exists")
    }

    domain1Pk should equal(domain2Pk)

    val fetched = catalogServer.findDomainByPk(domain1Pk) match {
      case Failure(_) => fail("should look up domain by pk")
      case Success(domain) => domain
    }

    fetched.moniker should equal(domainMoniker)
    fetched.domainProperties should contain ("a" -> "bar")
    fetched.domainProperties should contain ("b" -> "bar")
    fetched.domainProperties should contain ("c" -> "baz")
  }
}
