/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.cache

import java.util.Date
import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.burstsys.fabric.wave.data.model.snap.FabricSnap
import org.burstsys.fabric.wave.data.worker.cache.{FabricSnapCacheListener, burstModuleName => _}
import org.burstsys.fabric.test.mock
import org.burstsys.tesla.thread.request.TeslaRequestCoupler
import org.burstsys.vitals.uid

import scala.language.postfixOps

class FabricWaveCacheResliceSpec extends FabricWaveCacheLifecycleSpec with FabricSnapCacheListener {

  val resliceGate = new CountDownLatch(1)

  it should "should reslice" in {
    TeslaRequestCoupler {
      mock.currentGenerationHash = uid.newBurstUid
      val generationClock = new Date().getTime
      loadGeneration(1, 1, generationClock)
      mock.currentGenerationHash = uid.newBurstUid
      loadGeneration(1, 1, generationClock)
    }

    resliceGate.await(60, TimeUnit.SECONDS) should equal(true)

  }

  override def onSnapReslice(snap: FabricSnap): Unit = {
    log info s"onSnapReslice!"
    resliceGate.countDown()
  }

}
