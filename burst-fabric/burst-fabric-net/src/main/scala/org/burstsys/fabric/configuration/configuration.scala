/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric

import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostPort, getPublicHostAddress}
import org.burstsys.vitals.properties._

import java.lang.Runtime.getRuntime
import scala.language.postfixOps

package object configuration extends VitalsPropertyRegistry {

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Fabric Net
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
  // HTTP
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final val defaultHttpPort: Int = 443

  val burstHttpNameProperty: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.liaison.name",
    description = "user friendly name for app",
    default = Some("burst")
  )

  val burstHttpHostProperty: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.fabric.http.host",
    description = "host/address for REST API",
    default = Some("0.0.0.0")
  )

  val burstHttpPortProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "burst.fabric.http.port",
    description = "port for REST API",
    default = Some(defaultHttpPort)
  )

  val burstUseHttpsProperty: VitalsPropertySpecification[Boolean] = VitalsPropertySpecification[Boolean](
    key = "burst.fabric.http.secure",
    description = "if the dashboard should be served over https",
    default = Some(true)
  )

  val burstHttpSslKeystorePath: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.fabric.http.keystore.path",
    description = "the keystore for the http server",
    default = Some("") // this is a super giant hack to get around the fact that you cannot have a default of None
  )

  private final val KEYSTORE_SERVER_PWD = "burstomatic"

  val burstHttpSslKeystorePassword: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.fabric.http.keystore.password",
    description = "the keystore for the http server",
    sensitive = true,
    default = Some(KEYSTORE_SERVER_PWD)
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
