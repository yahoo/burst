/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.cache

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.fabric.test.FabricWaveSupervisorWorkerBaseSpec
import org.burstsys.fabric.test.mock.{MockScanner, MockStoreName}
import org.burstsys.fabric.topology.FabricTopologyWorker
import org.burstsys.fabric.topology.supervisor.FabricTopologyListener
import org.burstsys.fabric.wave.data.model.limits.FabricSnapCacheLimits
import org.burstsys.fabric.wave.data.model.store.FabricStoreNameProperty
import org.burstsys.fabric.wave.data.worker.cache.{FabricSnapCache, FabricSnapCacheListener, burstModuleName => _}
import org.burstsys.fabric.wave.execution.model.execute.group.FabricGroupKey
import org.burstsys.fabric.wave.execution.model.gather.FabricGather
import org.burstsys.fabric.wave.execution.model.wave.{FabricParticle, FabricWave}
import org.burstsys.fabric.wave.metadata.model._
import org.burstsys.fabric.wave.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.wave.metadata.model.domain.FabricDomain
import org.burstsys.fabric.wave.metadata.model.view.FabricView
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.uid.newBurstUid

import java.util.concurrent.{CountDownLatch, Semaphore, TimeUnit}
import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

abstract class FabricWaveCacheLifecycleSpec extends FabricWaveSupervisorWorkerBaseSpec
  with FabricSnapCacheListener with FabricTopologyListener {

  val newWorkerGate = new CountDownLatch(1)

  override def wantsContainers = true

  override protected
  def beforeAll(): Unit = {
    snapCache withLimits mockLimits
    supervisorContainer.topology.talksTo(this)
    super.beforeAll()
    newWorkerGate.await(60, TimeUnit.SECONDS) should equal(true)
  }

  override protected
  def afterAll(): Unit = {
    super.afterAll()
  }

  private val tendCount = new Semaphore(0)

  override def onSnapCacheTend(cache: FabricSnapCache): Unit = {
    tendCount.release(1)
  }

  protected
  def waitForTend(): Unit = {
    log info s"TEND WAIT"
    tendCount.acquire(1)
    log info s"TEND ARRIVE"
  }

  private val startCount = new CountDownLatch(1)
  private val stopCount = new CountDownLatch(1)

  final val mockLimits = new MockLimits()

  final class MockLimits extends FabricSnapCacheLimits {
    def putMemoryLow(): Unit = {
      log info s"$marker marking memory as low"
      memoryUsageBelowLowWater = true
      memoryUsageAboveHighWater = false
    }

    def putMemoryHigh(): Unit = {
      log info s"$marker marking memory as high"
      memoryUsageBelowLowWater = false
      memoryUsageAboveHighWater = true
    }

    def putMemoryMiddle(): Unit = {
      log info s"$marker marking memory as middle"
      memoryUsageBelowLowWater = false
      memoryUsageAboveHighWater = false
    }

    def putDiskLow(): Unit = {
      log info s"$marker marking disk as low"
      diskUsageBelowLowWater = true
      diskUsageAboveHighWater = false
    }

    def putDiskHigh(): Unit = {
      log info s"$marker marking disk as high"
      diskUsageBelowLowWater = false
      diskUsageAboveHighWater = true
    }

    def putDiskMiddle(): Unit = {
      log info s"$marker marking disk as middle"
      diskUsageBelowLowWater = false
      diskUsageAboveHighWater = false
    }

    var memoryUsageBelowLowWater: Boolean = true
    var memoryUsageAboveHighWater: Boolean = false
    var diskUsageBelowLowWater: Boolean = true
    var diskUsageAboveHighWater: Boolean = false

    override def tendStartWait: Duration = 1 second

    override def tendPeriod: Duration = 1 second
  }

  protected
  def loadGeneration(domainKey: FabricDomainKey, viewKey: FabricViewKey, generationClock: FabricGenerationClock): Unit = {
    val guid = newBurstUid
    val promise = Promise[FabricGather]()
    // get an appropriate datasource
    val datasource = FabricDatasource(
      FabricDomain(domainKey = domainKey),
      FabricView(
        domainKey = domainKey, viewKey = viewKey, generationClock = generationClock,
        schemaName = BrioSchema("quo").name,
        storeProperties = Map(FabricStoreNameProperty -> MockStoreName)
      )
    )

    // handy dandy mock scanner
    val scanner = MockScanner(datasource.view.schemaName).initialize(
      FabricGroupKey(groupName = "mockgroup", groupUid = guid),
      datasource
    )

    def FAIL(t: Throwable): Unit = {
      log error s"FabricWaveCacheLifecycleSpec FAIL!! $t"
      promise.failure(t)
    }

    supervisorContainer.data.slices(guid, datasource) onComplete {
      case Failure(t) =>
        FAIL(t)
      case Success(slices) =>
        Try {
          // get appropriate set of slices and create particles out of them
          val particles = slices map (slice => FabricParticle(guid, slice, scanner))
          // create a wave from the particles
          FabricWave(guid, particles)
        } match {
          case Failure(t) =>
            FAIL(t)
          case Success(wave) =>
            supervisorContainer.execution.executionWaveOp(wave) onComplete {
              case Failure(t) =>
                FAIL(t)
              case Success(gather) =>
                promise.success(gather)
            }
        }
    }

    // execute the wave - wait for future - get back a gather
    Await.result(promise.future, 10 minutes)
  }

  override
  def onTopologyWorkerGained(worker: FabricTopologyWorker): Unit = {
    log info s"$marker added worker"
    newWorkerGate.countDown()
  }

  override def onSnapCacheStart(cache: FabricSnapCache): Unit = {
    startCount.countDown()
  }

  override def onSnapCacheStop(cache: FabricSnapCache): Unit = {
    stopCount.countDown()
  }

}
