/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.cache

import java.nio.file.{Files, Paths}

import org.burstsys.fabric.wave.data.model.slice.data.FabricSliceData
import org.burstsys.fabric.wave.data.model.slice.region
import org.burstsys.fabric.wave.data.model.snap.FabricSnap
import org.burstsys.fabric.wave.data.worker.pump.FabricCacheIntake
import org.burstsys.fabric.wave.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.test.FabricWaveSupervisorWorkerBaseSpec
import org.burstsys.fabric.test.mock.MockSlice
import org.burstsys.fabric.topology.model.node.UnknownFabricNodeId
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.vitals.net.getPublicHostAddress
import org.burstsys.vitals.uid.newBurstUid

class FabricWaveCacheSnapDataSpec extends FabricWaveSupervisorWorkerBaseSpec {

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    FabricCacheIntake.start
  }

  override protected def afterAll(): Unit = {
    FabricCacheIntake.stop
    super.afterAll()
  }

  val tmpFolder = Files.createTempDirectory("snap")
  val tmpPath = Paths.get(tmpFolder.toString, "mock")
  val worker = FabricWorkerNode(workerId = UnknownFabricNodeId, workerNodeAddress = getPublicHostAddress)
  val slice = MockSlice(newBurstUid, 0, newBurstUid, 1, FabricDatasource(-1, -1, -1), "", worker)
  val snap = FabricSnap(tmpPath, slice)

  it should "manage regions" in {
    val data = FabricSliceData(snap)

    // no regions before we open the data
    data.regionCount == 0

    data.openForWrites()
    data.regionCount == region.regionFolders.length

    data.closeForWrites()
    data.regionCount == 0

    data.openForWrites()
    data.regionCount == region.regionFolders.length

    data.closeForWrites()
    data.regionCount == 0
  }

}
