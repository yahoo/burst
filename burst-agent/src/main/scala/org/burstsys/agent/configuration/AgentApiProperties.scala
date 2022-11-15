/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.configuration

import org.burstsys.vitals.configuration.SslGlobalProperties
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostPort}

import scala.concurrent.duration.Duration

trait AgentApiProperties extends Any with SslGlobalProperties {

  /**
   * The host for this api
   *
   * @return
   */
  final def apiHost: VitalsHostAddress = burstAgentApiHostProperty.get

  /**
   * The port for this api
   *
   * @return
   */
  final def apiPort: VitalsHostPort = burstAgentApiPortProperty.get

  /**
   * enable SSL socket connections
   *
   * @return
   */
  final def enableSsl: Boolean = burstAgentApiSslEnableProperty.get

  /**
   *
   * @return
   */
  final def maxConnectionIdleTime: Duration = burstAgentServerConnectionIdleDuration

  /**
   *
   * @return
   */
  final def maxConnectionLifeTime: Duration = burstAgentServerConnectionLifeDuration

  /**
   * TODO
   *
   * @return
   */
  final def requestTimeout: Duration = burstAgentServerConnectionLifeDuration

}
