/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.cache

import java.util.Date
import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.burstsys.fabric.wave.configuration.burstViewCacheEraseTtlMsPropertyDefault
import org.burstsys.fabric.wave.data.model.snap.FabricSnap
import org.burstsys.fabric.wave.data.worker.cache.{burstModuleName => _}
import org.burstsys.tesla.thread.request.TeslaRequestCoupler

import scala.concurrent.duration._
import scala.language.postfixOps

//@Ignore
final class FabricWaveCacheEvictSpec extends FabricWaveCacheLifecycleSpec {

  private val evictCount = new CountDownLatch(1)
  private val flushCount = new CountDownLatch(1)
  private val eraseCount = new CountDownLatch(1)

  "Fabric Cache" should "go through full resource limit lifecycle" in {
    burstViewCacheEraseTtlMsPropertyDefault.set((2 seconds).toMillis)
    TeslaRequestCoupler {
      mockLimits.putDiskLow()
      mockLimits.putMemoryLow()
      waitForTend()
      snapCache.coldSnapCount should equal(0)
      snapCache.warmSnapCount should equal(0)
      snapCache.hotSnapCount should equal(0)

      loadGeneration(1, 1, new Date().getTime)
      snapCache.coldSnapCount should equal(0)
      snapCache.warmSnapCount should equal(0)
      snapCache.hotSnapCount should equal(1)
      waitForTend()

      mockLimits.putMemoryHigh()
      waitForTend()

      evictCount.await(60, TimeUnit.SECONDS) should equal(true)

      snapCache.coldSnapCount should equal(0)
      snapCache.warmSnapCount should equal(1)
      snapCache.hotSnapCount should equal(0)

      waitForTend()
      mockLimits.putDiskHigh()
      waitForTend()
      flushCount.await(60, TimeUnit.SECONDS) should equal(true)
      snapCache.coldSnapCount should equal(1)
      snapCache.warmSnapCount should equal(0)
      snapCache.hotSnapCount should equal(0)

      waitForTend()
      eraseCount.await(60, TimeUnit.SECONDS) should equal(true)
      snapCache.coldSnapCount should equal(0)
      snapCache.warmSnapCount should equal(0)
      snapCache.hotSnapCount should equal(0)
    }

  }

  override def onSnapEvict(snap: FabricSnap, elapsedNs: Long, bytes: Long): Unit = {
    mockLimits.putMemoryLow()
    evictCount.countDown()
  }

  override def onSnapFlush(snap: FabricSnap, elapsedNs: Long, bytes: Long): Unit = {
    mockLimits.putDiskLow()
    flushCount.countDown()
  }

  override def onSnapErase(snap: FabricSnap): Unit = {
    eraseCount.countDown()
  }

}
