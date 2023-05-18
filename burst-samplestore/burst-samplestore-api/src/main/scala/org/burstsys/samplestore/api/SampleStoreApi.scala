/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.api

import org.burstsys.api.BurstApi
import org.burstsys.samplestore.api
import org.burstsys.samplestore.api.configuration.burstSampleStoreApiPortProperty
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.configuration.SslGlobalProperties
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostPort}

import scala.concurrent.duration.Duration

trait SampleStoreApiListener {

  def onViewGenerationRequest(guid: String, dataSource: BurstSampleStoreDataSource): Unit

  def onViewGeneration(guid: String, dataSource: BurstSampleStoreDataSource, result: BurstSampleStoreApiViewGenerator): Unit
}

/**
  * A front end to the underlying thrift API
  */
trait SampleStoreApi extends BurstSampleStoreApiService.MethodPerEndpoint with BurstApi with SslGlobalProperties {

  def modality: VitalsServiceModality

  final override def apiName: String = "samplestore"

  def apiHost: VitalsHostAddress = api.configuration.burstSampleStoreApiHostProperty.get

  def apiPort: VitalsHostPort = api.configuration.burstSampleStoreApiPortProperty.get

  def enableSsl: Boolean = api.configuration.burstSampleStoreApiSslEnableProperty.get

  def maxConnectionIdleTime: Duration = api.configuration.burstSampleStoreServerConnectionIdleMsProperty.get

  def maxConnectionLifeTime: Duration = api.configuration.burstSampleStoreServerConnectionLifeMsProperty.get

}
