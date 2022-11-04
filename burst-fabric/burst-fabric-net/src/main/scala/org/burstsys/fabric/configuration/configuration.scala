/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric

import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostPort, getPublicHostAddress}
import org.burstsys.vitals.properties._

import java.lang.Runtime.getRuntime
import scala.language.postfixOps

package object configuration extends VitalsPropertyRegistry {

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NETWORK
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  val burstFabricNetHostProperty: VitalsPropertySpecification[VitalsHostAddress] = VitalsPropertySpecification[VitalsHostAddress](
    key = "burst.fabric.net.host",
    description = "host/address for fabric net",
    default = Some(getPublicHostAddress)
  )

  val burstFabricNetPortProperty: VitalsPropertySpecification[VitalsHostPort] = VitalsPropertySpecification[VitalsHostPort](
    key = "burst.fabric.net.port",
    description = "port for fabric net",
    default = Some(37060)
  )

  /**
   * currently not used... NETTY defaults...
   */
  val burstFabricNetClientThreadsProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "burst.fabric.net.client.threads",
    description = "",
    default = Some(getRuntime.availableProcessors)
  )

  /**
   * currently not used... NETTY defaults...
   */
  val burstFabricNetServerThreadsProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "burst.fabric.net.server.threads",
    description = "",
    default = Some(getRuntime.availableProcessors * 2)
  )

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TOPOLOGY
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  val burstFabricTopologyHomogeneous: VitalsPropertySpecification[Boolean] = VitalsPropertySpecification[Boolean](
    key = "burst.fabric.topology.homogeneous",
    description = "whether or not the topology should consist of only homogeneous versions",
    default = Some(true)
  )

  val burstFabricSupervisorStandaloneProperty: VitalsPropertySpecification[Boolean] = VitalsPropertySpecification[Boolean](
    key = "burst.fabric.supervisor.standalone",
    description = "enable standalone supervisor container world",
    default = Some(false)
  )

  val burstFabricWorkerStandaloneProperty: VitalsPropertySpecification[Boolean] = VitalsPropertySpecification[Boolean](
    key = "burst.fabric.worker.standalone",
    description = "enable standalone worker container world",
    default = Some(false)
  )
}
