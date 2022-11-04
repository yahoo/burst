/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test

import org.burstsys.fabric.container.supervisor.{FabricSupervisorListener, MockSupervisorContainer}
import org.burstsys.fabric.container.worker.{FabricWorkerListener, MockWorkerContainer}
import org.burstsys.vitals.configuration.burstVitalsHealthCheckPortProperty
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}

abstract class FabricSupervisorWorkerBaseSpec extends AnyFlatSpec with Suite with Matchers with BeforeAndAfterAll with BeforeAndAfterEach
  with FabricSpecLog {

  final val marker = "---------------------------->"

  protected def wantsContainers = false

  protected def workerCount = 1

  protected def configureSupervisor(supervisor: MockTestSupervisorContainer): Unit = {}

  protected def configureWorker(worker: MockTestWorkerContainer): Unit = {}

  protected var supervisorContainer: MockTestSupervisorContainer = {
    MockSupervisorContainer[FabricSupervisorListener](logFile = "fabric", containerId = 1)
  }

  protected var workerContainer1: MockTestWorkerContainer = {
    // we mix supervisor and worker in the same JVM so move the health port
    val port = burstVitalsHealthCheckPortProperty.getOrThrow
    burstVitalsHealthCheckPortProperty.set(port + 1)
    MockWorkerContainer[FabricWorkerListener](logFile = "fabric", containerId = 1)
  }

  protected var workerContainers = Array.empty[MockTestWorkerContainer]

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
        val worker = MockWorkerContainer[FabricWorkerListener](logFile = "fabric", containerId = i)
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
