/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.json.samplestore

import org.burstsys.vitals.logging.VitalsLogger
import org.burstsys.vitals.reporter.VitalsReporter
import org.burstsys.vitals.reporter.VitalsReporterSource

package object master extends VitalsReporterSource with VitalsLogger {

  override def reporters: Array[VitalsReporter] = Array()

}
