/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys

import org.burstsys.tesla.block.TeslaBlockReporter
import org.burstsys.tesla.buffer.TeslaBufferReporter
import org.burstsys.tesla.director.TeslaDirectorReporter
import org.burstsys.tesla.parcel.TeslaParcelReporter
import org.burstsys.tesla.scatter.TeslaScatterReporter
import org.burstsys.vitals.logging.VitalsLogger
import org.burstsys.vitals.reporter.VitalsReporter
import org.burstsys.vitals.reporter.VitalsReporterSource

package object tesla extends VitalsReporterSource with VitalsLogger {

  override def reporters: Array[VitalsReporter] = Array(
    TeslaScatterReporter,
    TeslaBlockReporter,
    TeslaDirectorReporter,
    TeslaParcelReporter,
    TeslaBufferReporter
  )

}
