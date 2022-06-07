/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt

import org.burstsys.vitals.properties.{VitalsPropertyRegistry, VitalsPropertySpecification}

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

package object configuration extends VitalsPropertyRegistry {

  val burstFeltCompileThreadsProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "burst.felt.compile.threads",
    description = "how many threads in the compile pool",
    default = Some(Runtime.getRuntime.availableProcessors)
  )

  val burstFeltMaxCachedSweepProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "burst.felt.sweep.cached.max",
    description = "how many sweeps are cached",
      default = Some(128)
  )

  val burstFeltSweepCleanSecondsProperty: VitalsPropertySpecification[Long] = VitalsPropertySpecification[Long](
    key = "burst.felt.sweep.clean.sec",
    description = "how often sweeps are 'cleaned' (sec)",
      default = Some((1 minute).toSeconds)
  )

}
