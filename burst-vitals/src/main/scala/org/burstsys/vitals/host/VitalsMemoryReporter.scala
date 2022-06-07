/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.host

import org.burstsys.vitals.reporter._
import org.burstsys.vitals.reporter.metric.VitalsReporterByteValueMetric

import scala.language.postfixOps

/**
 * helper types/functions for Host/Process/Os state
 */
private[vitals]
object VitalsMemoryReporter extends VitalsReporter {

  final val dName: String = "vitals-mem"

  /////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  final val _heapUsed = VitalsReporterByteValueMetric("proc_heap_used_mem")

  private[this]
  final val _heapCommitted = VitalsReporterByteValueMetric("proc_heap_commit_mem")

  private[this]
  final val _heapMax = VitalsReporterByteValueMetric("proc_heap_max_mem")

  private[this]
  final val _offHeapUsed = VitalsReporterByteValueMetric("proc_off_heap_max_mem")

  private[this]
  final val _offHeapCommitted = VitalsReporterByteValueMetric("proc_off_heap_commit_mem")

  private[this]
  final val _offHeapMax = VitalsReporterByteValueMetric("proc_off_heap_max_mem")

  private[this]
  final val _osTotalMemory = VitalsReporterByteValueMetric("host_phys_total_mem")

  private[this]
  final val _osFreeMemory = VitalsReporterByteValueMetric("host_phys_free_mem")

  private[this]
  final val _osCommittedMemory = VitalsReporterByteValueMetric("host_virt_commit_mem")

  this +=_heapUsed
  this +=_heapCommitted
  this +=_heapMax
  this += _offHeapUsed
  this +=_offHeapCommitted
  this +=_offHeapMax
  this +=_osTotalMemory
  this +=_osFreeMemory
  this += _osCommittedMemory

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def sample(sampleMs: Long): Unit = {
    newSample()
    _heapUsed.record(heapUsed)
    _heapCommitted.record(nonHeapUsed)
    _heapMax.record(heapMax)

    _offHeapUsed.record(nonHeapUsed)
    _offHeapCommitted.record(nonHeapCommitted)
    _offHeapMax.record(nonHeapMax)

    _osTotalMemory.record(osTotalPhysMemory)
    _osFreeMemory.record(osFreePhysMemory)
    _osCommittedMemory.record(osCommittedVirtualMemory)

    super.sample(sampleMs)

  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // REPORT
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def report: String = {
    if (nullData) return ""
    f"${_heapUsed.report}${_heapCommitted.report}${_heapMax.report}${_offHeapUsed.report}${_offHeapCommitted.report}" +
      f"${_offHeapMax.report}${_osTotalMemory.report}${_osFreeMemory.report}${_osCommittedMemory.report}"
  }

}
