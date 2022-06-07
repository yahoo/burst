/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.test.reporter

import org.burstsys.vitals
import org.burstsys.vitals.reporter
import org.burstsys.vitals.test.VitalsAbstractSpec

import scala.concurrent.duration._
import scala.language.postfixOps

class VitalsMetricSpec1 extends VitalsAbstractSpec {

  it should "test metrics with out samples" in {

    reporter.startReporterSystem(1 second, 1 second, 1 second, 1 minute)

    Thread.sleep((4 second).toMillis)

  }

}
