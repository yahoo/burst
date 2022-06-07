/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.scatter

import java.util

import org.burstsys.vitals.uid._
import org.burstsys.vitals.logging._
import org.jctools.queues.MpmcArrayQueue

import scala.language.postfixOps

package object pool extends VitalsLogger {

  final val maxActiveScatters: Int = 1e3.toInt

  private
  final lazy val scatterQueue: util.Queue[TeslaScatter] = new MpmcArrayQueue[TeslaScatter](maxActiveScatters)

  /**
   * get started with a free scatter by assigning a GUID and giving it the
   * ''request'' to execute. Scatters are grabbed from a queue.
   *
   * @param guid
   * @return
   */
  final
  def grabScatter(guid: VitalsUid): TeslaScatter = {
    scatterQueue poll match {
      case null => TeslaScatter().asInstanceOf[TeslaScatterContext].open(guid)
      case scatter: TeslaScatterContext => scatter.open(guid)
    }
  }

  /**
   * release the reusable scatter back to a pool of scatters
   *
   * @param scatter
   */
  final
  def releaseScatter(scatter: TeslaScatter): Unit = {
    scatterQueue add scatter.asInstanceOf[TeslaScatterContext].close
  }

}
