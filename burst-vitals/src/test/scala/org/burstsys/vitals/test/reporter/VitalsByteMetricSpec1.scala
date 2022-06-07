/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.test.reporter

import org.burstsys.vitals
import org.burstsys.vitals.reporter.metric.VitalsReporterByteOpMetric
import org.burstsys.vitals.test.VitalsAbstractSpec
import org.scalatest.Ignore

import scala.concurrent.duration._
import scala.language.postfixOps


@Ignore
class VitalsByteMetricSpec1 extends VitalsAbstractSpec {

  vitals.reporter.startReporterSystem(1 second, 1 second, 1 second)

  it should "test omni ops only" in {
    val m1 = VitalsReporterByteOpMetric("m1")
    m1.report should equal("")
    for (i <- 0 until 10) {
      m1.recordOp()
      Thread.sleep((1 second).toMillis)
    }
    log info s" \n*** ops only \n${m1.report}****"
  }

  it should "test omni ops/times" in {
    val m1 = VitalsReporterByteOpMetric("m1")
    m1.report should equal("")
    for (i <- 0 until 10) {
      m1.recordOpWithTime(ns = 1000 * i)
      Thread.sleep((1 second).toMillis)
    }
    log info s" \n*** ops/times \n${m1.report}****"
  }

  it should "test omni ops/sizes" in {
    val m1 = VitalsReporterByteOpMetric("m1")
    m1.report should equal("")
    for (i <- 0 until 10) {
      m1.recordOpWithSize(bytes = 1e6.toLong * i)
      Thread.sleep((1 second).toMillis)
    }
    log info s" \n*** ops/sizes \n${m1.report}****"
  }

  it should "test omni ops/times/sizes" in {
    val m1 = VitalsReporterByteOpMetric("m1")
    m1.report should equal("")
    for (i <- 0 until 10) {
      m1.recordOpWithTimeAndSize(ns = 1000 * i, bytes = 1e6.toLong)
      Thread.sleep((1 second).toMillis)
    }
    log info s" \n*** ops/times/sizes \n${m1.report}****"
  }

}
