/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus

import java.util.concurrent.TimeUnit

import org.burstsys.vitals.net.VitalsHostPort
import org.burstsys.vitals.properties.{VitalsPropertyRegistry, VitalsPropertySpecification}
import org.burstsys.vitals.time.VitalsMs

import scala.concurrent.duration._
import scala.language.postfixOps

package object configuration extends VitalsPropertyRegistry {

  val burstNexusClientThreadsProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "burst.nexus.client.threads",
    description = "",
    default = Some(Runtime.getRuntime.availableProcessors)
  )

  val burstNexusSslEnableProperty: VitalsPropertySpecification[Boolean] = VitalsPropertySpecification[Boolean](
    key = "burst.nexus.ssl.enable",
    description = "enable SSL sockets",
    default = Some(false)
  )

  val burstNexusClientPoolStaleProperty: VitalsPropertySpecification[VitalsMs] = VitalsPropertySpecification[VitalsMs](
    key = "burst.nexus.client.pool.stale.ms",
    description = "idle client stale time",
    default = Some((1 hour).toMillis)
  )

  def burstNexusClientStaleDuration: Duration = Duration(burstNexusClientPoolStaleProperty.get, TimeUnit.MILLISECONDS)

  val burstNexusClientCacheTenderProperty: VitalsPropertySpecification[VitalsMs] = VitalsPropertySpecification[VitalsMs](
    key = "burst.nexus.client.cache.tender.ms",
    description = "idle client check period",
    default = Some((10 minute).toMillis)
  )

  def burstNexusClientCacheTenderDuration: Duration = Duration(burstNexusClientCacheTenderProperty.get, TimeUnit.MILLISECONDS)

  val burstNexusServerPortProperty: VitalsPropertySpecification[VitalsHostPort] = VitalsPropertySpecification[VitalsHostPort](
    key = "burst.nexus.server.port",
    description = "Nexus Server port",
    default = Some(1270)
  )

  val burstNexusPipeSizeProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "burst.nexus.pipe.size",
    description = "",
    default = Some(1E2.toInt)
  )

  val burstNexusStreamTimeoutProperty: VitalsPropertySpecification[VitalsMs] = VitalsPropertySpecification[VitalsMs](
    key = "burst.nexus.stream.timeout.ms",
    description = "",
    default = Some((20 seconds).toMillis)
  )

  def burstNexusStreamTimeoutDuration: Duration = Duration(burstNexusStreamTimeoutProperty.get, TimeUnit.MILLISECONDS)

  val burstNexusStreamParcelPackerConcurrencyProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "burst.nexus.stream.parcel.packer.concurrency",
    description = "The number of parcel packers per stream",
    default = Some(2)
  )

}
