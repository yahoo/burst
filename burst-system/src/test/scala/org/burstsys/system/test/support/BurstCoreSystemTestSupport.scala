/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.test.support

import org.burstsys._
import org.burstsys.catalog.model.domain.CatalogDomain
import org.burstsys.fabric.configuration
import org.burstsys.fabric.configuration.burstHttpPortProperty
import org.burstsys.system.test.supervisor.BurstSystemTestSupervisorContainer
import org.burstsys.system.test.worker.BurstSystemTestWaveWorkerContainer
import org.burstsys.vitals.logging._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util.concurrent.TimeUnit
import scala.language.postfixOps

trait BurstCoreSystemTestSupport extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  VitalsLog.configureLogging("system", consoleOnly = true)

  final
  def domain: CatalogDomain = supervisorContainer.catalog.findDomainByMoniker("BurstSupervisorTestDomain").get

  vitals.configuration.configureForUnitTests()
  tesla.configuration.configureForUnitTests()
  fabric.wave.configuration.configureForUnitTests()
  configuration.burstFabricSupervisorStandaloneProperty.set(true)
  configuration.burstFabricWorkerStandaloneProperty.set(true)

  final
  val supervisorContainer: BurstSystemTestSupervisorContainer = fabric.wave.container.supervisorContainer.asInstanceOf[BurstSystemTestSupervisorContainer]

  final
  val workerContainer: BurstSystemTestWaveWorkerContainer = {
    // we mix supervisor and worker in the same JVM so move the health port
    val port = burstHttpPortProperty.get
    burstHttpPortProperty.set(port + 1)
    fabric.wave.container.workerContainer.asInstanceOf[BurstSystemTestWaveWorkerContainer]
  }

  val topoWatcher: TopologyWatcher = TopologyWatcher()

  override protected
  def beforeAll(): Unit = {

    org.burstsys.vitals.configuration.burstCellNameProperty.set("Cell1")

    supervisorContainer.topology talksTo topoWatcher

    supervisorContainer.containerId = 1
    workerContainer.containerId = 1

    supervisorContainer.start
    workerContainer.start

    // wait for the local worker to be available before trying anything
    topoWatcher.workerGainGate.await(30, TimeUnit.SECONDS)
    log info s"WORKER_FOUND_READY_TO_GO!"
  }

  override protected
  def afterAll(): Unit = {
    supervisorContainer.stop
    workerContainer.stop
  }
}
