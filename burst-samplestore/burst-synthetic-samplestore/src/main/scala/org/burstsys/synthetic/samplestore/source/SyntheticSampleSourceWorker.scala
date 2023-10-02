/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.synthetic.samplestore.source

import io.opentelemetry.api.trace.Span
import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.provider.SyntheticDataProvider
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.samplesource.service.{MetadataParameters, SampleSourceWorkerService}
import org.burstsys.samplestore.pipeline
import org.burstsys.synthetic.samplestore.SyntheticStoreReporter
import org.burstsys.synthetic.samplestore.configuration._
import org.burstsys.synthetic.samplestore.trek._
import org.burstsys.tesla.thread.request.{TeslaRequestFuture, teslaRequestExecutor}
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.logging.{burstLocMsg, burstStdMsg}
import org.burstsys.vitals.properties._

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}
import scala.concurrent.{Await, Future, Promise}
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
    SyntheticFeedStreamTrek.begin(stream.guid, stream.suid) {stage =>
      TeslaRequestFuture {
        val props = stream.properties.extend
        val timeout = props.getValueOrProperty(defaultPressTimeoutProperty)
        val batchCount = Math.max(1, props.getValueOrProperty(defaultBatchCountProperty))
        val stats = {
          val globalItemCount = props.getValueOrProperty(defaultItemCountProperty)
          val itemCount = globalItemCount / batchCount
          val maxItemSize = props.getValueOrProperty(defaultMaxItemSizeProperty)
          val maxLoadSize = props.getValueOrProperty(defaultMaxLoadSizeProperty)
          val workerCount = props.getValueOrProperty(defaultWorkersCountProperty)
          val streamMaxSize = Math.max(maxLoadSize / workerCount, 1e6.toInt)
          val bs = BatchStats(itemCount, streamMaxSize, maxItemSize)
          bs.expectedItemCount.set(globalItemCount)
          bs
        }

        log info burstStdMsg(s"(traceId=${stage.getTraceId}, itemCount=${stats.itemCount}, batchCont=$batchCount) starting batches")
        val batches = (1 to batchCount).map { i =>
          doBatch(stream, i, stats)
        }
        val s = Future.sequence(batches)
        try {
          // wait for all batches to complete but not forever
          Await.ready(s, timeout)
          log info burstStdMsg(s"stream completed (traceId=${stage.getTraceId}, itemCount=${stats.itemCounter.get()}, " +
            s"rejectedItemCount=${stats.rejectedItemCounter.get()}, expectedItemCount=${stats.expectedItemCount})")
          stream.complete(itemCount = stats.itemCounter.longValue(), expectedItemCount = stats.expectedItemCount.longValue(),
            potentialItemCount = stats.expectedItemCount.longValue(), stats.rejectedItemCounter.longValue)
          feedStreamComplete.success(())
        } catch {
          case t: java.util.concurrent.TimeoutException =>
            log error burstStdMsg("(traceId=${stage.getTraceId}) synthetic samplesource feedStream timedout")
            stats.cancelWork.set(true)
            stream.timedOut(timeout)
            feedStreamComplete.failure(t)
          case t =>
            log error("(traceId=${stage.getTraceId}) synthetic samplesource feedStream failed", t)
            stats.cancelWork.set(true)
            stream.completeExceptionally(t)
            feedStreamComplete.failure(t)
        }
      }
    }
    feedStreamComplete.future
  }

  private case class BatchStats(itemCount: Int, maxStreamSize: Long, maxItemSize: Int) {
    val rejectedItemCounter = new AtomicInteger()
    val itemCounter = new AtomicInteger()
    val cancelWork = new AtomicBoolean(false)
    val skipped = new AtomicBoolean(false)
    val expectedItemCount = new AtomicInteger(itemCount)

    def continueProcessing: Boolean = {
      !cancelWork.get() && !skipped.get()
    }

    def addAttributes(span: Span): Unit = {
      span.setAttribute(REJECTED_ITEMS_KEY, rejectedItemCounter.get())
      span.setAttribute(ITEM_COUNT_KEY, itemCounter.get())
      span.setAttribute(EXPECTED_ITEM_COUNT_KEY, expectedItemCount.get())
      span.setAttribute(CANCEL_WORK_KEY, Boolean.box(cancelWork.get()))
      span.setAttribute(SKIPPED_KEY, Boolean.box(skipped.get()))
    }
  }

  private final case class BatchResult(batchId: Int, itemCount: Int, skipped: Boolean)

  private def doBatch(stream: NexusStream, batchId: Int, stats: BatchStats): Future[BatchResult] = {
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

    val batchComplete = Promise[BatchResult]()
    SyntheticBatchTrek.begin(stream.guid, stream.suid) {stage =>
      stage.span.setAttribute(BATCH_ID_KEY, batchId)
      TeslaRequestFuture {
        if (log.isDebugEnabled())
          log debug burstLocMsg(s"starting (guid=${stream.guid}, batchId=$batchId)")
        val data = dataProvider.data(stats.itemCount, stream.properties)
        if (!data.hasNext) {
          if (log.isDebugEnabled())
            log debug burstLocMsg(s"skipping batch (guid=${stream.guid}, batchId=$batchId)")
          batchComplete.success(BatchResult(batchId, 0, skipped = false))
        } else {
          val inFlightItemCount = new AtomicInteger(0)
          val readItemCount = new AtomicInteger(0)
          val scanDone = new AtomicBoolean(false)
          while (data.hasNext && stats.continueProcessing) {
            val item = data.next()
            inFlightItemCount.incrementAndGet()
            readItemCount.incrementAndGet()

            val pressSource = dataProvider.pressSource(item)
            val pressedItemFuture = pipeline.pressToFuture(stream, pressSource, schema, item.schemaVersion, stats.maxItemSize, stats.maxStreamSize)
            pressedItemFuture.andThen {
              case Success(pipeline.PressJobResults(jobId, pressSize, jobDuration, pressDuration, skipped)) =>
                if (log.isDebugEnabled) {
                  log debug burstLocMsg(s"(guid=${stream.guid}, batchId=$batchId, jobId=$jobId, " +
                    s"bytes=$pressSize, jobDuration=$jobDuration, pressDuration=$pressDuration, skipped=$skipped) on stream")
                }
                if (skipped) {
                  stats.cancelWork.set(true)
                  stats.rejectedItemCounter.incrementAndGet()
                  stats.skipped.set(true)
                  SyntheticStoreReporter.onReadSkipped()
                  SyntheticStoreReporter.onReadReject()
                } else {
                  stats.itemCounter.incrementAndGet()
                  SyntheticStoreReporter.onReadComplete(jobDuration, pressSize)
                }
              case Failure(t) =>
                stage.span.recordException(t)
                stats.rejectedItemCounter.incrementAndGet()
                SyntheticStoreReporter.onReadReject()
            }.andThen {
              case _ =>
                if (inFlightItemCount.decrementAndGet() == 0 && scanDone.get()) {
                  if (log.isDebugEnabled)
                    log debug burstLocMsg(s"(guid=${stream.guid}, batchId=$batchId, readItemCount=$readItemCount) completed")
                  batchComplete.success(BatchResult(batchId, readItemCount.get(), skipped = false))
                  stats.addAttributes(stage.span)
                  SyntheticBatchTrek.end(stage)
                }
            }
          }
          scanDone.set(true)
        }
      }

    }
    batchComplete.future.andThen {
      case Success(BatchResult(batchId, itemCount, skipped)) =>
        log info burstStdMsg(s"completed (batch=$batchId, itemCount=$itemCount, truncated=$skipped, cancelled=${stats.cancelWork.get()})")
      case Failure(ex) =>
        log error burstStdMsg(s"exception", ex)
    }
  }

  override def putBroadcastVars(metadata: MetadataParameters): Unit = {}
}
