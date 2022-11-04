/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.test.client

import org.burstsys.catalog.model.domain._
import org.burstsys.catalog.model.view._
import org.burstsys.catalog.test.BurstCatalogSpecSupport
import org.burstsys.{catalog, fabric}
import org.joda.time.DateTime

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

class BurstViewCatalogSpec extends BurstCatalogSpecSupport {

  it should "have canned data" in {

    catalogServer.findViewByMoniker("Domain1View1") match {
      case Failure(t) => throw t
      case Success(entity) =>
        entity.moniker should equal("Domain1View1")
        entity.createTimestamp.isDefined should equal(true)
        new DateTime(entity.createTimestamp.get).dayOfYear should equal(DateTime.now.dayOfYear)
        entity.modifyTimestamp.isDefined should equal(true)
        new DateTime(entity.modifyTimestamp.get).dayOfYear should equal(DateTime.now.dayOfYear)
        entity.generationClock should not be <=(0)
        entity.generationClock should not be >(System.currentTimeMillis())
      /*
              entity.viewProperties.size should equal(3)
              entity.viewProperties("view1_k1") should equal("view1_v1")
              entity.viewProperties("view1_k2") should equal("view1_v2")
              entity.viewProperties("view1_k3") should equal("view1_v3")
      */

    }
    catalogServer.findViewByMoniker("Domain1View2") match {
      case Failure(t) => throw t
      case Success(entity) =>
        entity.moniker should equal("Domain1View2")
        entity.createTimestamp.isDefined should equal(true)
        new DateTime(entity.createTimestamp.get).dayOfYear should equal(DateTime.now.dayOfYear)
        entity.modifyTimestamp.isDefined should equal(true)
        new DateTime(entity.modifyTimestamp.get).dayOfYear should equal(DateTime.now.dayOfYear)
      /*
              entity.viewProperties.size should equal(3)
              entity.viewProperties("view2_k1") should equal("view2_v1")
              entity.viewProperties("view2_k2") should equal("view2_v2")
              entity.viewProperties("view2_k3") should equal("view2_v3")
      */
    }

    catalogServer.searchViewsByLabel(catalog.cannedDataLabel) match {
      case Failure(t) => throw t
      case Success(entities) =>
        for (entity <- entities) {
          entity.labels.isDefined should equal(true)
          entity.labels.get.keys should contain(catalog.cannedDataLabel)
        }
    }
  }

  it should "be available as part of it's domain's views" in {
    val (viewId, domainId) =
      catalogServer.findViewByMoniker("Domain1View2") match {
        case Failure(t) => throw t
        case Success(entity) =>
          (entity.pk, entity.domainFk)
      }

    catalogServer.allViewsForDomain(domainId) match {
      case Failure(t) => throw t
      case Success(views) =>
        views.map(_.pk) should contain(viewId)
    }
  }

  it should "update generations time when modified" in {
    val originalView = catalogServer.findViewByMoniker("Domain1View1") match {
      case Failure(t) => throw t
      case Success(entity) => entity
    }

    // update the view
    val changedView = originalView.copy(storeProperties = originalView.storeProperties.concat(Array("test" -> "value")))
    catalogServer.ensureView(changedView) match {
      case Failure(t) => throw t
      case Success(_) =>
    }

    val recordedView = catalogServer.findViewByPk(originalView.pk) match {
      case Failure(t) => throw t
      case Success(entity) => entity
    }

    recordedView.generationClock should be >(originalView.generationClock)
    recordedView.generationClock should not be >(System.currentTimeMillis())
  }

  it should "not update generations time when unmodified" in {
    val originalView = catalogServer.findViewByMoniker("Domain1View1") match {
      case Failure(t) => throw t
      case Success(entity) => entity
    }

    // invoke updateView with and unmodified view
    catalogServer.ensureView(originalView) match {
      case Failure(t) => throw t
      case Success(_) =>
    }

    val recordedView = catalogServer.findViewByPk(originalView.pk) match {
      case Failure(t) => throw t
      case Success(entity) => entity
    }

    recordedView.generationClock should equal(originalView.generationClock)
  }

  it should "update generations time when parent domain is modified" in {
    val originalDomain =
      catalogServer.findDomainByMoniker("Domain1") match {
        case Failure(t) => throw t
        case Success(entity) => entity
      }

    val originalViews = catalogServer.allViewsForDomain(originalDomain.pk) match {
      case Failure(t) => throw t
      case Success(entity) => entity
    }

    // update the domain
    val changedDomain = originalDomain.copy(domainProperties = originalDomain.domainProperties.concat(Array("test" -> "value")))
    catalogServer.ensureDomain(changedDomain) match {
      case Failure(t) => throw t
      case Success(_) =>
    }

    // no go through the views and make sure the generation clock has been updated
    for (ov <- originalViews) {
      val fetched = catalogServer.findViewByPk(ov.pk) match {
        case Failure(t) => throw t
        case Success(entity) => entity
      }
      fetched.generationClock should be > ov.generationClock
      fetched.generationClock should be <= System.currentTimeMillis()

    }
  }

  it should "record access and update view properties" in {
    val view =
      catalogServer.findViewByMoniker("Domain1View2") match {
        case Failure(t) => throw t
        case Success(entity) => entity
      }

    var updatedProperties = view.viewProperties.concat(Array("view_update" -> "1"))
    catalogServer.recordViewLoad(view.pk, updatedProperties) match {
      case Failure(t) => throw t
      case Success(_) =>
    }

    val accessTs = catalogServer.findViewByPk(view.pk) match {
      case Failure(t) => throw t
      case Success(entity) =>
        entity.viewProperties should equal(updatedProperties)
        entity.accessTimestamp.isDefined should equal(true)
        entity.accessTimestamp.get
    }

    // Make damn sure that the new access time will be later than accessTs
    Thread.sleep(250)

    updatedProperties = updatedProperties.concat(Array("view_update" -> "2"))
    catalogServer.recordViewLoad(view.pk, updatedProperties) match {
      case Failure(t) => throw t
      case Success(_) =>
    }

    catalogServer.findViewByPk(view.pk) match {
      case Failure(t) => throw t
      case Success(entity) =>
        entity.viewProperties should equal(updatedProperties)
        entity.accessTimestamp.isDefined should equal(true)
        entity.accessTimestamp.get should be > accessTs
    }
  }

  it should "expire genclock after stale" in {

    val oldDuration = catalog.configuration.burstCatalogGenerationStaleMsProperty.getOrThrow
    val view = {
      catalogServer.findViewByMoniker("Domain1View2") match {
        case Failure(t) => throw t
        case Success(entity) => entity
      }
    }
    val oldGenClock = view.generationClock

    catalog.configuration.burstCatalogGenerationStaleMsProperty.set((1 seconds).toMillis)
    Thread.sleep((2 seconds).toMillis)

    val accessTs = catalogServer.findViewByPk(view.pk) match {
      case Failure(t) => throw t
      case Success(entity) =>
        entity.generationClock should not equal (oldGenClock)
    }

    catalog.configuration.burstCatalogGenerationStaleMsProperty.set(oldDuration)
  }

  it should "delete child views" in {
    val domainId =
      catalogServer.findDomainByMoniker("Domain1") match {
        case Failure(t) => throw t
        case Success(entity) => entity.pk
      }

    catalogServer.deleteViewsForDomain(domainId) match {
      case Failure(t) => throw t
      case Success(ret) =>
        ret should equal(domainId)
    }

    catalogServer.allViewsForDomain(domainId) match {
      case Failure(t) => throw t
      case Success(views) =>
        views.length should equal(0)
    }
  }

  "ensureView" should "work when domainFk is specified" in {
    val viewUdk = "ensureViewTest1"
    val domainPk = 2

    val toInsert = CatalogView(0, "ensureViewTest1", domainPk, "quo", udk = Some(viewUdk))
    val viewPk = catalogServer.ensureView(toInsert) match {
      case Failure(t) => throw t
      case Success(pk) => pk
    }

    val fetched = catalogServer.findViewByPk(viewPk) match {
      case Failure(_) => fail("could not find inserted view")
      case Success(fetched) => fetched
    }

    fetched.pk shouldBe viewPk
    fetched.domainFk shouldBe 2
    fetched.schemaName shouldBe "quo"
    fetched.udk shouldBe Some(viewUdk)
  }

  it should "fail when domainFk is missing" in {
    val viewUdk = "ensureViewTest2"
    val toInsert = CatalogView(0, "ensureViewTest1", 0, "quo", udk = Some(viewUdk))
    catalogServer.ensureView(toInsert) match {
      case Failure(_) => // failure is success in this test
      case Success(pk) => fail("success without domainFk")
    }
  }

  "ensureViewInDomain" should "preserve unspecified fields" in {
    val viewUdk = "ensureViewInDomainTest1"
    val domainPk = 2
    val domainUdk = catalogServer.findDomainByPk(domainPk) match {
      case Failure(t) => throw t
      case Success(domain) => domain.udk.get
    }

    val storeProps = Map("store_prop_1" -> "foo")
    val viewProps = Map("view_prop_1" -> "foo")
    val labels = Some(Map("label_1" -> "foo"))
    val toInsert = CatalogView(0, moniker = viewUdk, domainPk, "quo", generationClock = 10, storeProps, "view v {}", viewProps, labels, Some(viewUdk))

    val (computedGenClock, computedViewProps) = catalogServer.ensureViewInDomain(domainUdk, toInsert) match {
      case Failure(t) => throw t
      case Success(view) => (view.generationClock, view.viewProperties)
    }

    val empty = CatalogView(0, "", 0, "", udk = Some(viewUdk))
    val ensured = catalogServer.ensureViewInDomain(domainUdk, empty) match {
      case Failure(exception) => throw exception
      case Success(view) => view
    }

    ensured.moniker shouldBe viewUdk
    ensured.domainFk shouldBe domainPk
    ensured.schemaName shouldBe "quo"
    ensured.generationClock shouldBe computedGenClock
    ensured.storeProperties shouldBe storeProps
    ensured.viewMotif shouldBe "view v {}"
    ensured.viewProperties shouldBe computedViewProps
    ensured.labels shouldBe labels
    ensured.udk shouldBe Some(viewUdk)
  }

  it should "merge prop maps" in {
    val viewUdk = "ensureViewInDomainTest2"
    val domainPk = 2
    val domainUdk = catalogServer.findDomainByPk(domainPk) match {
      case Failure(t) => throw t
      case Success(domain) => domain.udk.get
    }

    val storeProps = Map("store_prop_1" -> "foo", "store_prop_2" -> "bar")
    val viewProps = Map("view_prop_1" -> "foo", "view_prop_2" -> "bar")
    val labels = Some(Map("label_1" -> "foo", "label_2" -> "bar"))
    val toInsert = CatalogView(0, moniker = viewUdk, domainPk, "quo", storeProperties = storeProps, viewProperties = viewProps, labels = labels, udk = Some(viewUdk))

    catalogServer.ensureViewInDomain(domainUdk, toInsert) match {
      case Failure(t) => throw t
      case Success(_) =>
    }

    val newStoreProps = Map("store_prop_1" -> "bar", "store_prop_3" -> "baz")
    val newViewProps = Map("view_prop_1" -> "bar", "view_prop_3" -> "baz")
    val newLabels = Map("label_1" -> "bar", "label_3" -> "")
    val updateMaps = CatalogView(0, "", 0, "", storeProperties = newStoreProps, viewProperties = newViewProps, labels = Some(newLabels), udk = Some(viewUdk))
    val ensured = catalogServer.ensureViewInDomain(domainUdk, updateMaps) match {
      case Failure(exception) => throw exception
      case Success(view) => view
    }

    ensured.moniker shouldBe viewUdk
    ensured.domainFk shouldBe domainPk
    ensured.schemaName shouldBe "quo"
    ensured.storeProperties shouldBe Map("store_prop_1" -> "bar", "store_prop_2" -> "bar", "store_prop_3" -> "baz")
    ensured.viewProperties shouldBe Map("view_prop_1" -> "bar", "view_prop_2" -> "bar", "view_prop_3" -> "baz", fabric.wave.metadata.ViewEarliestLoadAtProperty -> s"${ensured.generationClock}")
    ensured.labels shouldBe Some(Map("label_1" -> "bar", "label_2" -> "bar"))
    ensured.udk shouldBe Some(viewUdk)
  }

  it should "still work when the view's pk is provided" in {
    val viewUdk = "ensureViewInDomainTest2"
    val domainPk = 2
    val domainUdk = catalogServer.findDomainByPk(domainPk) match {
      case Failure(t) => throw t
      case Success(domain) => domain.udk.get
    }

    val storeProps = Map("store_prop_1" -> "foo", "store_prop_2" -> "bar")
    val viewProps = Map("view_prop_1" -> "foo", "view_prop_2" -> "bar")
    val labels = Some(Map("label_1" -> "foo", "label_2" -> "bar"))
    val toInsert = CatalogView(0, moniker = viewUdk, domainPk, "quo", storeProperties = storeProps, viewProperties = viewProps, labels = labels, udk = Some(viewUdk))

    val inserted = catalogServer.ensureViewInDomain(domainUdk, toInsert) match {
      case Failure(t) => throw t
      case Success(view) => view
    }

    val newStoreProps = Map("store_prop_1" -> "bar", "store_prop_3" -> "baz")
    val newViewProps = Map("view_prop_1" -> "bar", "view_prop_3" -> "baz")
    val newLabels = Map("label_1" -> "bar", "label_3" -> "")
    val updateMaps = CatalogView(inserted.pk, "", 0, "", storeProperties = newStoreProps, viewProperties = newViewProps, labels = Some(newLabels), udk = Some(viewUdk))
    val ensured = catalogServer.ensureViewInDomain(domainUdk, updateMaps) match {
      case Failure(exception) => throw exception
      case Success(view) => view
    }

    ensured.moniker shouldBe viewUdk
    ensured.domainFk shouldBe domainPk
    ensured.schemaName shouldBe "quo"
    ensured.storeProperties shouldBe Map("store_prop_1" -> "bar", "store_prop_2" -> "bar", "store_prop_3" -> "baz")
    ensured.viewProperties shouldBe Map("view_prop_1" -> "bar", "view_prop_2" -> "bar", "view_prop_3" -> "baz", fabric.wave.metadata.ViewEarliestLoadAtProperty -> s"${ensured.generationClock}")
    ensured.labels shouldBe Some(Map("label_1" -> "bar", "label_2" -> "bar"))
    ensured.udk shouldBe Some(viewUdk)
  }
}
