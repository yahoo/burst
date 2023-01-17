/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test

import org.burstsys.fabric.configuration.burstHttpPortProperty
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
import scala.util.{Random, Try}

abstract class FabricWaveSupervisorWorkerBaseSpec extends AnyFlatSpec with Suite with Matchers with BeforeAndAfterAll with BeforeAndAfterEach
  with FabricSpecLog with FabricMetadataLookup with FabricSnapCacheListener with FabricTopologyListener {

  final val marker = "---------------------------->"

  protected def wantsContainers = false

  protected def workerCount = 1

  protected def configureSupervisor(supervisor: MockWaveSupervisorContainer): Unit = {}

  protected def configureWorker(worker: MockWaveWorkerContainer): Unit = {}

  private var port = burstHttpPortProperty.get
  protected var supervisorContainer: MockWaveSupervisorContainer = {
    port = (port + (Random.nextInt().abs % 1000)) & 0xffff
    burstHttpPortProperty.set(port)
    MockWaveSupervisorContainer(logFile = "fabric", containerId = 1)
  }

  protected var workerContainer1: MockWaveWorkerContainer = {
    // we mix supervisor and worker in the same JVM so move the health port
    burstHttpPortProperty.set(port + 1)
    MockWaveWorkerContainer(logFile = "fabric", containerId = 1)
  }

  protected var workerContainers = Array.empty[MockWaveWorkerContainer]

  protected var workerConnectionGate: CountDownLatch = new CountDownLatch(workerCount)

  def snapCache: FabricSnapCache = workerContainer1.data.cache

  /**
   * Starts the containers for the test
   */
  override protected def beforeAll(): Unit = {
    if (!wantsContainers)
      return

    supervisorContainer.metadata withLookup this
    supervisorContainer.topology talksTo this

    snapCache talksTo this

    configureSupervisor(supervisorContainer)
    supervisorContainer.start
    workerConnectionGate = new CountDownLatch(workerCount)

    if (workerCount == 1) {
      configureWorker(workerContainer1)
      workerContainer1.start
    } else {
      workerContainers = (1 until workerCount + 1).indices.map({ i =>
        // we are adding multiple workers in the same JVM so move the health port
        val port = burstHttpPortProperty.get
        burstHttpPortProperty.set(port + 1)
        val worker = MockWaveWorkerContainer(logFile = "fabric", containerId = i)
        configureWorker(worker)
        worker.start
      }).toArray
    }
    workerConnectionGate.await(5, TimeUnit.SECONDS)
  }

  /**
   * Stops any started containers
   */
  override protected def afterAll(): Unit = {
    supervisorContainer.stopIfNotAlreadyStopped
    workerContainer1.stopIfNotAlreadyStopped
    workerContainers.foreach(_.stopIfNotAlreadyStopped)
  }

  override def domainLookup(key: FabricDomainKey): Try[FabricDomain] = ???

  override def viewLookup(key: FabricViewKey, validate: Boolean): Try[FabricView] = ???

  override def recordViewLoad(key: FabricGenerationKey, updatedProperties: VitalsPropertyMap): Try[Boolean] = ???

  override def onTopologyWorkerGain(worker: FabricTopologyWorker): Unit = workerConnectionGate.countDown()
}
