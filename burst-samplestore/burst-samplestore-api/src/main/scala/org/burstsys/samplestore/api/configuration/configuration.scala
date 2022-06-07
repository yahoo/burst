/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.api

import java.util.concurrent.TimeUnit

import org.burstsys.vitals.logging._
import org.burstsys.vitals.properties._
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostPort, getPublicHostAddress}
import org.burstsys.vitals.time.VitalsMs

import scala.concurrent.duration._
import scala.language.postfixOps

package object configuration extends VitalsLogger with VitalsPropertyRegistry {

  /////////////////////////////////////////////
  // Samplestore heartbeat
  /////////////////////////////////////////////
  final
  val burstSampleStoreHeartbeatInterval: VitalsPropertySpecification[VitalsMs] = VitalsPropertySpecification[VitalsMs](
    key = "burst.samplestore.heartbeat.interval.ms",
    description = "heartbeat frequency, in ms",
    exportToWorker = true, default = Some(10000)
  )

  def burstSampleStoreHeartbeatDuration: Duration = Duration(burstSampleStoreHeartbeatInterval.getOrThrow, TimeUnit.MILLISECONDS)

  /////////////////////////////////////////////
  // Sample Store API properties
  /////////////////////////////////////////////
  private[samplestore]
  val burstSampleStoreApiHostProperty: VitalsPropertySpecification[VitalsHostAddress] = VitalsPropertySpecification[VitalsHostAddress](
    key = "burst.samplestore.api.host",
    description = "host/address for samplestore thrift API",
    default = Some(getPublicHostAddress)
  )

  private[samplestore]
  val burstSampleStoreApiPortProperty: VitalsPropertySpecification[VitalsHostPort] = VitalsPropertySpecification[VitalsHostPort](
    key = "burst.samplestore.api.port",
    description = "port for samplestore thrift API",
    default = Some(37020)
  )

  val burstSampleStoreApiSslEnableProperty: VitalsPropertySpecification[Boolean] = VitalsPropertySpecification[Boolean](
    key = "burst.samplestore.api.ssl.enable",
    description = "enable samplestore thrift SSL",
    default = Some(false)
  )

  private[samplestore]
  val burstSampleStoreApiTimeoutMsProperty: VitalsPropertySpecification[VitalsMs] = VitalsPropertySpecification[VitalsMs](
    key = "burst.samplestore.api.timeout.ms",
    description = "",
    default = Some((120 seconds).toMillis)
  )

  private[samplestore]
  def burstSampleStoreApiTimeoutDuration: Duration = Duration(burstSampleStoreApiTimeoutMsProperty.getOrThrow, TimeUnit.MILLISECONDS)

  private[samplestore]
  val burstSampleStoreServerConnectionLifeMsProperty: VitalsPropertySpecification[VitalsMs] = VitalsPropertySpecification[VitalsMs](
    key = "burst.samplestore.server.connect.life.ms",
    description = "samplestore server connect lifetime",
    default = Some((5 minutes).toMillis)
  )

  private[samplestore]
  def burstSampleStoreServerConnectionLifeDuration: Duration = Duration(burstSampleStoreServerConnectionLifeMsProperty.getOrThrow, TimeUnit.MILLISECONDS)

  private[samplestore]
  val burstSampleStoreServerConnectionIdleMsProperty: VitalsPropertySpecification[VitalsMs] = VitalsPropertySpecification[VitalsMs](
    key = "burst.samplestore.server.connect.idle.ms",
    description = "samplestore server connect idletime",
    default = Some((5 minutes).toMillis)
  )

  private[samplestore]
  def burstSampleStoreServerConnectionIdleDuration: Duration = Duration(burstSampleStoreServerConnectionIdleMsProperty.getOrThrow, TimeUnit.MILLISECONDS)


}
