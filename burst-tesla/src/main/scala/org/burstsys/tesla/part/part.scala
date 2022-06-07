/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla

import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.vitals.logging._

import scala.concurrent.duration._
import scala.language.postfixOps

package object part extends VitalsLogger {

  final val debugTending = false

  /**
   * the size of the array of pools for a given part
   */
  final val maxPoolsPerPart = 10000

  /**
   * we try not to free everything in one tending run
   */
  final var maxPartsFreedInOneRun: Int = 5e3.toInt

  /**
   * the default length of the parts queue
   */
  final val defaultPartsQueueSize = 2e5.toInt

  final val teslaBuilderUseDefaultSize: TeslaMemorySize = -1

}
