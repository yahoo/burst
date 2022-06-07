/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.reporter

import org.burstsys.hydra
import org.burstsys.hydra.HydraReporter
import org.burstsys.vitals.logging._
import org.burstsys.vitals.reporter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{Ignore, Suite}

import scala.concurrent.duration._
import scala.language.postfixOps

@Ignore
class HydraReporterSpec1 extends AnyFlatSpec with Suite with Matchers {

  VitalsLog.configureLogging("hydra", consoleOnly = true)

  it should "test hydra reporter with samples" in {
    reporter.startReporterSystem(samplePeriod = 1 second, reportPeriod = 1 second, waitPeriod = 1 second)

    HydraReporter.recordParse(10, "\n\n\n")

    Thread.sleep((10 seconds).toMillis)

  }
}
