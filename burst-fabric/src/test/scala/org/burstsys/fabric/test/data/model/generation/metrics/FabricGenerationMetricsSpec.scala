/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.data.model.generation.metrics

import org.burstsys.fabric.configuration
import org.burstsys.fabric.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.data.model.generation.metrics.FabricGenerationMetrics
import org.burstsys.fabric.data.model.slice.state.FabricDataCold
import org.burstsys.fabric.test.FabricBaseSpec
import org.burstsys.vitals

class FabricGenerationMetricsSpec extends FabricBaseSpec {

  private val genKey = FabricGenerationKey(1, 2, 3)

  it should "compute SSR if data does not need to be sampled" in {
    val size = 100 * vitals.io.MB
    val items = 100
    val expectedItems = 100
    val potentialItems = 100
    val metrics = metricsWithItems(size, items, 0, expectedItems, potentialItems)

    val slices = Array(
      metrics,
      metricsWithItems(size, items, 0, expectedItems, potentialItems),
      metricsWithItems(size, items, 0, expectedItems, potentialItems),
      metricsWithItems(size, items, 0, expectedItems, potentialItems),
      metricsWithItems(size, items, 0, expectedItems, potentialItems),
    )


    metrics.finalizeWaveMetricsOnSupervisor(slices)

    metrics.sliceCount should equal(5)
    metrics.byteCount should equal(size * metrics.sliceCount)
    metrics.itemCount should equal(items * metrics.sliceCount)
    metrics.expectedItemCount should equal(items * metrics.sliceCount)
    metrics.potentialItemCount should equal(items * metrics.sliceCount)
    toNearestThousandth(metrics.suggestedSampleRate) should equal(toNearestThousandth(1))
    metrics.loadInvalid should equal(false)
  }

  it should "compute SSR if data was capped" in {
    val size = 100 * vitals.io.MB
    val items = 100
    val expectedItems = 120
    val potentialItems = 120
    val metrics = metricsWithItems(size, items, 0, expectedItems, potentialItems)

    val slices = Array(
      metrics,
      metricsWithItems(size, items, 0, expectedItems, potentialItems),
      metricsWithItems(size, items, 0, expectedItems, potentialItems),
      metricsWithItems(size, items, 0, expectedItems, potentialItems),
      metricsWithItems(size, items, 0, expectedItems, potentialItems),
    )

    metrics.finalizeWaveMetricsOnSupervisor(slices)

    metrics.sliceCount should equal(5)
    metrics.byteCount should equal(size * metrics.sliceCount)
    metrics.itemCount should equal(items * metrics.sliceCount)
    metrics.expectedItemCount should equal(expectedItems * metrics.sliceCount)
    metrics.potentialItemCount should equal(potentialItems * metrics.sliceCount)
    toNearestThousandth(metrics.suggestedSampleRate) should equal(toNearestThousandth(items.toDouble / potentialItems))
    metrics.loadInvalid should equal(true)
  }

  it should "compute SSR if data was sampled" in {
    val size = 100 * vitals.io.MB
    val items = 100
    val expectedItems = 100
    val potentialItems = 120
    val metrics = metricsWithItems(size, items, 0, expectedItems, potentialItems)

    val slices = Array(
      metrics,
      metricsWithItems(size, items, 0, expectedItems, potentialItems),
      metricsWithItems(size, items, 0, expectedItems, potentialItems),
      metricsWithItems(size, items, 0, expectedItems, potentialItems),
      metricsWithItems(size, items, 0, expectedItems, potentialItems),
    )

    metrics.finalizeWaveMetricsOnSupervisor(slices)

    metrics.sliceCount should equal(5)
    metrics.byteCount should equal(size * metrics.sliceCount)
    metrics.itemCount should equal(items * metrics.sliceCount)
    metrics.expectedItemCount should equal(expectedItems * metrics.sliceCount)
    metrics.potentialItemCount should equal(potentialItems * metrics.sliceCount)
    toNearestThousandth(metrics.suggestedSampleRate) should equal(toNearestThousandth(items.toDouble / potentialItems))
    metrics.loadInvalid should equal(false)
  }

  it should "compute SSR if data exceeded size limits" in {
    val maxBytes = configuration.burstFabricDatasourceMaxSizeProperty.getOrThrow
    val size = (maxBytes.toDouble / 4).toLong
    val items = 100
    val expectedItems = 100
    val potentialItems = 120
    val metrics = metricsWithItems(size, items, 0, expectedItems, potentialItems)

    val slices = Array(
      metrics,
      metricsWithItems(size, items, 0, expectedItems, potentialItems),
      metricsWithItems(size, items, 0, expectedItems, potentialItems),
      metricsWithItems(size, items, 0, expectedItems, potentialItems),
      metricsWithItems(size, items, 0, expectedItems, potentialItems),
    )

    metrics.finalizeWaveMetricsOnSupervisor(slices)

    metrics.sliceCount should equal(5)
    metrics.byteCount should equal(size * metrics.sliceCount)
    metrics.byteCount should be > maxBytes
    metrics.itemCount should equal(items * metrics.sliceCount)
    metrics.expectedItemCount should equal(expectedItems * metrics.sliceCount)
    metrics.potentialItemCount should equal(potentialItems * metrics.sliceCount)
    toNearestThousandth(metrics.suggestedSampleRate) should equal(toNearestThousandth(0.8 * items.toDouble / potentialItems))
    metrics.loadInvalid should equal(true)
  }

  private def metricsWithItems(bytes: Long, items: Long, rejectedItems: Long, expectedItems: Long, potentialItems: Long): FabricGenerationMetrics = {
    FabricGenerationMetrics().init(
      genKey, FabricDataCold,
      bytes, items, sliceCount = 1, regionCount = 1,
      coldLoadAt = 0, coldLoadTook = 0, warmLoadAt = 0, warmLoadTook = 0, warmLoadCount = 0,
      sizeSkew = 0, timeSkew = 0, itemSize = 0, itemVariation = 0,
      loadInvalid = false, earliestLoadAt = 0, rejectedItems, expectedItems, potentialItems,
      suggestedSampleRate = 0, suggestedSliceCount = 0
    )
  }

  private def toNearestThousandth(num: Double): Double = math.round(num * 1000).toDouble / 1000
}
