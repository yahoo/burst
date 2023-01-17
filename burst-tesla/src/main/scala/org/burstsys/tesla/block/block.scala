/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla

import org.burstsys.tesla.TeslaTypes.{SizeOfInteger, TeslaMemorySize}
import org.burstsys.vitals.logging._
import org.burstsys.vitals.reporter.VitalsByteQuantReporter

package object block extends VitalsLogger {

  final val partName: String = "block"

  final
  val SizeofBlockHeader: TeslaMemorySize = SizeOfInteger + SizeOfInteger + SizeOfInteger

  object TeslaBlockReporter extends VitalsByteQuantReporter("tesla", "block")

}
