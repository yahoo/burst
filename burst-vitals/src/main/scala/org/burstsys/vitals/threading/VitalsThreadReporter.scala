/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.threading

import org.burstsys.vitals.host.{osTotalCores, threadsCurrent, threadsPeak, threadsTotalCpuTime, threadsUserCpuTime}
import org.burstsys.vitals.instrument.prettyTimeFromNanos
import org.burstsys.vitals.reporter.VitalsReporter

/**
 * helper types/functions for Host/Process/Os state
 */
private[vitals]
object VitalsThreadReporter extends VitalsReporter {

  final val dName: String = "vitals-thread"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def sample(sampleMs: Long): Unit = {
    newSample()
    super.sample(sampleMs)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////
  // REPORT
  /////////////////////////////////////////////////////////////////////////////////////////////

  override def report: String = {
    if (nullData) return ""
    val counts = s"\thost_cores_available=$osTotalCores, proc_threads_current=$threadsCurrent, proc_threads_peak=$threadsPeak \n "
    val times = s"\tproc_threads_user_time=$threadsUserCpuTime ns (${prettyTimeFromNanos(threadsUserCpuTime)}), proc_threads_total_time=$threadsTotalCpuTime ns (${prettyTimeFromNanos(threadsTotalCpuTime)}) \n "
    s"$counts$times"
  }

}
