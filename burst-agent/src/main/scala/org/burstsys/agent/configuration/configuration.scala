/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent

import java.util.concurrent.TimeUnit

import org.burstsys.vitals.logging._
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostPort, getPublicHostName}
import org.burstsys.vitals.properties._
import org.burstsys.vitals.time.VitalsMs

import scala.concurrent.duration._
import scala.language.postfixOps

package object configuration extends VitalsLogger with VitalsPropertyRegistry {

  val burstAgentApiMaxConcurrencyProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "burst.agent.concurrency.max",
    description = "max num of concurrent agent requests",
    default = Some(40)
  )

  val burstAgentApiHostProperty: VitalsPropertySpecification[VitalsHostAddress] = VitalsPropertySpecification[VitalsHostAddress](
    key = "burst.agent.api.host",
    description = "host/address for agent thrift API",
    default = Some(getPublicHostName)
  )

  private[agent]
  val burstAgentApiPortProperty: VitalsPropertySpecification[VitalsHostPort] = VitalsPropertySpecification[VitalsHostPort](
    key = "burst.agent.api.port",
    description = "port for agent thrift API",
    default = Some(37000)
  )

  val burstAgentApiSslEnableProperty: VitalsPropertySpecification[Boolean] = VitalsPropertySpecification[Boolean](
    key = "burst.agent.api.ssl.enable",
    description = "enable agent thrift SSL",
    default = Some(false)
  )

  private[agent]
  def burstAgentApiSslEnabled: Boolean = burstAgentApiSslEnableProperty.getOrThrow

  val burstAgentApiTimeoutMsProperty: VitalsPropertySpecification[VitalsMs] = VitalsPropertySpecification[VitalsMs](
    key = "burst.agent.api.timeout.ms",
    description = "timeout for agent thrift API",
    default = Some((60 seconds).toMillis)
  )

  private[agent]
  def burstAgentApiTimeoutDuration: Duration = Duration(burstAgentApiTimeoutMsProperty.getOrThrow, TimeUnit.MILLISECONDS)

  private[agent]
  val burstAgentServerConnectionLifeMsProperty: VitalsPropertySpecification[VitalsMs] = VitalsPropertySpecification[VitalsMs](
    key = "burst.agent.server.connect.life.ms",
    description = "agent server connect lifetime",
    default = Some((3 minutes).toMillis)
  )

  private[agent]
  def burstAgentServerConnectionLifeDuration: Duration = Duration(burstAgentServerConnectionLifeMsProperty.getOrThrow, TimeUnit.MILLISECONDS)

  private[agent]
  val burstAgentServerConnectionIdleMsProperty: VitalsPropertySpecification[VitalsMs] = VitalsPropertySpecification[VitalsMs](
    key = "burst.agent.server.connect.idle.ms",
    description = "agent server connect idletime",
    default = Some((3 minutes).toMillis)
  )

  private[agent]
  def burstAgentServerConnectionIdleDuration: Duration = Duration(burstAgentServerConnectionIdleMsProperty.getOrThrow, TimeUnit.MILLISECONDS)

}
