/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.api

import org.apache.logging.log4j.Logger
import org.burstsys.samplestore.api.client.SampleStoreApiClient
import org.burstsys.samplestore.api.server.SampleStoreApiServer
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.vitals.VitalsService.VitalsStandaloneServer
import org.burstsys.vitals.VitalsService.VitalsStandardClient
import org.burstsys.vitals.logging.VitalsLog
import org.burstsys.vitals.logging.VitalsLogger
import org.burstsys.vitals.metrics.VitalsMetricsRegistry
import org.burstsys.vitals.net.getLocalHostAddress
import org.burstsys.vitals.net.getLocalHostName
import org.burstsys.vitals.properties._
import org.burstsys.vitals.uid.newBurstUid
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

package object test extends VitalsLogger {

  val mockStoreProperties: VitalsPropertyMap = Map(
    SampleStoreSourceNameProperty -> "mocksource",
    SampleStoreSourceVersionProperty -> "0.1"
  )

  trait SampleStoreSpecLog {

    VitalsMetricsRegistry.disable()

    VitalsLog.configureLogging("samplestore", true)

    def log: Logger
  }


  trait SampleStoreAbstractSpec extends AnyFlatSpec with Matchers with SampleStoreSpecLog {

    final override def log: Logger = test.log
  }

  trait SampleStoreSupervisorSpec extends AnyFlatSpec with Matchers
    with SampleStoreSpecLog with BeforeAndAfterAll {

    final override def log: Logger = test.log

    var storeServiceClient: SampleStoreApiClient = _
    var storeServiceServer: SampleStoreApiServer = _

    override protected
    def beforeAll(): Unit = {
      VitalsPropertyRegistry.logReport
      storeServiceServer = SampleStoreApiServer((guid: String, dataSource: BurstSampleStoreDataSource) => {
        TeslaRequestFuture {
          val loci = Array(
            SampleStoreDataLocus(newBurstUid, getLocalHostAddress, getLocalHostName, 0, "some other partition paradata"),
            SampleStoreDataLocus(newBurstUid, getLocalHostAddress, getLocalHostName, 1, "yet some more partition paradata")
          )
          SampleStoreGeneration(guid, guid, loci, dataSource.view.schemaName, Some(dataSource.view.viewMotif))
        }
      })
      storeServiceClient = SampleStoreApiClient()
    }

    override protected
    def afterAll(): Unit = {
      storeServiceClient.stop
      storeServiceServer.stop
    }

  }

}
