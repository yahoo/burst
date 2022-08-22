/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test

import org.burstsys.fabric.container.master.MockMasterContainer
import org.burstsys.fabric.container.worker.MockWorkerContainer
import org.burstsys.fabric.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.data.worker.cache.{FabricSnapCache, FabricSnapCacheListener}
import org.burstsys.fabric.metadata.model.domain.FabricDomain
import org.burstsys.fabric.metadata.model.view.FabricView
import org.burstsys.fabric.metadata.model.{FabricDomainKey, FabricMetadataLookup, FabricViewKey}
import org.burstsys.fabric.topology.model.node.master.FabricMaster
import org.burstsys.fabric.topology.model.node.worker.FabricWorker
import org.burstsys.vitals.configuration.burstVitalsHealthCheckPortProperty
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostName}
import org.burstsys.vitals.properties.VitalsPropertyMap
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}

import scala.util.{Failure, Success, Try}

abstract class FabricMasterWorkerBaseSpec extends AnyFlatSpec with Suite with Matchers with BeforeAndAfterAll with BeforeAndAfterEach
  with FabricSpecLog with FabricMetadataLookup with FabricSnapCacheListener {

  final val marker = "---------------------------->"

  protected def wantsContainers = false

  protected def workerCount = 1

  protected def configureMaster(master: MockMasterContainer): Unit = {}

  protected def configureWorker(worker: MockWorkerContainer): Unit = {}

  protected var masterContainer: MockMasterContainer = MockMasterContainer(logFile = "fabric", containerId = 1)

  protected var workerContainer1: MockWorkerContainer = {
    // we mix master and worker in the same JVM so move the health port
    val port = burstVitalsHealthCheckPortProperty.getOrThrow
    burstVitalsHealthCheckPortProperty.set(port + 1)
    MockWorkerContainer(logFile = "fabric", containerId = 1)
  }

  protected var workerContainers = Array.empty[MockWorkerContainer]

  def snapCache: FabricSnapCache = workerContainer1.data.cache

  /**
   * Starts the containers for the test
   */
  override protected def beforeAll(): Unit = {
    if (!wantsContainers)
      return

    masterContainer.metadata withLookup this

    snapCache talksTo this

    configureMaster(masterContainer)
    masterContainer.start
    if (workerCount == 1) {
      configureWorker(workerContainer1)
      workerContainer1.start
    } else {
      workerContainers = (1 until workerCount + 1).indices.map({ i =>
        // we are adding multiple workers in the same JVM so move the health port
        val port = burstVitalsHealthCheckPortProperty.getOrThrow
        burstVitalsHealthCheckPortProperty.set(port + 1)
        val worker = MockWorkerContainer(logFile = "fabric", containerId = i)
        configureWorker(worker)
        worker.start
      }).toArray
    }
  }

  /**
   * Stops any started containers
   */
  override protected def afterAll(): Unit = {
    masterContainer.stopIfNotAlreadyStopped
    workerContainer1.stopIfNotAlreadyStopped
    workerContainers.foreach(_.stopIfNotAlreadyStopped)
  }

  override def domainLookup(key: FabricDomainKey): Try[FabricDomain] = ???

  override def viewLookup(key: FabricViewKey, validate: Boolean): Try[FabricView] = ???

  override def recordViewLoad(key: FabricGenerationKey, updatedProperties: VitalsPropertyMap): Try[Boolean] = ???
}
