/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.press

import java.util.concurrent.atomic.AtomicLong

import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.logging._

import scala.concurrent.duration._
import scala.language.postfixOps

package object pipeline extends VitalsLogger with BrioPressPipeline {

  private[pipeline]
  final val jobId = new AtomicLong

  final val pressBufferSize = 10000000

  final val slowPressDuration = 60 seconds // how long is suspiciously long for a press

  final val brioPressDefaultDictionarySize: TeslaPoolId = 10e6.toInt

}
