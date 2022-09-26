/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.model.metrics

import org.burstsys.fabric.data.model.generation.key.FabricGenerationKey
import org.burstsys.vitals.json.VitalsJsonRepresentable

/**
 * An object for storing a set of metrics associated with the wave execution
 * scan/merge/gather pipeline results
 *
 * @tparam M
 */
trait FabricMetrics[M <: FabricMetrics[M]] extends VitalsJsonRepresentable[M] {

  /**
   * initialize this metric object
   *
   * @param key
   */
  def initMetrics(key: FabricGenerationKey): Unit

  //------------------ WORKER SIDE -----------------------------

  /**
   * called once as each item in a region is scanned on worker
   *
   * @param metrics
   */
  def mergeItemMetricsOnWorker(metrics: M): Unit

  /**
   * called once after all items in a region are merged on worker
   */
  def finalizeRegionMetricsOnWorker(): Unit

  /**
   * called once after all regions in a slice are merged on worker
   */
  def finalizeSliceMetricsOnWorker(): Unit

  //------------------ SUPERVISOR SIDE -----------------------------

  /**
   * called once after all slices in a wave are merged on supervisor. The array is expected
   * to contain metrics for all slices (i.e. the array must contain this object).
   */
  def finalizeWaveMetricsOnSupervisor(sliceMetrics: Array[M]): Unit

}
