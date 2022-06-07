/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.api

import org.burstsys.brio.provider.loadBrioSchemaProviders
import org.burstsys.vitals.properties._
import org.burstsys.vitals.VitalsService.{VitalsStandaloneServer, VitalsStandardClient}
import org.burstsys.vitals.logging.{VitalsLog, VitalsLogger}
import org.burstsys.vitals.metrics.VitalsMetricsRegistry
import org.apache.logging.log4j.Logger
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

  trait SampleStoreMasterSpec extends AnyFlatSpec with Matchers
    with SampleStoreSpecLog with BeforeAndAfterAll {

    final override def log: Logger = test.log

    var storeServiceClient: SampleStoreApiService = _
    var storeServiceServer: SampleStoreApiService = _

    override protected
    def beforeAll() {
      loadBrioSchemaProviders()
      VitalsPropertyRegistry.logReport
      storeServiceServer = SampleStoreApiService(VitalsStandaloneServer)
      storeServiceClient = SampleStoreApiService(VitalsStandardClient)
    }

    override protected
    def afterAll() {
      storeServiceClient.stop
      storeServiceServer.stop
    }

  }

}
