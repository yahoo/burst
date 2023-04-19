/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test

import org.burstsys.fabric.configuration.burstHttpPortProperty
import org.burstsys.fabric.container
import org.burstsys.fabric.container.supervisor.{FabricSupervisorListener, MockSupervisorContainer}
import org.burstsys.fabric.container.worker.{FabricWorkerListener, MockWorkerContainer}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

abstract class FabricSupervisorWorkerBaseSpec extends AnyFlatSpec with Suite with Matchers with BeforeAndAfterAll with BeforeAndAfterEach
  with FabricSpecLog {

  final val marker = "---------------------------->"
  final val beginMarker = "vvvvvvvvvvvvvvvvvvvvvvvvvvvv"
  final val endMarker = "^^^^^^^^^^^^^^^^^^^^^^^^^^^^"

  protected def wantsContainers = false

  protected def workerCount = 1

  protected def configureSupervisor(supervisor: MockTestSupervisorContainer): Unit = {}

  protected def configureWorker(worker: MockTestWorkerContainer): Unit = {}

  protected var supervisorContainer: MockTestSupervisorContainer = {
    burstHttpPortProperty.set(container.getNextHttpPort)
    MockSupervisorContainer[FabricSupervisorListener](logFile = "fabric", containerId = 1)
  }

  protected var workerContainer1: MockTestWorkerContainer = {
    // we mix supervisor and worker in the same JVM so move the health port
    burstHttpPortProperty.set(container.getNextHttpPort)
    MockWorkerContainer[FabricWorkerListener](logFile = "fabric", containerId = 1)
  }

  protected var workerContainers = Array.empty[MockTestWorkerContainer]

  /**
   * Starts the containers for the test
   */
  override protected def beforeAll(): Unit = {
    log debug s"$beginMarker before all $beginMarker"
    if (!wantsContainers) {
      log debug s"$endMarker before all $endMarker"
      return
    }

    configureSupervisor(supervisorContainer)
    supervisorContainer.start
    if (workerCount == 1) {
      configureWorker(workerContainer1)
      workerContainer1.start
    } else {
      workerContainers = (1 until workerCount + 1).indices.map({ i =>
        // we are adding multiple workers in the same JVM so move the health port
        burstHttpPortProperty.set(container.getNextHttpPort)
        val worker = MockWorkerContainer[FabricWorkerListener](logFile = "fabric", containerId = i)
        configureWorker(worker)
        log debug s"starting worker#$i"
        worker.start
      }).toArray
    }
    log debug s"$endMarker before all $endMarker"

  }

  /**
   * Stops any started containers
   */
  override protected def afterAll(): Unit = {
    log debug s"$beginMarker after all $beginMarker"
    supervisorContainer.stopIfNotAlreadyStopped
    workerContainer1.stopIfNotAlreadyStopped
    log debug "stopping workers"
    workerContainers.foreach(_.stopIfNotAlreadyStopped)
    log debug s"$endMarker after all $endMarker"
  }
}
