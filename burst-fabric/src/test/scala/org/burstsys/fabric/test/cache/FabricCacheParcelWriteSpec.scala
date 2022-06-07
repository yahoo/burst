/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.cache

import org.burstsys.brio.flurry.provider.unity.BurstUnityMockData
import org.burstsys.fabric.data.model.slice.FabricSlice
import org.burstsys.fabric.data.model.slice.state.FabricDataState
import org.burstsys.fabric.data.model.slice.state.FabricDataWarm
import org.burstsys.fabric.data.model.snap.FabricSnap
import org.burstsys.fabric.data.model.snap.getSnapFile
import org.burstsys.fabric.data.worker.cache.{burstModuleName => _}
import org.burstsys.fabric.data.worker.store.FabricWorkerLoader
import org.burstsys.fabric.test.FabricMasterWorkerBaseSpec
import org.burstsys.fabric.test.mock.mockDatasource
import org.burstsys.fabric.topology.model.node.UnknownFabricNodeId
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.tesla.parcel.TeslaParcel
import org.burstsys.tesla.thread.request.TeslaRequestCoupler
import org.burstsys.vitals.errors._
import org.burstsys.vitals.instrument.prettyByteSizeString
import org.burstsys.vitals.instrument.prettyRateString
import org.burstsys.vitals.instrument.prettyTimeFromNanos
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net._
import org.burstsys.vitals.uid._

import scala.language.postfixOps

/**
 */
class FabricCacheParcelWriteSpec extends FabricMasterWorkerBaseSpec with FabricWorkerLoader {

  private
  var byteTally = 0

  private
  var startNanos = System.nanoTime()

  override def wantsContainers: Boolean = true

  // these are deflated mock data parcels
  private
  lazy val data: Array[TeslaParcel] = BurstUnityMockData(3e2.toInt).pressToDeflatedParcels // miniStoreData(datasource.view)

  "Fabric Cache" should "write parcels really fast" in {

    TeslaRequestCoupler {

      val guid = newBurstUid
      val hash = newBurstUid

      val slice = FabricSlice(
        guid = guid, datasource = mockDatasource,
        worker = FabricWorkerNode(
          UnknownFabricNodeId,
          workerNodeAddress = getPublicHostAddress
        ),
        sliceKey = 0, generationHash = hash, slices = 1
      )
      val snap = FabricSnap(getSnapFile(slice), slice)
      try {
        this.loadSliceFromCacheOrInitialize(snap)
      } catch safely {
        case t: Throwable =>
          val msg = burstStdMsg(t)
          log.error(msg, t)
          throw VitalsException(msg, t)
      }

      val elapsedNanos = System.nanoTime - startNanos
      log info
        s"""
           |  ${prettyByteSizeString(byteTally)}
           |  elapsedNanos=$elapsedNanos (${prettyTimeFromNanos(elapsedNanos)})
           |  ${prettyRateString("byte", byteTally, elapsedNanos)}
       """.stripMargin

    }
  }

  override protected
  def initializeSlice(snap:FabricSnap): FabricDataState = {
    try {
      val start = System.nanoTime
      var itemCount = 0
      var byteCount = 0
      snap.data.openForWrites()
      try {
        data foreach {
          parcel =>
            itemCount += 1
            byteCount += parcel.currentUsedMemory
            snap.data queueParcelForWrite parcel
        }
        snap.data.waitForWritesToComplete()
        snap.metadata.state = FabricDataWarm
        snap.metadata.generationMetrics.recordSliceNormalColdLoad(
          loadTookMs = ((System.nanoTime - start) / 1e6).toLong, regionCount = snap.data.regionCount,
          itemCount = itemCount, expectedItemCount = itemCount, potentialItemCount = itemCount,
          rejectedItemCount = 0,
          byteCount = byteCount
        )
      } finally snap.data.closeForWrites()
    } catch safely {
      case t: Throwable =>
        val msg = burstStdMsg(t)
        log.error(msg, t)
        throw VitalsException(msg, t)
    }
    snap.metadata.state
  }

}
