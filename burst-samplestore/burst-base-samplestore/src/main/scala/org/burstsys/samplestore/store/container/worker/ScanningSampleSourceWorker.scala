/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store.container.worker

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.press.{BrioPressInstance, BrioPressSource}
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.samplesource.service.{MetadataParameters, SampleSourceWorkerService}
import org.burstsys.samplestore.pipeline
import org.burstsys.samplestore.trek.{BATCH_ID_KEY, SyntheticBatchTrek, SyntheticFeedStreamTrek}
import org.burstsys.tesla.thread.request.{TeslaRequestFuture, teslaRequestExecutor}
import org.burstsys.vitals.logging.{burstLocMsg, burstStdMsg}
import org.burstsys.vitals.properties._

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}
import scala.concurrent.{Await, Future, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success}

abstract class ScanningSampleSourceWorker[T, F <: FeedControl, B <: BatchControl]() extends SampleSourceWorkerService {

  protected trait DataProvider {
    def scanner(stream: NexusStream): Iterator[T]

    def pressInstance(item: T): BrioPressInstance

    def pressSource(item: BrioPressInstance): BrioPressSource

    def schema: BrioSchema
  }

  protected def getProvider(control: B): DataProvider

  def prepareBatchControls(feedControl: F, stream: NexusStream): Iterable[B]

  def finalizeBatch(control: B): B

  def finalizeBatchResults(feedControl: F, results: Iterable[BatchResult]): Unit

  def prepareFeedControl(stream: NexusStream): F

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
        val feedControl = prepareFeedControl(stream)
        val batchControls = prepareBatchControls(feedControl, stream)

        log info burstStdMsg(s"(traceId=${stage.getTraceId}, batchCount=${batchControls.size}) starting batches")
        val batches = batchControls.map { b =>
          doBatch(b)
        }
        val s = Future.sequence(batches)
        try {
          // wait for all batches to complete but not forever
          Await.ready(s, feedControl.timeout)
          s.onComplete{
            case Success(results) =>
              finalizeBatchResults(feedControl, results)
            case Failure(ex) =>
              log error(burstLocMsg(s"buckets completed with exception", ex), ex)
          }
          log info burstStdMsg(s"stream completed (traceId=${stage.getTraceId}, processedItemsCount=${feedControl.processedItemsCount}, " +
            s"rejectedItemsCount=${feedControl.rejectedItemsCount}, expectedItemsCount=${feedControl.expectedItemsCount})")
          stream.complete(
            itemCount = feedControl.processedItemsCount,
            expectedItemCount = feedControl.expectedItemsCount,
            potentialItemCount = feedControl.potentialItemsCount,
            feedControl.rejectedItemsCount)
          feedStreamComplete.success(())
        } catch {
          case t: java.util.concurrent.TimeoutException =>
            log error burstStdMsg("(traceId=${stage.getTraceId}) synthetic samplesource feedStream timedout")
            feedControl.cancel()
            stream.timedOut(feedControl.timeout)
            feedStreamComplete.failure(t)
          case t: Throwable =>
            log error(burstLocMsg(s"(traceId=${stage.getTraceId}) synthetic samplesource feedStream failed", t), t)
            feedControl.cancel()
            stream.completeExceptionally(t)
            feedStreamComplete.failure(t)
        }
      }
    }
    feedStreamComplete.future
  }


  final case class BatchResult(control: B, itemCount: Int, skipped: Boolean)

  private def doBatch(control: B) = {
    val provider = getProvider(control)
    val data = provider.scanner(control.stream)

    val batchComplete = Promise[BatchResult]()
    SyntheticBatchTrek.begin(control.stream.guid, control.stream.suid) {stage =>
      stage.span.setAttribute(BATCH_ID_KEY, Long.box(control.id))
      if (log.isDebugEnabled())
        log debug burstLocMsg(s"starting (guid=${control.stream.guid}, batchId=${control.id})")
      TeslaRequestFuture {
        try {
          if (!data.hasNext) {
            if (log.isDebugEnabled())
              log debug burstLocMsg(s"skipping batch (guid=${control.stream.guid}, batchId=${control.id})")
            batchComplete.success(BatchResult(control, 0, skipped = false))
          } else {
            val inFlightItemCount = new AtomicInteger(0)
            val readItemCount = new AtomicInteger(0)
            val scanDone = new AtomicBoolean(false)
            while (data.hasNext && control.continueProcessing) {
              val item = data.next()
              inFlightItemCount.incrementAndGet()
              readItemCount.incrementAndGet()

              val pressInstance = provider.pressInstance(item)
              val pressSource = provider.pressSource(pressInstance)
              val pressedItemFuture = pipeline.pressToFuture(control.stream, pressSource, provider.schema,
                pressInstance.schemaVersion, control.maxItemSize, control.maxStreamSize)
              pressedItemFuture.andThen {
                case Success(pipeline.PressJobResults(jobId, pressSize, jobDuration, pressDuration, skipped)) =>
                  if (log.isDebugEnabled) {
                    log debug burstLocMsg(s"(guid=${control.stream.guid}, batchId=${control.id}, jobId=$jobId, " +
                      s"bytes=$pressSize, jobDuration=$jobDuration, pressDuration=$pressDuration, skipped=$skipped) on stream")
                  }
                  if (skipped) {
                    control.cancel()
                    ScanningStoreReporter.onReadSkipped(control.feedControl)
                    ScanningStoreReporter.onReadReject(control.feedControl)
                  } else {
                    ScanningStoreReporter.onReadComplete(control.feedControl, jobDuration, pressSize)
                  }
                case Failure(t) =>
                  stage.span.recordException(t)
                  ScanningStoreReporter.onReadReject(control.feedControl)
              }.andThen {
                case _ =>
                  if (inFlightItemCount.decrementAndGet() == 0 && scanDone.get()) {
                    if (log.isDebugEnabled)
                      log debug burstLocMsg(s"(guid=${control.stream.guid}, batchId=${control.id}, readItemCount=$readItemCount) completed")

                    batchComplete.success(BatchResult(finalizeBatch(control), readItemCount.get(), skipped = false))
                    control.addAttributes(stage.span)
                    SyntheticBatchTrek.end(stage)
                  }
              }
            }
            scanDone.set(true)
          }
        } catch {
          case ex: Throwable =>
            log error(burstLocMsg(s"batch exception (guid=${control.stream.guid}, batchId=${control.id})", ex), ex)
            stage.span.recordException(ex)
            batchComplete.failure(ex)
        }
      }
    }
    batchComplete.future.andThen {
      case Success(BatchResult(control, itemCount, skipped)) =>
        log info burstStdMsg(s"completed (batch=${control.id}, itemCount=$itemCount, truncated=$skipped, cancelled=${control.isCancelled})")
      case Failure(ex) =>
        log error(burstStdMsg(s"exception", ex), ex)
    }
  }

  override def putBroadcastVars(metadata: MetadataParameters): Unit = {}
}
