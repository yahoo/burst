/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.test.sql

import java.util.UUID

import org.burstsys.catalog.test._
import org.burstsys.catalog.test.support.{TestUdkEntity, TestUdkEntityPersister}
import org.burstsys.relate.RelateExceptions.{BurstDuplicateKeyException, BurstUnknownPrimaryKeyException}
import org.burstsys.relate.RelateService
import org.burstsys.relate.provider.RelateMockProvider
import org.burstsys.tesla.thread.request.TeslaRequestFuture

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class UdkCatalogEntitySpec extends BurstCatalogSqlSpecSupport {

  var sql: RelateService = _
  var persister: TestUdkEntityPersister = _
  var moniker: String = _
  var udk: String = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    sql = RelateMockProvider().start
    persister = TestUdkEntityPersister(sql)
    sql.registerPersister(persister)
    persister.connection localTx {
      implicit session => persister.createTable
    }
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    moniker = UUID.randomUUID.toString
    udk = UUID.randomUUID.toString
    persister.waitForDuplicateInsert = false
  }

  override def afterAll(): Unit = {
    persister.connection localTx {
      implicit session => persister.dropTable
    }
    sql.stop
    super.afterAll()
  }

  it should "insert entities with no pk and no udk" in {
    val entity: TestUdkEntity = TestUdkEntity(moniker = moniker)
    val (upserted, sqlExecuted) = persister.inTx { implicit s => persister.upsertEntity(entity) }

    upserted.pk should be > 0L
    sqlExecuted shouldBe true

    log info s"Inserted entity with pk $upserted"
    persister.inTx { implicit s => persister.findEntityByPk(upserted.pk) } match {
      case None => fail("Could not fetch persisted entity by pk")
      case Some(fetched) => entity.copy(pk = upserted.pk) should equal(fetched)
    }
  }

  it should "fail to insert entities with no pk that violate other constraints" in {
    val entity = TestUdkEntity(moniker = moniker)

    persister.inTx { implicit s => persister.upsertEntity(entity) }
    the[BurstDuplicateKeyException] thrownBy {
      persister.inTx { implicit s => persister.upsertEntity(entity) }
    } should have message "Cannot update or insert entity"
  }

  it should "insert an entity by udk when the udk does not exist" in {
    val entity: TestUdkEntity = TestUdkEntity(udk = Some(udk), moniker = moniker)
    val (upserted, sqlExecuted) = persister.inTx { implicit s => persister.upsertEntity(entity) }

    upserted.pk should be > 0L
    sqlExecuted shouldBe true

    persister.inTx { implicit s => persister.findEntityByUdk(udk) } match {
      case None => fail("Could not look up entity by UDK")
      case Some(fetched) => entity.copy(pk = upserted.pk) should equal(fetched)
    }
  }

  it should "update an entity by udk when the udk exists" in {
    val entity: TestUdkEntity = TestUdkEntity(udk = Some(udk), moniker = moniker)
    val (upserted1, sqlExecuted1) = persister.inTx { implicit s => persister.upsertEntity(entity) }

    upserted1.pk should be > 0L
    sqlExecuted1 shouldBe true

    val updated = entity.copy(moniker = s"$moniker$moniker")
    val (upserted2, sqlExecuted2) = persister.inTx { implicit s => persister.upsertEntity(updated) }

    upserted2.pk shouldBe upserted1.pk
    sqlExecuted2 shouldBe true

    persister.inTx { implicit s => persister.findEntityByUdk(udk) } match {
      case None => fail("Could not look up entity by UDK")
      case Some(fetched) => updated.copy(pk = upserted1.pk) should equal(fetched)
    }
  }

  it should "do nothing if no changes are required" in {
    val entity: TestUdkEntity = TestUdkEntity(udk = Some(udk), moniker = moniker)
    val (upserted1, sqlExecuted1) = persister.inTx { implicit s => persister.upsertEntity(entity) }

    upserted1.pk should be > 0L
    sqlExecuted1 shouldBe true

    val (upserted2, sqlExecuted2) = persister.inTx { implicit s => persister.upsertEntity(entity) }
    upserted2.pk shouldBe upserted1.pk
    sqlExecuted2 shouldBe false

    val (upserted3, sqlExecuted3) = persister.inTx { implicit s => persister.upsertEntity(entity.copy(pk = upserted1.pk)) }
    upserted3.pk shouldBe upserted1.pk
    sqlExecuted3 shouldBe false
  }

  it should "update an entity when a pk is provided" in {
    val entity: TestUdkEntity = TestUdkEntity(udk = Some(udk), moniker = moniker)
    val (upserted1, sqlExecuted1) = persister.inTx { implicit s => persister.upsertEntity(entity) }

    upserted1.pk should be > 0L
    sqlExecuted1 shouldBe true

    val updated = entity.copy(pk = upserted1.pk, moniker = s"$moniker$moniker")
    val (upserted2, sqlExecuted2) = persister.inTx { implicit s => persister.upsertEntity(updated) }

    upserted2.pk shouldBe upserted1.pk
    sqlExecuted2 shouldBe true

    persister.inTx { implicit s => persister.findEntityByPk(upserted1.pk) } match {
      case None => fail("Could not look up entity by pk")
      case Some(fetched) => updated.copy(pk = upserted1.pk) should equal(fetched)
    }
  }

  it should "fail to update an entity when the pk provided does not exist" in {
    val entity = TestUdkEntity(pk = 99999, moniker = moniker)

    the[BurstUnknownPrimaryKeyException] thrownBy {
      persister.inTx { implicit s => persister.upsertEntity(entity) }
    } should have message s"Referenced primary key not found [${entity.pk}]"
  }

  it should "update an entity's udk when both udk and pk are provided" in {
    val entity: TestUdkEntity = TestUdkEntity(udk = Some(udk), moniker = moniker)
    val (upserted1, sqlExecuted1) = persister.inTx { implicit s => persister.upsertEntity(entity) }

    upserted1.pk should be > 0L
    sqlExecuted1 shouldBe true

    val updated = entity.copy(pk = upserted1.pk, moniker = s"$moniker$moniker", udk = Some(s"$udk$udk"))
    val (upserted2, sqlExecuted2) = persister.inTx { implicit s => persister.upsertEntity(updated) }

    upserted2.pk shouldBe upserted1.pk
    sqlExecuted2 shouldBe true

    persister.inTx { implicit s => persister.findEntityByPk(upserted1.pk) } match {
      case None => fail("Could not look up entity by pk")
      case Some(fetched) => updated.copy(pk = upserted1.pk) should equal(fetched)
    }
  }

  it should "not fail a second, concurrent, attempt to insert a new entity by udk" in {
    val entity: TestUdkEntity = TestUdkEntity(udk = Some(udk), moniker = moniker)

    persister.waitForDuplicateInsert = true
    val upserts = (1 to 100).indices.map({ _ =>
      TeslaRequestFuture {
        persister.inTx { implicit s => persister.upsertEntity(entity) }
      }
    }).map(Await.result(_, 10 seconds)).toArray

    upserts.filter(_._2) should have length 1
    val upserted = upserts.filter(_._2)(0)._1
    upserts.filter(!_._2).foreach(_._1 should equal(upserted))

    persister.inTx { implicit s => persister.findEntityByUdk(udk) } match {
      case None => fail("Could not look up entity by UDK")
      case Some(fetched) => entity.copy(pk = upserted.pk) should equal(fetched)
    }
  }

}
