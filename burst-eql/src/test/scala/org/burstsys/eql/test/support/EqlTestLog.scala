/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.support

import org.burstsys.vitals.logging._
import org.burstsys.vitals.metrics.VitalsMetricsRegistry

trait EqlTestLog {

  VitalsMetricsRegistry.disable()

  VitalsLog.configureLogging("eql", true)
}
