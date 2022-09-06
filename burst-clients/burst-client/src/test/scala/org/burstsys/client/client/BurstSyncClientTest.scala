/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.client.client

import org.burstsys.client.BaseClientTest
import org.burstsys.client.StubBurstService
import org.burstsys.client.client.exception.BurstRequestException
import org.burstsys.gen.thrift.api.client.BTBurstService
import org.burstsys.gen.thrift.api.client.BTRequestOutcome
import org.burstsys.gen.thrift.api.client.BTResultStatus
import org.burstsys.gen.thrift.api.client.domain.BTDomain
import org.burstsys.gen.thrift.api.client.domain.BTDomainResponse
import org.burstsys.gen.thrift.api.client.view.BTView
import org.burstsys.gen.thrift.api.client.view.BTViewResponse

import java.util
import scala.jdk.CollectionConverters._

class BurstSyncClientTest extends BaseClientTest {

  private val view1Udk = "view1Udk"
  private val domainWithViewsUdk = "domainWithViews"
  private val domainWithNoViewsUdk = "domainWithNoViewsUdk"

  private val view1 = new BTView(view1Udk, "view1", domainWithViewsUdk, emptyStringMap, "", emptyStringMap, emptyStringMap, "")

  private val domainWithViews = {
    val d = new BTDomain(domainWithViewsUdk, "Found Domain", emptyStringMap, emptyStringMap)
    d.setViews(Array(view1).toList.asJava)
    d
  }

  val handler: StubBurstService = new StubBurstService() {
    override def findDomain(udk: String): BTDomainResponse =
      udk match {
        case `domainWithViewsUdk` =>
          val r = new BTDomainResponse(new BTRequestOutcome(BTResultStatus.SuccessStatus, "Mock Success"), emptyStringMap)
          r.setDomain(domainWithViews)
          r
        case _ =>
          new BTDomainResponse(new BTRequestOutcome(BTResultStatus.NotFound, "Mock NotFound"), emptyStringMap)
      }

    /**
     * listViewsInDomain returns any views defined in the specified domain.
     *
     * @param domainUdk the udk of the domain containing the views to be returned.
     */
    override def listViewsInDomain(domainUdk: String): BTViewResponse =
      domainUdk match {
        case `domainWithViewsUdk` =>
          val r = new BTViewResponse(new BTRequestOutcome(BTResultStatus.SuccessStatus, "Mock Success"), emptyStringMap)
          r.setViews(Array(view1).toList.asJava)
          r
        case `domainWithNoViewsUdk` =>
          val r = new BTViewResponse(new BTRequestOutcome(BTResultStatus.SuccessStatus, "Mock Success"), emptyStringMap)
          r.setViews(new util.ArrayList())
          r
        case _ =>
          new BTViewResponse(new BTRequestOutcome(BTResultStatus.NotFound, "Mock Success"), emptyStringMap)
      }
  }

  override def syncHander: Option[BTBurstService.Iface] = Some(handler)

  private def newClient: BurstSyncClient = BurstSyncClient.httpClient("localhost")

  "findDomain" should "return a domain when the domain exists" in {
    val client = newClient
    val response = client.findDomain(domainWithViewsUdk)
    response.isPresent should be(true)
    val domain = response.get
    domain.getUdk should be(domainWithViewsUdk)
  }

  it should "return empty when the domain does not exist" in {
    val client = newClient
    val response = client.findDomain("bogusUdk")
    response.isPresent should be(false)
  }

  "listViewsInDomain" should "return views in domain when the domain has views" in {
    val client = newClient
    val response = client.listViewsInDomain(domainWithViewsUdk)
    response.size() > 0 should be(true)
  }

  it should "return an empty list when the domain has no views" in {
    val client = newClient
    val response = client.listViewsInDomain(domainWithViewsUdk)
    response.size() > 0 should be(true)
  }

  it should "throw an exception when the domain does not exist" in {
    val client = newClient
    a [BurstRequestException] should be thrownBy client.listViewsInDomain("bogusUdk")
  }
}
