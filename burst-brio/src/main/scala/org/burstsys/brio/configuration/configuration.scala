/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio

import org.burstsys.vitals.properties.{VitalsPropertyRegistry, VitalsPropertySpecification}

package object configuration extends VitalsPropertyRegistry {

  val brioPressThreadsProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "burst.brio.press.threads",
    description = "",
    default = Some(Runtime.getRuntime.availableProcessors * 2)
  )

}
