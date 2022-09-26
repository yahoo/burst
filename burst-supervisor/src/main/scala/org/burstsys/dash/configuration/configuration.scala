/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash

import org.burstsys.vitals.logging.VitalsLogger
import org.burstsys.vitals.properties.VitalsPropertyRegistry
import org.burstsys.vitals.properties.VitalsPropertySpecification

package object configuration extends VitalsLogger with VitalsPropertyRegistry {

  final val defaultRestPort: Int = 443

  val burstRestNameProperty: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.liaison.name",
    description = "user friendly name for app",
    default = Some("burst")
  )

  val burstRestHostProperty: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.liaison.host",
    description = "host/address for REST API",
    default = Some("0.0.0.0")
  )

  val burstRestPortProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "burst.liaison.port",
    description = "port for REST API",
    default = Some(defaultRestPort)
  )

  val burstHomePageProperty: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.liaison.homepage",
    description = "home page for UI",
    default = Some("burst.html")
  )

  val burstRestUsesHttpsProperty: VitalsPropertySpecification[Boolean] = VitalsPropertySpecification[Boolean](
    key = "burst.liaison.https",
    description = "if the dashboard should be served over https",
    default = Some(true)
  )

  val burstRestSslKeystorePath: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.liaison.keystore.path",
    description = "the keystore for the http server",
    default = Some("") // this is a super giant hack to get around the fact that you cannot have a default of None
  )

  private final val KEYSTORE_SERVER_PWD = "burstomatic"

  val burstRestSslKeystorePassword: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.liaison.keystore.password",
    description = "the keystore for the http server",
    sensitive = true,
    default = Some(KEYSTORE_SERVER_PWD)
  )

}
