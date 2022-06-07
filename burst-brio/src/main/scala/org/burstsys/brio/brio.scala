/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys

import org.burstsys.brio.dictionary.BrioDictionaryReporter
import org.burstsys.tesla.TeslaTypes.TeslaMemoryOffset
import org.burstsys.vitals.logging._
import org.burstsys.vitals.reporter.VitalsReporter
import org.burstsys.vitals.reporter.VitalsReporterSource

package object brio extends VitalsReporterSource with VitalsLogger {

  override def reporters: Array[VitalsReporter] = Array(
    BrioReporter,
    BrioDictionaryReporter
  )

  /**
   * The root of an object tree is always at offset zero. This may seem obvious, but it has tripped me
   * up many times...
   */
  final val BrioLatticeRoot: TeslaMemoryOffset = 0


}
