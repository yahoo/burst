/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.api.configuration

import org.burstsys.api.BurstApi
import org.burstsys.vitals.configuration.SslGlobalProperties
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostPort}

import scala.concurrent.duration.Duration

trait SampleStoreApiProperties extends BurstApi with SslGlobalProperties {

  /**
    *
    * @return
    */
  final def apiHost: VitalsHostAddress = burstSampleStoreApiHostProperty.getOrThrow

  /**
    *
    * @return
    */
  final def enableSsl: Boolean = burstSampleStoreApiSslEnableProperty.getOrThrow

  /**
    *
    * @return
    */
  final def maxConnectionIdleTime: Duration = burstSampleStoreServerConnectionIdleDuration

  /**
    *
    * @return
    */
  final def maxConnectionLifeTime: Duration = burstSampleStoreServerConnectionLifeDuration

  /**
    *
    * @return
    */
  final def apiPort: VitalsHostPort = burstSampleStoreApiPortProperty.getOrThrow

  /**
    * TODO
    *
    * @return
    */
  final def requestTimeout: Duration = burstSampleStoreApiTimeoutDuration

}
