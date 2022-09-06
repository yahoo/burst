/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.master.test.rest

import org.burstsys.client.client.BurstSyncClient
import org.burstsys.client.client.model.BParameter
import org.burstsys.client.client.model.results.BCell
import org.burstsys.dash
import org.burstsys.dash.configuration
import org.burstsys.master.test.support.BurstMasterSpecSupport
import org.burstsys.vitals.uid.newBurstUid

import scala.jdk.CollectionConverters._

class ThriftSpec extends BurstMasterSpecSupport {

  private val schema = "unity"

  private var useHttps = true

  override def beforeAll(): Unit = {
    useHttps = configuration.burstRestUsesHttpsProperty.getOrThrow
    configuration.burstRestUsesHttpsProperty.set(false)
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    configuration.burstRestUsesHttpsProperty.set(useHttps)
  }

  private lazy val client = BurstSyncClient.httpClient(
    dash.configuration.burstRestHostProperty.getOrThrow,
    dash.configuration.burstRestPortProperty.getOrThrow
  )

  private def getFirstRowFromThrift(query: String, args: Option[Array[BParameter]] = None): List[BCell] = {
    val params = args.getOrElse(Array.empty[BParameter]).toList.asJava
    val results = client.executeQuery(newBurstUid, domain.udk.get, views.head.udk.get, query, "UTC", params)
    results.resultSets.size() should equal(1)
    val resultSet = results.resultSets.asScala.head._2
    resultSet.rows.size() should equal(1)
    resultSet.rows.get(0).asScala.toList
  }

  "thrift endpoint" should "execute hydra with no parameters" in {
    val row = getFirstRowFromThrift(s"select cast(10 as long) as vl from schema $schema")
    row.head.datum.longVal() should equal(10)
  }

  it should "execute hydra with one long parameter" in {
    val row = getFirstRowFromThrift(
      s"select(v: long) $$v as vl from schema $schema",
      Some(Array(BParameter.longVal("v", 10)))
    )
    row.head.datum.longVal() should equal(10)
  }

  it should "execute hydra with one integer parameter" in {
    val row = getFirstRowFromThrift(
      s"select (v: integer) $$v as vl from schema $schema",
      Some(Array(BParameter.intVal("v", 10)))
    )
    row.head.datum.intVal() should equal(10)
  }

  ignore should "execute hydra with one string parameter" in {
    val _ = getFirstRowFromThrift(
      "",
      None // Some()
    )
  }

  it should "execute hydra with one double parameter" in {
    val row = getFirstRowFromThrift(
      s"select (v: double) $$v as vl from schema $schema",
      Some(Array(BParameter.doubleVal("v", 10)))

    )
    row.head.datum.doubleVal() should equal(10)
  }

  it should "execute hydra with one boolean parameter" in {
    val row = getFirstRowFromThrift(
      s"select (v: boolean) $$v as vl from schema $schema",
      Some(Array(BParameter.boolVal("v", true)))

    )
    row.head.datum.boolVal() should equal(true)
  }

  it should "execute hydra with multiple parameters" in {
    val row = getFirstRowFromThrift(
      s"select (v1: integer, v2: long, v3: double) $$v1 as one, $$v2 as two, $$v3 as three from schema $schema",
      Some(Array(
        BParameter.intVal("v1", 10),
        BParameter.longVal("v2", 10),
        BParameter.doubleVal("v3", 10)
      ))
    )
    row.head.datum.intVal() should equal(10)
    row(1).datum.longVal() should equal(10)
    row(2).datum.doubleVal() should equal(10)
  }

}
