/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.synthetic.samplestore.source

import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.provider.SyntheticDataProvider
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.samplesource.service.{MetadataParameters, SampleSourceWorkerService}
import org.burstsys.samplestore.pipeline
import org.burstsys.synthetic.samplestore.configuration._
import org.burstsys.tesla.thread.request.{TeslaRequestFuture, teslaRequestExecutor}
import org.burstsys.vitals.errors.{VitalsException, safely}
import org.burstsys.vitals.logging.{burstLocMsg, burstStdMsg}
import org.burstsys.vitals.properties._

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.{Future, Promise, TimeoutException}
import scala.language.postfixOps
import scala.util.{Failure, Success}

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
    val feedStreamComplete = Promise[Unit]()
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
        val globalItemCount = props.getValueOrProperty(defaultItemCountProperty)
        val batchCount = props.getValueOrProperty(defaultBatchCountProperty)
        val rejectedItemCounter = new AtomicInteger()
        val completedItemCounter = new AtomicInteger()
        val finished = new CountDownLatch(globalItemCount)

        val start = System.nanoTime
        val batches = (1 to batchCount).map { i =>
          TeslaRequestFuture[Unit] {
          log info burstLocMsg(s"starting batch $i")
          val itemCount = globalItemCount / batchCount
          dataProvider.data(itemCount, stream.properties).foreach(item => {
            val pressSource = dataProvider.pressSource(item)
            val pressedItem = pipeline.pressToFuture(stream, pressSource, schema, item.schemaVersion, maxItemSize)
            pressedItem
              .map({ result =>
                if (log.isDebugEnabled)
                  log debug burstLocMsg(s"job=${result._1} size=${result._2} on stream")
              })
              .recover({ case _ => rejectedItemCounter.incrementAndGet() })
              .andThen({ case _ => finished.countDown() })
          })}.andThen {
            case Success(_) =>
              log info burstStdMsg(s"completed batch $i items")
              completedItemCounter.incrementAndGet()
            case Failure(ex) =>
              log error burstStdMsg(s"exception", ex)
          }
        }
        val s = Future.sequence(batches)
        s.onComplete({
          case Success(_) =>
            log info burstStdMsg(s"completed ${completedItemCounter.get()} items")
            stream.complete(itemCount = globalItemCount, expectedItemCount = globalItemCount, potentialItemCount = globalItemCount, rejectedItemCounter.longValue)
            feedStreamComplete.success(())
          case Failure(ex) =>
            log error burstStdMsg(s"exception", ex)
            stream.completeExceptionally(ex)
            feedStreamComplete.failure(ex)
        })
      } catch safely {
        case _: TimeoutException =>
          stream.timedOut(timeout)

        case t =>
          log error("Synthetic samplesource feedStream failed", t)
          stream.completeExceptionally(t)
      }
    }
    feedStreamComplete.future
  }

  override def putBroadcastVars(metadata: MetadataParameters): Unit = {}
}
