/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.ginsu.test.support

import org.burstsys.vitals.logging._
import org.apache.logging.log4j.Logger

trait GinsuSpecLog {

  VitalsLog.configureLogging("ginsu", true)

}
