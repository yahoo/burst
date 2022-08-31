/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.test

import org.burstsys.vitals.properties.VitalsPropertyRegistry
import org.burstsys.vitals.properties.VitalsPropertySpecification

import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import scala.language.postfixOps

package object properties extends VitalsPropertyRegistry {

  val mockDurationProperty: VitalsPropertySpecification[Duration] = VitalsPropertySpecification[Duration](
    key = "burst.mock.test",
    description = "test properties",
    default = Some(10 minutes)
  )

  val mockDurationProperty2: VitalsPropertySpecification[Duration] = VitalsPropertySpecification[Duration](
    key = "burst.mock.test2",
    description = "test properties",
    default = Some(10 minutes)
  )

  val mockSensitiveProperty: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.hidden.prop",
    description = "Value should be redacted",
    sensitive = true,
    default = Some("You won't see me")
  )

}
