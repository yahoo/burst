/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.worker

import org.burstsys.vitals.logging._

import scala.concurrent.duration._
import scala.language.postfixOps

package object pump extends VitalsLogger {

  type FabricImpellerId = Int

  type FabricSpindleId = Int

  final val writerQueuePoolWait = 50 milliseconds

}
