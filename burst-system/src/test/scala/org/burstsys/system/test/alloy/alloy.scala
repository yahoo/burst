/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.test

import org.burstsys.json.samplestore.JsonSampleStoreContainer
import org.burstsys.json.samplestore.configuration.JsonSamplestoreDefaultConfiguration
import org.burstsys.samplestore.api.client.SampleStoreApiClient
import org.burstsys.vitals
import org.burstsys.vitals.VitalsService.VitalsStandaloneServer
import org.burstsys.vitals.VitalsService.VitalsStandardClient
import org.burstsys.vitals.git
import org.burstsys.vitals.logging.VitalsLog
import org.burstsys.vitals.logging.VitalsLogger
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

package object alloy extends VitalsLogger {

  trait AlloySampleSourceSpecSupport extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

    git.turnOffBuildValidation()
    VitalsLog.configureLogging("alloy-samplesource", consoleOnly = true)

  }

  trait AlloySampleStoreSpecSupport extends AlloySampleSourceSpecSupport {
    vitals.configuration.burstVitalsEnableReporting.set(false)

    var sampleStoreClient: SampleStoreApiClient = _

    val alloyStore: JsonSampleStoreContainer = JsonSampleStoreContainer(JsonSamplestoreDefaultConfiguration(), VitalsStandaloneServer)

    override protected
    def beforeAll(): Unit = {
      alloyStore.start
      sampleStoreClient = SampleStoreApiClient().start
    }

    override protected
    def afterAll(): Unit = {
      sampleStoreClient.stop
      alloyStore.stop
    }
  }
}
