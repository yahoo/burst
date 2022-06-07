/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.api.test

import org.burstsys.api._
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsStandardClient}
import org.burstsys.vitals.configuration.SslGlobalProperties
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostPort}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Success, Try}

trait TestApiService extends VitalsService with SslGlobalProperties {

  override val serviceName: String = "test-api"

  def apiHost: VitalsHostAddress = configuration.burstTestApiHostProperty.getOrThrow

  def apiPort: VitalsHostPort = configuration.burstTestApiPortProperty.getOrThrow

  def enableSsl: Boolean = configuration.burstTestApiSslEnableProperty.getOrThrow

  def requestTimeout: Duration = configuration.burstTestApiTimeoutDuration

  def maxConnectionIdleTime: Duration = configuration.burstTestServerConnectionIdleDuration

  def maxConnectionLifeTime: Duration = configuration.burstTestServerConnectionLifeDuration

  def testEndPoint(testMessage: String): Try[String]


}

object TestApiService {

  /**
    * default client config
    *
    * @return
    */
  def apply(): TestApiService = TestApiServiceContext(VitalsStandardClient)

  /**
    * basis constructor
    *
    * @param mode
    * @return
    */
  def apply(mode: VitalsServiceModality = VitalsStandardClient): TestApiService = TestApiServiceContext(mode: VitalsServiceModality)

}

private[test] final case
class TestApiServiceContext(modality: VitalsServiceModality) extends TestApiService {

  var apiClient: TestApiClient = _

  var apiServer: TestApiServer = _

  override
  def start: this.type = {
    log info startingMessage
    if (modality.isServer) {
      apiServer.start
    } else {
      apiClient.start
    }
    this
  }

  override
  def stop: this.type = {
    log info stoppingMessage
    if (modality.isServer) {
      apiServer.stop
    } else {
      apiClient.stop
    }
    this
  }

  override
  def testEndPoint(testMessage: String): Try[String] = {
    if (modality.isServer) {
      Success("world")
    } else {
      val future = apiClient.testEndPoint(testMessage)
      val executeResult = Await.result(future, requestTimeout)
      Success(executeResult)
    }
  }

}
