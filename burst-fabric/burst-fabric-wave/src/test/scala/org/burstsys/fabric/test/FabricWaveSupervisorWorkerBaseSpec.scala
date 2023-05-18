/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test

import org.burstsys.fabric
import org.burstsys.fabric.configuration.burstHttpPortProperty
import org.burstsys.fabric.container
import org.burstsys.fabric.topology.FabricTopologyWorker
import org.burstsys.fabric.topology.supervisor.FabricTopologyListener
import org.burstsys.fabric.wave.container.supervisor.MockWaveSupervisorContainer
import org.burstsys.fabric.wave.container.worker.MockWaveWorkerContainer
import org.burstsys.fabric.wave.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.wave.data.worker.cache.FabricSnapCache
import org.burstsys.fabric.wave.data.worker.cache.FabricSnapCacheListener
import org.burstsys.fabric.wave.metadata.model.domain.FabricDomain
import org.burstsys.fabric.wave.metadata.model.view.FabricView
import org.burstsys.fabric.wave.metadata.model.FabricDomainKey
import org.burstsys.fabric.wave.metadata.model.FabricMetadataLookup
import org.burstsys.fabric.wave.metadata.model.FabricViewKey
import org.burstsys.vitals.properties.VitalsPropertyMap
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterAll
import org.scalatest.BeforeAndAfterEach
import org.scalatest.Suite

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Random, Try}

abstract class FabricWaveSupervisorWorkerBaseSpec extends AnyFlatSpec with Suite with Matchers with BeforeAndAfterAll with BeforeAndAfterEach
  with FabricSpecLog with FabricMetadataLookup with FabricSnapCacheListener with FabricTopologyListener {

  final val marker = "---------------------------->"
  final val beginMarker = "vvvvvvvvvvvvvvvvvvvvvvvvvvvv"
  final val endMarker = "^^^^^^^^^^^^^^^^^^^^^^^^^^^^"

  protected def wantsContainers = false

  protected def workerCount = 1

  protected def configureSupervisor(supervisor: MockWaveSupervisorContainer): Unit = {}

  protected def configureWorker(worker: MockWaveWorkerContainer): Unit = {}

  protected var supervisorContainer: MockWaveSupervisorContainer = if (wantsContainers) {
    burstHttpPortProperty.set(container.getNextHttpPort)
    MockWaveSupervisorContainer(logFile = "fabric", containerId = 1)
  } else null

  protected var workerContainer1: MockWaveWorkerContainer = if (wantsContainers) {
    // we mix supervisor and worker in the same JVM so move the health port
    burstHttpPortProperty.set(container.getNextHttpPort)
    MockWaveWorkerContainer(logFile = "fabric", containerId = 1)
  } else null

  protected var workerContainers = Array.empty[MockWaveWorkerContainer]

  protected var workerConnectionGate: CountDownLatch = new CountDownLatch(workerCount)

  def snapCache: FabricSnapCache = workerContainer1.data.cache


  private val initHeartbeatPeriod = fabric.configuration.burstFabricTopologyHeartbeatPeriodMs.get

  /**
   * Starts the containers for the test
   */
  override protected def beforeAll(): Unit = {
    log debug s"$beginMarker beforeAll $suiteName containers=$wantsContainers workers=$workerCount $beginMarker"
    if (!wantsContainers) {
      log debug s"$endMarker beforeAll $suiteName $endMarker"
      return
    }

    fabric.configuration.burstFabricTopologyHeartbeatPeriodMs.set(100 milliseconds)
    supervisorContainer.metadata withLookup this
    supervisorContainer.topology talksTo this

    snapCache talksTo this

    configureSupervisor(supervisorContainer)
    supervisorContainer.start
    workerConnectionGate = new CountDownLatch(workerCount)
    log debug s"$marker expected workers=${workerConnectionGate.getCount}"

    if (workerCount == 1) {
      configureWorker(workerContainer1)
      workerContainer1.start
    } else {
      workerContainers = (1 until workerCount + 1).map({ i =>
        // we are adding multiple workers in the same JVM so move the health port
        burstHttpPortProperty.set(container.getNextHttpPort)
        val worker = MockWaveWorkerContainer(logFile = "fabric", containerId = i)
        configureWorker(worker)
        worker.start
      }).toArray
    }
    log debug "waiting for workers to connect"
    val allConnected = workerConnectionGate.await(5, TimeUnit.SECONDS)
    log debug s"$endMarker beforeAll $suiteName allWorkersConnected=$allConnected $endMarker"
  }

  /**
   * Stops any started containers
   */
  override protected def afterAll(): Unit = {
    log debug s"$beginMarker afterAll $suiteName $beginMarker"
    fabric.configuration.burstFabricTopologyHeartbeatPeriodMs.set(initHeartbeatPeriod)
    if (wantsContainers) {
      supervisorContainer.stopIfNotAlreadyStopped
      workerContainer1.stopIfNotAlreadyStopped
      workerContainers.foreach(_.stopIfNotAlreadyStopped)
    }
    log debug s"$endMarker afterAll $suiteName $endMarker"
  }

  override def domainLookup(key: FabricDomainKey): Try[FabricDomain] = ???

  override def viewLookup(key: FabricViewKey, validate: Boolean): Try[FabricView] = ???

  override def recordViewLoad(key: FabricGenerationKey, updatedProperties: VitalsPropertyMap): Try[Boolean] = ???

  override def onTopologyWorkerGained(worker: FabricTopologyWorker): Unit = {
    workerConnectionGate.countDown()
    log debug s"$suiteName gained worker remaining=${workerConnectionGate.getCount} $worker"
  }
}
