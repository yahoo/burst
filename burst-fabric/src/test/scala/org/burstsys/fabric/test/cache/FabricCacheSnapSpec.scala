/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.cache

import java.io.File
import java.nio.file.{Files, Paths}

import org.burstsys.fabric.data.model.snap.FabricSnap
import org.burstsys.fabric.data.worker.cache.{burstModuleName => _}
import org.burstsys.fabric.data.worker.pump.FabricCacheIntake
import org.burstsys.fabric.metadata.model
import org.burstsys.fabric.test.FabricSupervisorWorkerBaseSpec
import org.burstsys.fabric.test.mock.MockSlice
import org.burstsys.fabric.topology.model.node.UnknownFabricNodeId
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.vitals.net.getPublicHostAddress
import org.burstsys.vitals.uid._

import scala.language.postfixOps

class FabricCacheSnapSpec extends FabricSupervisorWorkerBaseSpec {

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    FabricCacheIntake.start
  }

  override protected def afterAll(): Unit = {
    FabricCacheIntake.stop
    super.afterAll()
  }

  it should "read and write snap files" in {
    // get a place for this to go
    val tmpFolder = Files.createTempDirectory("snap")
    val tmpPath = Paths.get(tmpFolder.toString, "mock")


    val datasource = model.datasource.FabricDatasource(domainKey = 1, viewKey = 1, generationClock = System.currentTimeMillis())

    // mock up something
    val slice = MockSlice(newBurstUid, 0, newBurstUid, 1, datasource, "",
      FabricWorkerNode(
        workerId = UnknownFabricNodeId,
        workerNodeAddress = getPublicHostAddress
      )
    )

    val snap = FabricSnap(tmpPath, slice)

    // open for writes to create the snap.data's regions
    snap.data.openForWrites()
    // persist it
    snap.persist

    // see if its there
    val files = new File(tmpFolder.toString).list()
    files.length should equal(1)
    files.head should equal("mock.snap")

    // get it back
    val snap2 = FabricSnap(tmpPath)
    snap2.snapFile should equal(snap.snapFile)
    snap2
  }

}
