/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla

import org.burstsys.vitals.logging.VitalsLogger
import org.burstsys.vitals.reporter.VitalsByteQuantReporter

import scala.language.postfixOps

package object buffer  extends VitalsLogger {

  object TeslaBufferReporter extends VitalsByteQuantReporter("tesla","buffer")

  final val partName: String = "buffer"

  ////////////////////////////////////////////////////////
  // BLOB stuff - yeah it should not be in this package...
  ////////////////////////////////////////////////////////

  /**
    * this is a zap blob
    */
  final val BlobEncodingVersion2 = 2
}
