/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.test

import org.burstsys.fabric.container.supervisor.{FabricSupervisorListener, MockSupervisorContainer}
import org.burstsys.fabric.container.worker.{FabricWorkerListener, MockWorkerContainer}
import org.burstsys.fabric.net.server.defaultFabricNetworkServerConfig
import org.burstsys.samplestore.store.container.supervisor.{FabricStoreSupervisorContainer, FabricStoreSupervisorContainerContext}
import org.burstsys.samplestore.store.container.worker.{FabricStoreWorkerContainer, FabricStoreWorkerContainerContext}
import org.burstsys.synthetic.samplestore.test.{MockTestSupervisorContainer, MockTestWorkerContainer, SyntheticSpecLog}
import org.burstsys.vitals.configuration.burstVitalsHealthCheckPortProperty
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}

abstract class BaseSupervisorWorkerBaseSpec extends AnyFlatSpec with Suite with Matchers with BeforeAndAfterAll with BeforeAndAfterEach
  with SyntheticSpecLog {

  final val marker = "---------------------------->"

  protected def wantsContainers = false

  protected def workerCount = 1

  protected def configureSupervisor(supervisor: FabricStoreSupervisorContainer): Unit = {}

  protected def configureWorker(worker: FabricStoreWorkerContainer): Unit = {}

  protected var supervisorContainer: FabricStoreSupervisorContainer = {
    new FabricStoreSupervisorContainerContext(defaultFabricNetworkServerConfig)
  }

  protected var workerContainer1: FabricStoreWorkerContainer = {
    // we mix supervisor and worker in the same JVM so move the health port
    val port = burstVitalsHealthCheckPortProperty.getOrThrow
    burstVitalsHealthCheckPortProperty.set(port + 1)
    new FabricStoreWorkerContainerContext()
  }

  protected var workerContainers: Array[FabricStoreWorkerContainer] = Array.empty

  /**
   * Starts the containers for the test
   */
  override protected def beforeAll(): Unit = {
    if (!wantsContainers)
      return

    configureSupervisor(supervisorContainer)
    supervisorContainer.start
    if (workerCount == 1) {
      configureWorker(workerContainer1)
      workerContainer1.start
    } else {
      workerContainers = (1 until workerCount + 1).indices.map({ i =>
        // we are adding multiple workers in the same JVM so move the health port
        val port = burstVitalsHealthCheckPortProperty.getOrThrow
        burstVitalsHealthCheckPortProperty.set(port + 1)

        val worker = new FabricStoreWorkerContainerContext()
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
