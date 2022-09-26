/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.metrics

import org.burstsys.fabric.container.supervisor.MockSupervisorContainer
import org.burstsys.fabric.container.worker.MockWorkerContainer
import org.burstsys.fabric.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.metadata.model.domain.FabricDomain
import org.burstsys.fabric.metadata.model.view.FabricView
import org.burstsys.fabric.metadata.model.{FabricDomainKey, FabricGenerationClock, FabricMetadataLookup, FabricViewKey}
import org.burstsys.fabric.topology.supervisor.FabricTopologyListener
import org.burstsys.fabric.topology.model.node.supervisor.FabricSupervisor
import org.burstsys.fabric.topology.model.node.worker.{FabricWorker, FabricWorkerNode}
import org.burstsys.vitals.configuration.burstVitalsHealthCheckPortProperty
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostName}
import org.burstsys.vitals.properties.VitalsPropertyMap
import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec

import java.util.Date
import java.util.concurrent.CountDownLatch
import scala.util.{Failure, Success, Try}

abstract class FabricMetricsBaseSpec extends AnyFlatSpec with Suite with org.scalatest.matchers.should.Matchers with BeforeAndAfterAll with BeforeAndAfterEach
  with FabricMetadataLookup with FabricTopologyListener {

  val domainKey: FabricDomainKey = 1
  val viewKey: FabricViewKey = 1
  val generationClock: FabricGenerationClock = new Date().getTime


  val newWorkerGate = new CountDownLatch(2)

  final val marker = "---------------------------->"

  override
  def onTopologyWorkerGained(worker: FabricWorkerNode): Unit = {
    newWorkerGate.countDown()
  }

  val healthCheckPort: Int = burstVitalsHealthCheckPortProperty.getOrThrow

  protected var supervisorContainer: MockSupervisorContainer = MockSupervisorContainer(logFile = "fabric", containerId = 1)

  protected var workerContainer1: MockWorkerContainer = {
    val worker = MockWorkerContainer(logFile = "fabric", containerId = 2)
    worker.health.healthCheckPort = healthCheckPort + 1
    worker
  }

  protected var workerContainer2: MockWorkerContainer = {
    val worker = MockWorkerContainer(logFile = "fabric", containerId = 3)
    worker.health.healthCheckPort = healthCheckPort + 2
    worker
  }

  override protected def beforeAll(): Unit = {
    supervisorContainer.metadata withLookup this
    supervisorContainer.topology.talksTo(this)
    supervisorContainer.start
    workerContainer1.start
    workerContainer2.start

  }

  /**
   * Stops any started containers
   */
  override protected def afterAll(): Unit = {
    supervisorContainer.stop
    workerContainer1.stop
    workerContainer2.stop
  }

  override def domainLookup(key: FabricDomainKey): Try[FabricDomain] = ???

  override def viewLookup(key: FabricViewKey, validate: Boolean): Try[FabricView] = ???

  override def recordViewLoad(key: FabricGenerationKey, updatedProperties: VitalsPropertyMap): Try[Boolean] = ???
}
