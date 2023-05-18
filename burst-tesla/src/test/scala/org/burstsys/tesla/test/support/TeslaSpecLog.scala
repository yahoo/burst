/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.test.support

import org.burstsys.vitals.logging._

/**
 * Configure test logging
 */
trait TeslaSpecLog extends VitalsLogger {

  VitalsLog.configureLogging("memory", consoleOnly = true)

}
