/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.test

import org.burstsys.brio.provider.loadBrioSchemaProviders
import org.burstsys.json.samplestore.JsonSampleStoreContainer
import org.burstsys.json.samplestore.configuration.JsonSamplestoreDefaultConfiguration
import org.burstsys.samplestore.api.SampleStoreApiService
import org.burstsys.vitals
import org.burstsys.vitals.VitalsService.VitalsStandaloneServer
import org.burstsys.vitals.git
import org.burstsys.vitals.logging.{VitalsLog, VitalsLogger}
import org.burstsys.vitals.metrics.VitalsMetricsRegistry
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

package object alloy extends VitalsLogger {

  trait AlloySampleSourceSpecSupport extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

    VitalsMetricsRegistry.disable()
    git.turnOffBuildValidation()
    VitalsLog.configureLogging("alloy-samplesource", consoleOnly = true)

  }

  trait AlloySampleStoreSpecSupport extends AlloySampleSourceSpecSupport {
    loadBrioSchemaProviders()

    vitals.configuration.burstVitalsEnableReporting.set(false)

    var sampleStoreClient: SampleStoreApiService = _

    val alloyStore: JsonSampleStoreContainer = JsonSampleStoreContainer(JsonSamplestoreDefaultConfiguration(), VitalsStandaloneServer)

    override protected
    def beforeAll(): Unit = {
      alloyStore.start
      sampleStoreClient = SampleStoreApiService().start
    }

    override protected
    def afterAll(): Unit = {
      sampleStoreClient.stop
      alloyStore.stop
    }
  }
}
