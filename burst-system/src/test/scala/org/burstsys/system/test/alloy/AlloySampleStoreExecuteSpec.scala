/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.test.alloy

import org.burstsys.fabric.metadata.model

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class AlloySampleStoreExecuteSpec extends BurstAlloySampleStoreTestSpecSupport {
  it should "execute eql from alloy sample store" in {

    val view = masterContainer.catalog.findViewByMoniker("BurstAlloyView").get

    val eql: String =
      s"""
         |select count(user) as users from schema quo
         |""".stripMargin

    val over = model.over.FabricOver(domain.pk, view.pk)
    val future = masterContainer.agent.execute(eql, over, "eql-alloy-sample-store")
    val result = checkResults(Await.result(future, 10 minutes))

    val names = result.columnNames.zipWithIndex.toMap
    val r = result.rowSet.map {
      row => row(names("users")).asLong
    }
    r should equal(Array(26))
  }
}
