/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.synthetic.samplestore.service

import org.burstsys.brio
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.provider.BrioSyntheticDataProvider
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.samplesource.service.SampleSourceWorkerService
import org.burstsys.synthetic.samplestore.configuration.defaultItemCountProperty
import org.burstsys.synthetic.samplestore.configuration.defaultMaxItemSizeProperty
import org.burstsys.synthetic.samplestore.configuration.defaultPressTimeoutProperty
import org.burstsys.synthetic.samplestore.configuration.itemCountProperty
import org.burstsys.synthetic.samplestore.configuration.maxItemSizeProperty
import org.burstsys.synthetic.samplestore.configuration.pressTimeoutProperty
import org.burstsys.synthetic.samplestore.configuration.syntheticDatasetProperty
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.tesla.thread.request.teslaRequestExecutor
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.properties._

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.Future
import scala.concurrent.TimeoutException
import scala.language.postfixOps
import scala.util.Failure
import scala.util.Success

case class SyntethicSampleSourceWorker() extends SampleSourceWorkerService {

  /**
   * @return The name of this sample source worker.
   */
  override def name: String = SynteticSampleSourceName

  /**
   * Instantiate an instance of a synthetic data provider and use it to feed the stream.
   *
   * @param stream the stream of the incoming request
   * @return a future that completes when the stream has been fed
   */
  override def feedStream(stream: NexusStream): Future[Unit] = {
    TeslaRequestFuture {
      val timeout = stream.getOrDefault(pressTimeoutProperty, defaultPressTimeoutProperty.getOrThrow)
      try {
        val modelName = stream.get[String](syntheticDatasetProperty)
        val dataProvider = BrioSyntheticDataProvider.providerNamed(modelName) // invoke data model with item count
        if (!(stream.schema equalsIgnoreCase dataProvider.schemaName)) {
          throw VitalsException(s"Stream and synthetic data provider do not specify the same schema. stream=${stream.schema} provider=${dataProvider.schemaName}")
        }

        val schema = BrioSchema(dataProvider.schemaName)
        val maxItemSize = stream.getOrDefault(maxItemSizeProperty, defaultMaxItemSizeProperty.getOrThrow)
        val itemCount = stream.getOrDefault(itemCountProperty, defaultItemCountProperty.getOrThrow)
        val rejectedItemCounter = new AtomicInteger()

        val start = System.nanoTime
        val finished = new CountDownLatch(itemCount)

        dataProvider.data(itemCount, stream.properties).foreach(item => {
          val pressSource = dataProvider.pressSource(item)
          val pressedItem = brio.press.pipeline.pressToFuture(stream.guid, pressSource, schema, item.schemaVersion, maxItemSize)
          pressedItem
            .map({ buffer => stream.put(buffer) })
            .recover({ case _ => rejectedItemCounter.incrementAndGet() })
            .andThen({ case _ => finished.countDown() })
        })

        while (!finished.await(50, TimeUnit.MILLISECONDS)) {
          if (System.nanoTime() > start + timeout.toNanos) throw new TimeoutException()
        }

        stream.complete(itemCount, expectedItemCount = itemCount, potentialItemCount = itemCount, rejectedItemCounter.longValue)

      } catch safely {
        case _: TimeoutException =>
          stream.timedOut(timeout)

        case t =>
          log error("Synthetic samplesource feedStream failed", t)
          stream.completeExceptionally(t)
      }
    }
  }

}
