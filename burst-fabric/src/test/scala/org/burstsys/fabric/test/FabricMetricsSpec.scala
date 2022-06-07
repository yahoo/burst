/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test

import org.burstsys.fabric.container.model.metrics.FabricLastHourMetricCollector
import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import scala.language.postfixOps

@Ignore
class FabricMetricsSpec extends AnyFlatSpec with Suite with Matchers with BeforeAndAfterAll with FabricSpecLog {

  "Fabric Metrics" should "do a single sample correctly" in {
    val collector = FabricLastHourMetricCollector().initialize
    collector.sample(1, 0)
    collector.export.history.map(t => (t.value, t.time)) should equal(Array(
      (1, 0))
    )
  }

  "Fabric Metrics" should "do two samples in same quantum bucket correctly" in {
    val collector = FabricLastHourMetricCollector().initialize
    collector.sample(1, 0)
    collector.sample(4, 0)
    collector.export.history.map(t => (t.value, t.time)) should equal(Array(
      (2.5, 0)
    ))
  }

  "Fabric Metrics" should "do two samples in two different quantum buckets correctly" in {
    val collector = FabricLastHourMetricCollector().initialize
    collector.sample(1.1, 0)
    collector.sample(23.5, (5 minutes).toMillis)
    collector.export.history.map(t => (t.value, t.time)) should equal(Array(
      (1.1, 0),
      (23.5, (5 minutes).toMillis)
    ))
  }

  "Fabric Metrics" should "do four samples in two different quantum buckets correctly" in {
    val collector = FabricLastHourMetricCollector().initialize
    collector.sample(1.1000, 0)
    collector.sample(4.6000, (1 minute).toMillis)
    collector.sample(23.5, (5 minutes).toMillis)
    collector.sample(23.5, (5 minutes).toMillis + (1 minute).toMillis)
    collector.export.history.map(t => (t.value, t.time)) should equal(Array(
      (2.8499999999999996, 0),
      (23.5, (5 minutes).toMillis)
    ))
  }


  "Fabric Metrics" should "do 20 samples in 20 different quantum buckets correctly" in {
    val collector = FabricLastHourMetricCollector(4, 10 milliseconds).initialize
    var nowTime = 0L
    var value = 0.0
    for (i <- 0 until 5) {
      collector.sample(value, nowTime)
      nowTime = nowTime + 10
      value += 1.0
    }
    collector.export.history.map(t => (t.value, t.time)) should equal(Array(
      (1.0, 10), (2.0, 20), (3.0, 30), (4.0, 40)
    ))
  }


}
