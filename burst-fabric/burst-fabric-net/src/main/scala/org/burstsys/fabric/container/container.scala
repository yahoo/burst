/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric

import org.burstsys.fabric.configuration.burstHttpPortProperty
import org.burstsys.fabric.container.supervisor.FabricSupervisorContainer
import org.burstsys.fabric.container.worker.FabricWorkerContainer
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.healthcheck.VitalsHealthMonitoredService
import org.burstsys.vitals.logging.VitalsLogger
import org.burstsys.vitals.{VitalsService, reflection}

import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicInteger
import scala.jdk.CollectionConverters._
import scala.util.Random

package object container extends VitalsLogger {

  type FabricContainerId = Long

  /**
    * All services that are supervisor side
    */
  trait FabricSupervisorService extends VitalsHealthMonitoredService {
    def container: FabricSupervisorContainer[_]
  }

  /**
    * All services that are worker side
    */
  trait FabricWorkerService extends VitalsHealthMonitoredService {
    def container: FabricWorkerContainer[_]
  }

  final val SupervisorLog4JPropertiesFileName: String = "supervisor"
  final val WorkerLog4JPropertiesFileName: String = "worker"

  def getNextHttpPort: Int = {
    val socket = new ServerSocket(0)
    val port = socket.getLocalPort
    socket.close()
    port
  }

}
