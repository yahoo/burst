/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.test.synthetic

import org.burstsys.fabric.wave.metadata.model

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class SyntheticSampleStoreExecuteSpec extends SyntheticSampleStoreTestSpecSupport {
  it should "execute eql from synthetic sample store" in {

    val view = supervisorContainer.catalog.findViewByMoniker("BurstSyntheticView").get
    val domain = supervisorContainer.catalog.findDomainByPk(view.domainFk).get

    val eql: String =
      s"""
         |select count(user) as users from schema unity
         |""".stripMargin

    val over = model.over.FabricOver(domain.pk, view.pk)
    val future = supervisorContainer.agent.execute(eql, over, "eql-synthetic-sample-store")
    val result = checkResults(Await.result(future, 10 minutes))

    val names = result.columnNames.zipWithIndex.toMap
    val r = result.rowSet.map {
      row => row(names("users")).asLong
    }
    r should equal(Array(5))
  }
}
