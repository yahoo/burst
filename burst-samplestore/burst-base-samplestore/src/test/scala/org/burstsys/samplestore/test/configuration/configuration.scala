/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.test

import org.burstsys.vitals.properties.VitalsPropertySpecification

import scala.concurrent.duration.{Duration, DurationInt}
import scala.language.postfixOps

package object configuration {
  ///////////////////////////////////////////////////////////////////
  // Data pressing
  ///////////////////////////////////////////////////////////////////

  val pressTimeoutProperty = "burst.samplestore.press.timeout"

  val defaultPressTimeoutProperty: VitalsPropertySpecification[Duration] = VitalsPropertySpecification[Duration](
    key = pressTimeoutProperty,
    description = "the amount of time to wait for all items to be pressed",
    default = Some(1 minute)
  )

  ///////////////////////////////////////////////////////////////////
  // Slice Generation
  ///////////////////////////////////////////////////////////////////

  val lociCountProperty: String = "synthetic.samplestore.loci.count"

  val defaultLociCountProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = lociCountProperty,
    description = "number of samplestore loci to generate",
    default = Some(0)
  )
}
