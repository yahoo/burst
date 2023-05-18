/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys

import org.burstsys.fabric.net.FabricNetReporter
import org.burstsys.fabric.topology.supervisor.FabricTopologyReporter
import org.burstsys.vitals.logging.VitalsLogger
import org.burstsys.vitals.reporter.{VitalsReporter, VitalsReporterSource}

package object fabric extends VitalsReporterSource with VitalsLogger {

  override def reporters: Array[VitalsReporter] = Array(
    FabricNetReporter,
    FabricTopologyReporter
  )

}
