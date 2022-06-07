/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.relate.test.support

import org.burstsys.vitals.logging._
import org.apache.logging.log4j.Logger

trait BurstSqlSpecLog {

  VitalsLog.configureLogging("relate", true)

}
