/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.synthetic.samplestore.source

import io.opentelemetry.api.trace.Span
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.press.{BrioPressInstance, BrioPressSource}
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.samplesource.service.{MetadataParameters, SampleSourceWorkerService}
import org.burstsys.samplestore.pipeline
import org.burstsys.synthetic.samplestore.SyntheticStoreReporter
import org.burstsys.synthetic.samplestore.configuration._
import org.burstsys.synthetic.samplestore.trek._
import org.burstsys.tesla.thread.request.{TeslaRequestFuture, teslaRequestExecutor}
import org.burstsys.vitals.logging.{burstLocMsg, burstStdMsg}
import org.burstsys.vitals.properties._

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}
import scala.concurrent.{Await, Future, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success}

abstract class ScanningSampleSourceWorker[T]() extends SampleSourceWorkerService {

  def prepareStats(stream: NexusStream, props: VitalsExtendedPropertyMap): BatchStats

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
        val props: VitalsExtendedPropertyMap = stream.properties.extend
        val timeout = props.getValueOrProperty(defaultPressTimeoutProperty)
        val batchCount = Math.max(1, props.getValueOrProperty(defaultBatchCountProperty))
        val stats = prepareStats(stream, props)

        log info burstStdMsg(s"(traceId=${stage.getTraceId}, itemCount=${stats.itemCount}, batchCont=$batchCount) starting batches")
        val batches = (1 to batchCount).map { i =>
          doBatch(stream, props, i, stats)
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

  protected class BatchStats(val itemCount: Int, val maxStreamSize: Long, val maxItemSize: Int) {
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

  protected case class BatchResult(batchId: Int, itemCount: Int, skipped: Boolean)

  protected trait DataProvider {
    def pressInstance(item: T): BrioPressInstance
    def pressSource(item: BrioPressInstance): BrioPressSource
    def schema: BrioSchema
    def schemaVersion: Int
  }

  protected def getScanner(stream: NexusStream, props: VitalsExtendedPropertyMap): Iterator[T]

  protected def getProvider(stream: NexusStream, props: VitalsExtendedPropertyMap): DataProvider

  private def doBatch(stream: NexusStream, props: VitalsExtendedPropertyMap, batchId: Int, stats: BatchStats): Future[BatchResult] = {
    val data = getScanner(stream, props)
    val provider = getProvider(stream, props)

    val batchComplete = Promise[BatchResult]()
    SyntheticBatchTrek.begin(stream.guid, stream.suid) {stage =>
      stage.span.setAttribute(BATCH_ID_KEY, batchId)
      TeslaRequestFuture {
        if (log.isDebugEnabled())
          log debug burstLocMsg(s"starting (guid=${stream.guid}, batchId=$batchId)")
        if (!data.hasNext) {
          if (log.isDebugEnabled())
            log debug burstLocMsg(s"skipping batch (guid=${stream.guid}, batchId=$batchId)")
          batchComplete.success(new BatchResult(batchId, 0, skipped = false))
        } else {
          val inFlightItemCount = new AtomicInteger(0)
          val readItemCount = new AtomicInteger(0)
          val scanDone = new AtomicBoolean(false)
          while (data.hasNext && stats.continueProcessing) {
            val item = data.next()
            inFlightItemCount.incrementAndGet()
            readItemCount.incrementAndGet()

            val pressInstance = provider.pressInstance(item)
            val pressSource = provider.pressSource(pressInstance)
            val pressedItemFuture = pipeline.pressToFuture(stream, pressSource, provider.schema, pressInstance.schemaVersion, stats.maxItemSize, stats.maxStreamSize)
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
                  batchComplete.success(new BatchResult(batchId, readItemCount.get(), skipped = false))
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
