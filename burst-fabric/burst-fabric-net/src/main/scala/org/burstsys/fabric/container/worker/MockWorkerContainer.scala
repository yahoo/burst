/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.worker

import org.burstsys.fabric.configuration
import org.burstsys.fabric.container.FabricContainerId
import org.burstsys.tesla.part.factory.TeslaFactoryBoss
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.git
import org.burstsys.vitals.logging._
import org.burstsys.vitals.metrics.VitalsMetricsRegistry
import org.burstsys.{tesla, vitals}

/**
  * A container for unit tests
  */
trait MockWorkerContainer[T <: FabricWorkerListener] extends FabricWorkerContainer[T]

object MockWorkerContainer {
  /**
    * constructor for situations where you know your container id in advance (such as unit tests)
    */
  def apply[T <: FabricWorkerListener](logFile: String, containerId: FabricContainerId): MockWorkerContainer[T] = {
    configuration.burstFabricWorkerStandaloneProperty.set(true)
    vitals.configuration.configureForUnitTests()
    tesla.configuration.configureForUnitTests()
    val c = MockWorkerContainerContext[T](logFile: String)
    c.containerId = containerId
    c
  }

  /**
    * constructor for when you do not know your container id in advance
    */
  def apply[T <: FabricWorkerListener](logFile: String): MockWorkerContainer[T] = {
    configuration.burstFabricWorkerStandaloneProperty.set(true)
    MockWorkerContainerContext[T](logFile: String)
  }

}

private final case
class MockWorkerContainerContext[T <: FabricWorkerListener](logFile: String) extends FabricWorkerContainerContext[T]
  with MockWorkerContainer[T] {

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

}
