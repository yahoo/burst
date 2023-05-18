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
  val burstSampleStoreHeartbeatInterval: VitalsPropertySpecification[Duration] = VitalsPropertySpecification[Duration](
    key = "burst.samplestore.heartbeat.interval",
    description = "heartbeat frequency",
    default = Some(10.seconds)
  )

  def burstSampleStoreHeartbeatDuration: Duration = burstSampleStoreHeartbeatInterval.get

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
  val burstSampleStoreServerConnectionLifeMsProperty: VitalsPropertySpecification[Duration] = VitalsPropertySpecification[Duration](
    key = "burst.samplestore.server.connect.life",
    description = "samplestore server connect lifetime",
    default = Some(5 minutes)
  )

  private[samplestore]
  val burstSampleStoreServerConnectionIdleMsProperty: VitalsPropertySpecification[Duration] = VitalsPropertySpecification[Duration](
    key = "burst.samplestore.server.connect.idle",
    description = "samplestore server connect idletime",
    default = Some(5 minutes)
  )

}
