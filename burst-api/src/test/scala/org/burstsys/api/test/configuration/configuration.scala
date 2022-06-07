/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.api.test

import java.util.concurrent.TimeUnit

import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostPort, getPublicHostAddress}
import org.burstsys.vitals.properties.{VitalsPropertyRegistry, VitalsPropertySpecification}
import org.burstsys.vitals.time._

import scala.concurrent.duration._
import scala.language.postfixOps

package object configuration extends VitalsPropertyRegistry {

  val burstTestApiHostProperty: VitalsPropertySpecification[VitalsHostAddress] = VitalsPropertySpecification[VitalsHostAddress](
    key = "burst.test.api.host",
    description = "host/address for test thrift API",
    default = Some(getPublicHostAddress)
  )

  val burstTestApiPortProperty: VitalsPropertySpecification[VitalsHostPort] = VitalsPropertySpecification[VitalsHostPort](
    key = "burst.test.api.port",
    description = "port for test thrift API",
    default = Some(37666)
  )

  val burstTestApiSslEnableProperty: VitalsPropertySpecification[Boolean] = VitalsPropertySpecification[Boolean](
    key = "burst.test.api.ssl.enable",
    description = "enable SSL sockets",
    default = Some(false)
  )

  val burstTestApiTimeoutMsProperty: VitalsPropertySpecification[VitalsMs] = VitalsPropertySpecification[VitalsMs](
    key = "burst.test.api.timeout.ms",
    description = "timeout for test thrift API",
    default = Some((5 minutes).toMillis)
  )

  def burstTestApiTimeoutDuration: Duration = Duration(burstTestApiTimeoutMsProperty.getOrThrow, TimeUnit.MILLISECONDS)

  val burstTestServerConnectionLifeMsProperty: VitalsPropertySpecification[VitalsMs] = VitalsPropertySpecification[VitalsMs](
    key = "burst.test.server.connect.life.ms",
    description = "test server connect lifetime",
    default = Some((5 minutes).toMillis)
  )

  def burstTestServerConnectionLifeDuration: Duration = Duration(burstTestServerConnectionLifeMsProperty.getOrThrow, TimeUnit.MILLISECONDS)

  val burstTestServerConnectionIdleMsProperty: VitalsPropertySpecification[VitalsMs] = VitalsPropertySpecification[VitalsMs](
    key = "burst.test.server.connect.idle.ms",
    description = "test server connect idletime",
    default = Some((5 minutes).toMillis)
  )

  def burstTestServerConnectionIdleDuration: Duration = Duration(burstTestServerConnectionIdleMsProperty.getOrThrow, TimeUnit.MILLISECONDS)

}
