/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys

import org.burstsys.hydra.runtime.HydraGatherReporter
import org.burstsys.vitals.logging._
import org.burstsys.vitals.reporter.VitalsReporter
import org.burstsys.vitals.reporter.VitalsReporterSource

package object hydra extends VitalsReporterSource with VitalsLogger {

  override def reporters: Array[VitalsReporter] = Array(
    HydraReporter,
    HydraGatherReporter
  )

}
