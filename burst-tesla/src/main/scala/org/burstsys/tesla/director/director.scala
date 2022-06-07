/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla

import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.part.TeslaPartBuilder
import org.burstsys.vitals.logging._
import org.burstsys.vitals.reporter.VitalsByteQuantReporter

package object director extends VitalsLogger {

  final val partName: String = "director"

  object TeslaDirectorReporter extends VitalsByteQuantReporter("tesla","director")

}
