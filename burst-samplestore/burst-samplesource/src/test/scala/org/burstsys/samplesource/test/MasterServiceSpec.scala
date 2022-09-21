/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource.test

import org.burstsys.samplesource.service.SampleSourceMasterService
import org.burstsys.samplestore.api
import org.burstsys.samplestore.api.BurstSampleStoreDataSource
import org.burstsys.samplestore.api.BurstSampleStoreDomain
import org.burstsys.samplestore.api.BurstSampleStoreView
import org.burstsys.samplestore.api.SampleStoreGeneration
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.vitals.properties.VitalsPropertyMap

import scala.concurrent.Future

class MasterServiceSpec extends BaseSpec {

  case class ExampleMasterService() extends SampleSourceMasterService {
    override def name: String = "Example"

    override def getViewGenerator(guid: String, dataSource: BurstSampleStoreDataSource, listenerProperties: VitalsPropertyMap): Future[api.SampleStoreGeneration] = {
      TeslaRequestFuture {
        SampleStoreGeneration(guid, "", Array.empty, "", Some(""))
      }
    }
  }

  it should "merge properties" in {
    // a set of properties of the form `[origin]-[overrider]`
    val viewStoreProps = Array("all", "store-listener", "store-domain", "store-view", "store-store").map((_, "1")).toMap
    val listenerProps = Array("all", "store-listener", "listener-domain", "listener-view", "listener-listener").map((_, "2")).toMap
    val domainProps = Array("all", "store-domain", "listener-domain", "domain-view", "domain-domain").map((_, "3")).toMap
    val viewViewProps = Array("all", "store-view", "listener-view", "domain-view", "view-view").map((_, "4")).toMap
    val datasource = BurstSampleStoreDataSource(
      BurstSampleStoreDomain(1, domainProps),
      BurstSampleStoreView(1, "", "", viewStoreProps, viewViewProps)
    )
    val merged = ExampleMasterService().mergeProperties(datasource, listenerProps)
    merged should equal(Map(
      "all" -> "4", "store-view" -> "4", "listener-view" -> "4", "domain-view" -> "4", "view-view" -> "4",
      "store-domain" -> "3", "listener-domain" -> "3", "domain-domain" -> "3",
      "store-listener" -> "2", "listener-listener" -> "2",
      "store-store" -> "1"
    ))
  }
}
