/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.worker

import org.burstsys.fabric.configuration
import org.burstsys.fabric.container.FabricContainerId
import org.burstsys.fabric.topology.model.node.worker.FabricWorker
import org.burstsys.tesla.part.factory.TeslaFactoryBoss
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.git
import org.burstsys.vitals.logging._
import org.burstsys.vitals.metrics.VitalsMetricsRegistry
import org.burstsys.vitals.net.{convertHostnameToAddress, getPublicHostAddress, getPublicHostName}
import org.burstsys.{fabric, tesla, vitals}
import org.burstsys.vitals.logging._

/**
  * A container for unit tests
  */
trait MockWorkerContainer extends FabricWorkerContainer {

}

object MockWorkerContainer {

  /**
    * constructor for situations where you know your container id in advance (such as unit tests)
    *
    * @param logFile
    * @param containerId
    * @return
    */
  def apply(logFile: String, containerId: FabricContainerId): MockWorkerContainer = {
    configuration.burstFabricWorkerStandaloneProperty.set(true)
    vitals.configuration.configureForUnitTests()
    tesla.configuration.configureForUnitTests()
    fabric.configuration.configureForUnitTests()
    val c = MockWorkerContainerContext(logFile: String)
    c.containerId = containerId
    c
  }

  /**
    * constructor for when you do not know your container id in advance
    *
    * @param logFile
    * @return
    */
  def apply(logFile: String): MockWorkerContainer = {
    configuration.burstFabricWorkerStandaloneProperty.set(true)
    MockWorkerContainerContext(logFile: String)
  }

}

private final case
class MockWorkerContainerContext(logFile: String) extends FabricWorkerContainerContext
  with MockWorkerContainer {

  override def serviceName: String = s"mock-worker-container"

  override def log4JPropertiesFileName: String = logFile

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    try {
      ensureNotRunning

      git.turnOffBuildValidation()
      VitalsMetricsRegistry.disable()

      VitalsLog.configureLogging(log4JPropertiesFileName, consoleOnly = true)

      log info startingMessage
      super.start
      log info startedWithDateMessage

      markRunning
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        throw t
    }
    this
  }

  override
  def stop: this.type = {
    ensureRunning
    log info stoppingMessage

    // stop fabric layer
    super.stop

    markNotRunning
    log info stoppedWithDateMessage
    TeslaFactoryBoss.assertNoInUseParts()
    this
  }

  override
  def registerWorker(): FabricWorker = {
    containerId match {
      case None =>
        super.registerWorker()
      case Some(id) =>
        // fakeout worker with id...
        val name = getPublicHostName
        FabricWorker(id, moniker = name, nodeName = name, getPublicHostAddress)
    }
  }

}
