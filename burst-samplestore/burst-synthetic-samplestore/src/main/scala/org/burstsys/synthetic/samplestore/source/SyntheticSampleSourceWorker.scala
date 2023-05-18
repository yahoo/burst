/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.synthetic.samplestore.source

import org.burstsys.brio
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.provider.SyntheticDataProvider
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.samplesource.service.{MetadataParameters, SampleSourceWorkerService}
import org.burstsys.synthetic.samplestore.configuration.{defaultItemCountProperty, defaultMaxItemSizeProperty, defaultPressTimeoutProperty, syntheticDatasetProperty}
import org.burstsys.tesla.thread.request.{TeslaRequestFuture, teslaRequestExecutor}
import org.burstsys.vitals.errors.{VitalsException, safely}
import org.burstsys.vitals.logging.burstStdMsg
import org.burstsys.vitals.properties._

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{CountDownLatch, TimeUnit}
import scala.concurrent.{Future, TimeoutException}
import scala.language.postfixOps

case class SyntheticSampleSourceWorker() extends SampleSourceWorkerService {

  /**
   * @return The name of this sample source worker.
   */
  override def name: String = SyntheticSampleSourceName

  private val unityBrio = BurstUnitySyntheticDataProvider()
  /**
   * Instantiate an instance of a synthetic data provider and use it to feed the stream.
   *
   * @param stream the stream of the incoming request
   * @return a future that completes when the stream has been fed
   */
  override def feedStream(stream: NexusStream): Future[Unit] = {
    TeslaRequestFuture {
      val props = stream.properties.extend
      val timeout = props.getValueOrProperty(defaultPressTimeoutProperty)
      try {
        val modelName = stream.get[String](syntheticDatasetProperty)
        val dataProvider = {
          val p = SyntheticDataProvider.providerNamed(modelName)
          if (p == null) {
            log error burstStdMsg(s"Data provider $modelName not found, substituting ${unityBrio.schemaName}")
            unityBrio
          } else {
            p
          }
        }
        if (!(stream.schema equalsIgnoreCase dataProvider.schemaName)) {
          throw VitalsException(s"Stream and synthetic data provider do not specify the same schema. stream=${stream.schema} provider=${dataProvider.schemaName}")
        }

        val schema = BrioSchema(dataProvider.schemaName)
        val maxItemSize = props.getValueOrProperty(defaultMaxItemSizeProperty)
        val itemCount = props.getValueOrProperty(defaultItemCountProperty)
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

  override def putBroadcastVars(metadata: MetadataParameters): Unit = {}
}
