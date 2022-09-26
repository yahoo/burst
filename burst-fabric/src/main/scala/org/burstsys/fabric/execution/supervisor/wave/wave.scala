/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.supervisor

import java.util.concurrent.atomic.AtomicLong

import org.burstsys.fabric.execution.model.execute.group.FabricGroupKey
import org.burstsys.fabric.execution.model.gather.control.FabricControlGatherContext
import org.burstsys.fabric.execution.model.scanner.FabricScanner
import org.burstsys.vitals.logging._

import scala.concurrent.duration._
import scala.language.postfixOps

package object wave extends VitalsLogger {

  val mergeWait: Duration = 5 minutes // longest merge pipeline possible?
  val scatterTimeout: FiniteDuration = 5.minutes

  /**
   * placed into the end of the merge pipeline queue when the last gather
   * has been pushed
   */
  private[wave] object EndOfQueueGather extends FabricControlGatherContext {
    override val resultMessage: String = "endOfQueueGather"
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // wave sequence numbers
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * JVM unique logical sequence number for fabric waves
   * one a given supervisor this number will increase monotonically until it wraps at [[Long.MaxValue]]
   */
  type FabricWaveSeqNum = Long

  private final val waveSequenceGenerator = new AtomicLong()

  /**
   * get a new JVM unique wave sequence number
   *
   * @return
   */
  final def newWaveSeqNum: FabricWaveSeqNum = waveSequenceGenerator.incrementAndGet()

}
