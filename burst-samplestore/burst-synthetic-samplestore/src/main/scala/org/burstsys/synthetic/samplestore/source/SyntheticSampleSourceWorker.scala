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
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}
import scala.concurrent.{Await, Future, Promise, TimeoutException}
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
      val maxLoadSize = props.getValueOrProperty(defaultMaxLoadSizeProperty)
      val globalItemCount = props.getValueOrProperty(defaultItemCountProperty)
      val batchCount = props.getValueOrProperty(defaultBatchCountProperty)
      val rejectedItemCounter = new AtomicInteger()
      val completedItemCounter = new AtomicInteger()
      val cancelWork = new AtomicBoolean(false)

      val batches = (1 to batchCount).map { i =>
        TeslaRequestFuture[Boolean] {
          val itemCount = globalItemCount / batchCount
          val finished = new CountDownLatch(itemCount)
          log info burstLocMsg(s"starting batch $i")
          val data = dataProvider.data(itemCount, stream.properties)
          var notSkipped = true
          while (data.hasNext && notSkipped && !cancelWork.get()) {
            val item = data.next()
            val pressSource = dataProvider.pressSource(item)
            val pressedItem = pipeline.pressToFuture(stream, pressSource, schema, item.schemaVersion, maxItemSize, maxLoadSize)
            pressedItem.map({
                case pipeline.PressJobResults(jobId, itemSize, jobDuration, pressDuration, skipped) =>
                  if (log.isDebugEnabled)
                    log debug burstLocMsg(s"(jobId=$jobId, itemSize=$itemSize, jobDuration=$jobDuration, pressDuration=$pressDuration, skipped=$skipped) on stream")
                  if (skipped) {
                    rejectedItemCounter.incrementAndGet()
                    notSkipped = false
                  } else {
                    completedItemCounter.incrementAndGet()
                  }
              })
              .recover({ case _ => rejectedItemCounter.incrementAndGet() })
              .andThen({ case _ => finished.countDown() })
          }
          finished.await(timeout.toMillis, java.util.concurrent.TimeUnit.MILLISECONDS)
          notSkipped
        }.andThen {
          case Success(notSkipped) =>
            log info burstStdMsg(s"completed (batch=$i, truncated=${!notSkipped}, cancelled=${cancelWork.get()})")
            completedItemCounter.incrementAndGet()
          case Failure(ex) =>
            log error burstStdMsg(s"exception", ex)
        }
      }
      val s = Future.sequence(batches)
      try {
        // wait for all batches to complete but not forever
        Await.ready(s, timeout)
        log info burstStdMsg(s"stream completed (itemCount=${completedItemCounter.get()}, rejectedItemCount=${rejectedItemCounter.get()}, targetItemCount=$globalItemCount)")
        stream.complete(itemCount = globalItemCount, expectedItemCount = globalItemCount, potentialItemCount = globalItemCount, rejectedItemCounter.longValue)
        feedStreamComplete.success(())
      } catch {
        case t: java.util.concurrent.TimeoutException =>
          log error burstStdMsg("Synthetic samplesource feedStream timedout")
          cancelWork.set(true)
          stream.timedOut(timeout)
          feedStreamComplete.failure(t)
        case t =>
          log error("Synthetic samplesource feedStream failed", t)
          cancelWork.set(true)
          stream.completeExceptionally(t)
          feedStreamComplete.failure(t)
      }
    }
    feedStreamComplete.future
  }

  override def putBroadcastVars(metadata: MetadataParameters): Unit = {}
}
