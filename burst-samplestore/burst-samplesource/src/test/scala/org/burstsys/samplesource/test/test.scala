/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource

import org.burstsys.vitals.logging._
import org.burstsys.vitals.metrics.VitalsMetricsRegistry
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

package object test extends VitalsLogger {
  class BaseSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll {
    VitalsMetricsRegistry.disable()
    VitalsLog.configureLogging("samplesource", consoleOnly = true)
  }
}
