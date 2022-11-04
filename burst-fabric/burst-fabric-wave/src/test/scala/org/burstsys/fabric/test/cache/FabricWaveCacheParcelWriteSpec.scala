/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.cache

import org.burstsys.brio.flurry.provider.unity.BurstUnityMockData
import org.burstsys.fabric.wave.data.model.slice.FabricSlice
import org.burstsys.fabric.wave.data.model.slice.state.FabricDataHot
import org.burstsys.fabric.wave.data.model.slice.state.FabricDataState
import org.burstsys.fabric.wave.data.model.slice.state.FabricDataWarm
import org.burstsys.fabric.wave.data.model.snap.FabricSnap
import org.burstsys.fabric.wave.data.model.snap.getSnapFile
import org.burstsys.fabric.wave.data.worker.cache.{burstModuleName => _}
import org.burstsys.fabric.wave.data.worker.pump.FabricCacheIntake
import org.burstsys.fabric.wave.data.worker.store.FabricWorkerLoader
import org.burstsys.fabric.test.FabricWaveSupervisorWorkerBaseSpec
import org.burstsys.fabric.test.mock.mockDatasource
import org.burstsys.fabric.topology.model.node.UnknownFabricNodeId
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.tesla.thread.request.TeslaRequestCoupler
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net._
import org.burstsys.vitals.uid._

import scala.language.postfixOps

/**
 */
class FabricWaveCacheParcelWriteSpec extends FabricWaveSupervisorWorkerBaseSpec with FabricWorkerLoader {

  private val itemsToGenerate = 3e2.toInt

  private var bytesLoaded = 0

  override def beforeAll(): Unit = {
    super.beforeAll()
    FabricCacheIntake.start
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    FabricCacheIntake.stop
  }

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
        snap.metadata.state shouldBe FabricDataHot
        snap.metadata.generationMetrics.itemCount shouldBe itemsToGenerate
        snap.metadata.generationMetrics.expectedItemCount shouldBe itemsToGenerate
        snap.metadata.generationMetrics.rejectedItemCount shouldBe 0
      } catch safely {
        case t: Throwable =>
          val msg = burstStdMsg(t)
          log.error(msg, t)
          throw VitalsException(msg, t)
      }
    }
  }

  override protected
  def initializeSlice(snap:FabricSnap): FabricDataState = {
    try {
      val start = System.nanoTime
      var itemCount = 0
      snap.data.openForWrites()
      try {
        BurstUnityMockData(itemsToGenerate).pressToDeflatedParcels foreach {
          parcel =>
            itemCount += parcel.bufferCount
            bytesLoaded += parcel.inflatedSize
            snap.data queueParcelForWrite parcel
        }
        snap.data.waitForWritesToComplete()
        snap.metadata.state = FabricDataWarm
        snap.metadata.generationMetrics.recordSliceNormalColdLoad(
          loadTookMs = ((System.nanoTime - start) / 1e6).toLong, regionCount = snap.data.regionCount,
          itemCount = itemCount, expectedItemCount = itemCount, potentialItemCount = itemCount,
          rejectedItemCount = 0,
          byteCount = bytesLoaded
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
