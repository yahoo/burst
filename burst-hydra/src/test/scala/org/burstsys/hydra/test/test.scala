/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra

import org.burstsys.vitals.logging.{VitalsLog, VitalsLogger}

import scala.language.postfixOps

package object test extends VitalsLogger {

  VitalsLog.configureLogging("hydra", true)


}
