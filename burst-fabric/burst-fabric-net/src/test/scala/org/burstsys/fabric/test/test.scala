/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric

import org.burstsys.fabric.container.supervisor.{FabricSupervisorListener, MockSupervisorContainer}
import org.burstsys.fabric.container.worker.{FabricWorkerListener, MockWorkerContainer}
import org.burstsys.vitals.logging.{VitalsLog, VitalsLogger}

package object test extends VitalsLogger {

  trait FabricSpecLog {
    VitalsLog.configureLogging("fabric-net", consoleOnly = true)
  }

  type MockTestSupervisorContainer = MockSupervisorContainer[FabricSupervisorListener]

  type MockTestWorkerContainer = MockWorkerContainer[FabricWorkerListener]

}
