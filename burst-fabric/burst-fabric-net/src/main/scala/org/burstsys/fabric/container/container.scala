/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric

import org.burstsys.fabric.container.supervisor.FabricSupervisorContainer
import org.burstsys.fabric.container.worker.FabricWorkerContainer
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.logging.VitalsLogger
import org.burstsys.vitals.{VitalsService, reflection}

import scala.jdk.CollectionConverters._

package object container extends VitalsLogger {

  type FabricContainerId = Long

  /**
    * All services that are supervisor side
    */
  trait FabricSupervisorService extends VitalsService {
    def container: FabricSupervisorContainer[_]
  }

  /**
    * All services that are worker side
    */
  trait FabricWorkerService extends VitalsService {
    def container: FabricWorkerContainer[_]
  }

  final val SupervisorLog4JPropertiesFileName: String = "supervisor"
  final val WorkerLog4JPropertiesFileName: String = "worker"

}
