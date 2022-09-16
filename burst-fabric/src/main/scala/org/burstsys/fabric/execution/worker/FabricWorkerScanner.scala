/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.worker

import java.util.concurrent.ArrayBlockingQueue
import org.burstsys.brio.blob.BrioBlob.BrioRegionIterator
import org.burstsys.fabric.execution.model.gather.FabricGather
import org.burstsys.fabric.execution.model.gather.control.FabricFaultGather
import org.burstsys.fabric.execution.model.gather.data.FabricEmptyGather
import org.burstsys.fabric.execution.model.scanner.FabricScanner
import org.burstsys.tesla.thread.worker.{TeslaWorkerCoupler, TeslaWorkerFuture}
import org.burstsys.vitals.errors.{VitalsException, safely}

/**
 * scan/merge all the items in a region for all regions in a slice on a worker
 * <hr/>
 * This is
 */
trait FabricWorkerScanner extends Any {

  self: FabricScanner =>

  /**
   * scan and merge each blob in each region in each slice into a single result gather
   *
   * @param sliceRegions all the regions in a slice
   * @return the final result gather
   */
  final
  def scanMergeRegionsInSlice(sliceRegions: Array[BrioRegionIterator]): FabricGather = {
    lazy val tag = s"FabricWorkerScanner.scanMergeRegionsInSlice(group=${self.group}) ${self.datasource})"

    val startNs = System.nanoTime

    var finalRegionsInSliceResult: FabricGather = null

    // this can happen...
    if (sliceRegions.isEmpty) throw VitalsException(s"SLICE_MERGE_NO_REGIONS $tag")

    try {
      // set up our results return path
      val itemsInRegionQueue = new ResultQueue(sliceRegions.length)
      var nonEmptyRegionCount = 0
      var allRegionsIndex = 0

      /**
       * go through all regions in slice and start collecting
       * results in a queue that are each the result of a scan/merge of
       * all items in the region
       */
      while (allRegionsIndex < sliceRegions.length) {
        val region = sliceRegions(allRegionsIndex)
        if (region.nonEmpty) {
          nonEmptyRegionCount += 1
          // feed result queue in background worker thread
          TeslaWorkerFuture {
            // single worker thread for the entire mmap region
            val itemsInRegionResult = try {
              scanMergeItemsInRegion(region)
            } catch safely {
              case t: Throwable =>
                FabricFaultGather(self, t)
            }
            itemsInRegionQueue put itemsInRegionResult
          }
        }
        allRegionsIndex += 1
      }

      // process final results from queue
      finalRegionsInSliceResult = mergeRegionResultsForSlice(nonEmptyRegionCount, itemsInRegionQueue)

    } catch safely {
      case t: Throwable =>
        finalRegionsInSliceResult = FabricFaultGather(this, t)
    }

    // capture the data load metrics from the snap
    finalRegionsInSliceResult.gatherMetrics.generationMetrics.xferSliceCacheLoadMetrics(snap.metadata.generationMetrics)

    // make sure the metrics are cleaned up as we reach one full slice on the worker
    finalRegionsInSliceResult.gatherMetrics.finalizeSliceMetricsOnWorker()

    // record metrics associated with the completion of a single slice on worker
    finalRegionsInSliceResult.gatherMetrics.executionMetrics.recordSliceScanOnWorker(System.nanoTime - startNs)

    finalRegionsInSliceResult
  }

  /**
   * scan and merge all the blobs in a region
   *
   * @param region
   * @return
   */
  private
  def scanMergeItemsInRegion(region: BrioRegionIterator): FabricGather = {
    lazy val tag = s"FabricWorkerScanner.scanMergeItemsInRegion(guid=${self.group.groupUid})"
    var finalItemsInRegionResult: FabricGather = null
    // go through all blobs in a region
    while (region.hasNext) {

      // grab the next blob in the region
      val currentItem = region.next()

      // an empty region returns a marker blob with .isEmpty == true
      // this is not truly an item and should not be counted as an item scanned
      if (currentItem.isEmpty) {
        log info s"REGION_SCAN_EMPTY_ITEM $tag"
      } else {

        // this is the actual specific scan i.e. the scanner.apply(blob) => gather
        val newItemGather = try {
          val scanStart = System.nanoTime
          val gather = this (currentItem) // scanner(blobItem) => gather -- scan of item returns gather
          gather.gatherMetrics.executionMetrics.recordItemScanOnWorker(System.nanoTime - scanStart)
          gather
        } catch safely {
          // for a low level scan fault, we  capture in special gather and track and recover all the way up the stack
          case t: Throwable =>
            FabricFaultGather(this, t)
        }

        // first regionResult handled differently than the rest
        if (finalItemsInRegionResult == null) {
          finalItemsInRegionResult = newItemGather // first one is our ultimate regionResult
        } else {
          finalItemsInRegionResult.regionMerge(newItemGather)
          // merge a new gather metric into the prior gather metric
          finalItemsInRegionResult.gatherMetrics.mergeItemMetricsOnWorker(newItemGather.gatherMetrics)
        }

      }
    }

    // if there were no non empty items - set up a special 'empty' gather
    if (finalItemsInRegionResult == null) finalItemsInRegionResult = FabricEmptyGather(this)

    // make sure metrics are cleaned up as we reach the end of one full region
    finalItemsInRegionResult.gatherMetrics.finalizeRegionMetricsOnWorker()

    finalItemsInRegionResult
  }

  /**
   * merge all the queued results of parallel scan merging all the items in regions for this slice
   *
   * @param nonEmptyRegionCount
   * @param regionResultQueue
   * @return
   */
  private
  def mergeRegionResultsForSlice(nonEmptyRegionCount: Int, regionResultQueue: ArrayBlockingQueue[FabricGather]): FabricGather = {
    lazy val tag = s"FabricWorkerScanner.mergeRegionResultsForSlice(guid=${self.group.groupUid})"
    var finalRegionsInSliceResult: FabricGather = null
    var regionIndex = 0
    while (regionIndex < nonEmptyRegionCount) {
      val newRegionResult = regionResultQueue.take
      if (finalRegionsInSliceResult == null) {
        finalRegionsInSliceResult = newRegionResult
      } else {
        TeslaWorkerCoupler { // worker thread to do CPU bound section
          try {
            finalRegionsInSliceResult.sliceMerge(newRegionResult)
          } catch safely {
            case t: Throwable =>
              finalRegionsInSliceResult.sliceMerge(FabricFaultGather(self, t))
          }
          finalRegionsInSliceResult.sliceFinalize()
        }
      }
      regionIndex += 1
    }
    // return appropriate type of gather
    if (finalRegionsInSliceResult == null)
      finalRegionsInSliceResult = FabricEmptyGather(self)

    finalRegionsInSliceResult
  }

}
