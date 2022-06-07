/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.relate.test

import org.burstsys.relate.test.model.BurstSqlTestEntity
import org.burstsys.relate.test.support.{BurstSqlSpecLog, BurstSqlSpecSupport}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 *
 */
class BurstSqlSpec extends AnyFlatSpec with Matchers with BurstSqlSpecLog with BurstSqlSpecSupport {

  "Burst SQL" should "succeed" in {
    sqlTest {
      persister =>
        persister.service.connection localTx {
          implicit session =>
            persister.updateEntity(BurstSqlTestEntity(100L, "Not exists"))
            log info s"testing..."
            val key1 = persister.insertEntity(BurstSqlTestEntity(test1 = "hello"))
            key1 should equal(1)
            val list1 = persister.fetchAllEntities()
            list1.size should equal(1)

            val key2 = persister.insertEntity(BurstSqlTestEntity(test1 = "goodbye"))
            key2 should equal(2)
            val list2 = persister.fetchAllEntities()
            list2.size should equal(2)
            list2(0).pk should equal(1)
            list2(0).test1 should equal("hello")
            list2(1).pk should equal(2)
            list2(1).test1 should equal("goodbye")

            list2(1).test1 = "new_string"
            persister.updateEntity(list2(1))
            persister.findEntityByPk(list2(1).pk) match {
              case None => throw new RuntimeException
              case Some(e) =>
                e.pk should equal(2)
                e.test1 should equal("new_string")
            }

            persister.deleteEntity(list2(1).pk)
            val list3 = persister.fetchAllEntities()
            list3.size should equal(1)
            list3(0).pk should equal(1)
            list3(0).test1 should equal("hello")

            (persister.findEntityByPk(2) match {
              case None => true
              case Some(e) => false
            }) should equal(true)

        }

    }
  }

}
