/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.test

import org.burstsys.fabric.configuration.burstHttpPortProperty
import org.burstsys.fabric.net.server.defaultFabricNetworkServerConfig
import org.burstsys.samplestore.api.client.SampleStoreApiClient
import org.burstsys.samplestore.store.container.supervisor.{SampleStoreFabricSupervisorContainer, SampleStoreFabricSupervisorContainerContext}
import org.burstsys.samplestore.store.container.worker.{SampleStoreFabricWorkerContainer, SampleStoreFabricWorkerContainerContext}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.annotation.unused

abstract class BaseSupervisorWorkerBaseSpec extends AnyFlatSpec with Suite with Matchers with BeforeAndAfterAll with BeforeAndAfterEach
  with BaseSpecLog {

  final val marker = "---------------------------->"

  protected def wantsContainers = false

  protected def workerCount = 1

  protected def configureSupervisor(supervisor: SampleStoreFabricSupervisorContainer): Unit = {}

  protected def configureWorker(@unused worker: SampleStoreFabricWorkerContainer): Unit = {}

  protected var supervisorContainer: SampleStoreFabricSupervisorContainer = {
    new SampleStoreFabricSupervisorContainerContext(defaultFabricNetworkServerConfig)
  }

  protected var storeServiceClient: SampleStoreApiClient = SampleStoreApiClient()

  protected var workerContainer1: SampleStoreFabricWorkerContainer = {
    // we mix supervisor and worker in the same JVM so move the health port
    val port = burstHttpPortProperty.get
    burstHttpPortProperty.set(port + 1)
    new SampleStoreFabricWorkerContainerContext(defaultFabricNetworkServerConfig)
  }

  protected var workerContainers: Array[SampleStoreFabricWorkerContainer] = Array.empty

  /**
   * Starts the containers for the test
   */
  override protected def beforeAll(): Unit = {
    if (!wantsContainers)
      return

    configureSupervisor(supervisorContainer)
    supervisorContainer.start
    storeServiceClient.start
    if (workerCount == 1) {
      configureWorker(workerContainer1)
      workerContainer1.start
    } else {
      workerContainers = (1 until workerCount + 1).indices.map({ _ =>
        // we are adding multiple workers in the same JVM so move the health port
        val port = burstHttpPortProperty.get
        burstHttpPortProperty.set(port + 1)

        val worker = new SampleStoreFabricWorkerContainerContext(defaultFabricNetworkServerConfig)
        configureWorker(worker)
        worker.start
      }).toArray
    }
  }

  /**
   * Stops any started containers
   */
  override protected def afterAll(): Unit = {
    supervisorContainer.stopIfNotAlreadyStopped
    workerContainer1.stopIfNotAlreadyStopped
    workerContainers.foreach(_.stopIfNotAlreadyStopped)
  }
}
