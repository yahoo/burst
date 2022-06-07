/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.test.reporter

import org.burstsys.tesla
import org.burstsys.tesla.test.support.TeslaSpecLog
import org.burstsys.vitals.reporter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import scala.language.postfixOps

class TeslaReporterSpec1 extends AnyFlatSpec with Matchers with TeslaSpecLog {

  it should "test hydra reporter without samples" in {
    reporter.startReporterSystem(samplePeriod = 1 second, reportPeriod = 1 second, waitPeriod = 1 second)
    Thread.sleep((10 seconds).toMillis)

  }

}
