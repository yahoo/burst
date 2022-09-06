/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.api

import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsStandaloneServer
import org.burstsys.vitals.logging.{VitalsLog, VitalsLogger}
import org.burstsys.vitals.metrics.VitalsMetricsRegistry
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostPort}
import org.burstsys.vitals.properties.VitalsPropertyRegistry
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.Duration

package object test extends VitalsLogger {

  /**
    * TODO
    */
  trait TestApi extends VitalsService with BurstTestApiService.MethodPerEndpoint with BurstApi {

    def service: TestApiService

    final override def modality: VitalsService.VitalsServiceModality = service.modality

    final override def apiName: String = "test"

    final override def apiPort: VitalsHostPort = service.apiPort

    final override def apiHost: VitalsHostAddress = service.apiHost

    final override def enableSsl: Boolean = service.enableSsl

    final override def certPath: String = service.certPath

    final override def keyPath: String = service.keyPath

    final override def caPath: String = service.caPath

    final override def enableCompositeTrust: Boolean = service.enableCompositeTrust

    final override def maxConnectionIdleTime: Duration = service.maxConnectionIdleTime

    final override def maxConnectionLifeTime: Duration = service.maxConnectionLifeTime
  }

  abstract class TestApiSpecSupport extends AnyFlatSpec with Matchers with BeforeAndAfterAll  {
    VitalsMetricsRegistry.disable()

    VitalsLog.configureLogging("api", consoleOnly = true)

    val testServer: TestApiService = TestApiService(VitalsStandaloneServer)
    val testClient: TestApiService = TestApiService()

    override protected
    def beforeAll(): Unit = {
      VitalsPropertyRegistry.logReport
    }

    override protected
    def afterAll(): Unit = {
    }


  }


}
