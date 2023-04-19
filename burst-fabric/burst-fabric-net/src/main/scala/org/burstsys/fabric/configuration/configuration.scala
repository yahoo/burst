/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric

import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostName, VitalsHostPort, getPublicHostAddress}
import org.burstsys.vitals.properties._

import java.lang.Runtime.getRuntime
import scala.concurrent.duration.{Duration, DurationInt}
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

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // HTTP
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final val defaultHttpPort: Int = 443

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

  val burstFabricTopologyHeartbeatPeriodMs: VitalsPropertySpecification[Duration] = VitalsPropertySpecification[Duration](
    key = "burst.fabric.topology.heartbeat.ms",
    description = "how often a worker should send heartbeat messages to the master",
    default = Some(5 seconds)
  )

  val burstFabricTopologyAssessmentPeriodMs: VitalsPropertySpecification[Duration] = VitalsPropertySpecification[Duration](
    key = "burst.fabric.topology.assessment.ms",
    description = "how often the master should ask for assessments from the workers",
    default = Some(15 seconds)
  )

  val burstFabricTopologyTardyThresholdMs: VitalsPropertySpecification[Duration] = VitalsPropertySpecification[Duration](
    key = "burst.fabric.topology.tardy.ms",
    description = "how long workers can not send a heartbeat before the topology marks them as tardy",
    default = Some(15 seconds)
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

  val burstFabricWorkerMonikerProperty: VitalsPropertySpecification[VitalsHostName] = VitalsPropertySpecification[VitalsHostName](
    key = "burst.fabric.worker.monkier",
    description = "",
    default = None
  )
}
