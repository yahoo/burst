/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore

import org.burstsys.vitals.properties.VitalsPropertySpecification

import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
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

}
