/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.test

import org.burstsys.felt
import org.burstsys.vitals.logging._
import org.burstsys.vitals.reporter
import org.scalatest.Suite
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import scala.language.postfixOps

class FeltReporterSpec1 extends AnyFlatSpec with Suite with Matchers {

  VitalsLog.configureLogging("hydra", true)


  it should "test felt report no samples" in {
    reporter.startReporterSystem(samplePeriod = 1 second, reportPeriod = 1 second, waitPeriod = 1 second)

    Thread.sleep((4 seconds).toMillis)
  }
}
