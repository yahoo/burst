/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.logging

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.burstsys.vitals.errors._
import org.burstsys.vitals.net._

trait VitalsLogger {

  private val logName = getClass.getName.stripSuffix(".package$")

  final lazy val log: Logger =
    if (VitalsLog.isInitialized)
      LogManager.getLogger(logName)
    else
      throw VitalsException(s"Use of logger $logName before initializing logging system")

}
