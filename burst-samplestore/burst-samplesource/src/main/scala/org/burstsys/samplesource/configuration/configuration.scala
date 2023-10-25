/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource

import org.burstsys.vitals.properties.VitalsPropertySpecification

import scala.language.postfixOps

package object configuration {

  ///////////////////////////////////////////////////////////////////
  // Sample Store Configuration
  ///////////////////////////////////////////////////////////////////

  val sampleStoreNexusFeedStreamLogSize: VitalsPropertySpecification[Int] = VitalsPropertySpecification(
    key = "burst.samplestore.nexus.feedstream.log.size",
    description = "how many nexus feedstream sample store calls to retain for inspection",
    default = Some(256)
  )

  val manualBatchSpan = "burst.samplestore.scanning.manualspan"
  val defaultManualBatchSpanProperty: VitalsPropertySpecification[Boolean] = VitalsPropertySpecification(
    key = manualBatchSpan,
    description = "manually create a span around a scanning batch",
    default = Some(false)
  )
}
