/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore

import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostName}
import org.burstsys.vitals.properties.VitalsPropertySpecification

import scala.language.postfixOps

package object configuration {

  ///////////////////////////////////////////////////////////////////
  // Sample Store Configuration
  ///////////////////////////////////////////////////////////////////

  val sampleStoreViewRequestLogSize: VitalsPropertySpecification[Int] = VitalsPropertySpecification(
    key = "burst.samplestore.request.log.size",
    description = "how many view generation requests to retain for inspection",
    default = Some(256)
  )

  val sampleStoreNexusHostAddrOverride: VitalsPropertySpecification[VitalsHostAddress] = VitalsPropertySpecification(
    key = "burst.samplestore.nexus.hostaddr.override",
    description = "in containerized environments the override the JVM reported host address",
    default = None
  )

  val sampleStoreNexusHostNameAddrOverride: VitalsPropertySpecification[VitalsHostName] = VitalsPropertySpecification(
    key = "burst.samplestore.nexus.hostname.override",
    description = "in containerized environments the override the JVM reported host address",
    default = None
  )

  val maxLoadSizeProperty = "burst.samplestore.load.max.bytes"
  val defaultMaxLoadSizeProperty: VitalsPropertySpecification[Long] = VitalsPropertySpecification(
    key = maxLoadSizeProperty,
    description = "the max size for a dataset load in bytes",
    default = Some(1e12.toInt) // 1TB
  )

}
