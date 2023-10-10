/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store.container.worker

import io.opentelemetry.api.trace.Span
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.press.{BrioPressInstance, BrioPressSource}
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.samplesource.service.{MetadataParameters, SampleSourceWorkerService}
import org.burstsys.samplestore.configuration.defaultManualBatchSpanProperty
import org.burstsys.samplestore.pipeline
import org.burstsys.samplestore.trek._
import org.burstsys.tesla.thread.request.{TeslaRequestFuture, teslaRequestExecutor}
import org.burstsys.vitals.logging.{burstLocMsg, burstStdMsg}
import org.burstsys.vitals.properties._

import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}
import scala.concurrent.{Await, Future, Promise}
import scala.jdk.CollectionConverters._
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
   * Instantiate an instance of a scanning data provider and use it to feed the stream.
   *
   * @param stream the stream of the incoming request
   * @return a future that completes when the stream has been fed
   */
  override def feedStream(stream: NexusStream): Future[Unit] = {
    val feedStreamComplete = Promise[Unit]()
    ScanningFeedStreamTrek.begin(stream.guid, stream.suid) {stage =>
      stage.span.setAttribute(SOURCE_NAME_KEY, this.name)
      TeslaRequestFuture {
        val feedControl = prepareFeedControl(stream)
        val batchControls = prepareBatchControls(feedControl, stream)

        val batchIds = new ConcurrentSkipListSet(batchControls.map(_.id).asJavaCollection)

        log info burstStdMsg(s"(traceId=${stage.getTraceId}, batchCount=${batchControls.size}) starting batches")
        stage.span.setAttribute(BATCH_COUNT_KEY, batchControls.size)
        val batches = batchControls.map { b =>
          spanWrap(b).andThen {
            case Success(BatchResult(control, itemCount, skipped)) =>
              if (batchIds.remove(control.id)) {
                if (log.isDebugEnabled) {
                  log debug burstStdMsg(s"batch completed (batchId=${control.id}, traceId=${stage.getTraceId}," +
                    s" itemCount=$itemCount, skipped=$skipped); " +
                    s"remaining batches=${batchIds.asScala.mkString("[", ",", "]")}")
                }
              } else {
                log warn burstLocMsg(s"batch ${control.id} already completed (traceId=${stage.getTraceId})")
              }
            case Failure(ex) =>
              log error(burstLocMsg(s"batch ${b.id} failed (batchId=${b.id}, traceId=${stage.getTraceId})", ex), ex)
          }
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
          ScanningFeedStreamTrek.end(stage)
        } catch {
          case t: java.util.concurrent.TimeoutException =>
            log error burstStdMsg(s"(traceId=${stage.getTraceId}) scanning samplesource feedStream timeout; remaining batches=${batchIds.asScala.mkString("[",",","]")}")
            feedControl.cancel()
            stream.timedOut(feedControl.timeout)
            feedStreamComplete.failure(t)
            ScanningFeedStreamTrek.fail(stage, t)
          case t: Throwable =>
            log error(burstLocMsg(s"(traceId=${stage.getTraceId}) scanning samplesource feedStream failed; remaining batches=${batchIds.asScala.mkString("[",",","]")}", t), t)
            feedControl.cancel()
            stream.completeExceptionally(t)
            feedStreamComplete.failure(t)
            ScanningFeedStreamTrek.fail(stage, t)
        }
      }
    }
    feedStreamComplete.future
  }


  final case class BatchResult(control: B, itemCount: Int, skipped: Boolean)

  private def spanWrap(b: B): Future[BatchResult] = {
    if (defaultManualBatchSpanProperty.get) {
      ScanningBatchTrek.begin(b.stream.guid, b.stream.suid) { stage =>
        stage.span.setAttribute(SOURCE_NAME_KEY, this.name)
        stage.span.setAttribute(BATCH_ID_KEY, Long.box(b.id))
        doBatch(b).andThen {
          case Success(_) =>
            b.addAttributes(stage.span)
            ScanningBatchTrek.end(stage)
          case Failure(ex) =>
            b.addAttributes(stage.span)
            ScanningBatchTrek.fail(stage, ex)
        }
      }
    } else {
      doBatch(b)
    }
  }

  private def doBatch(control: B): Future[BatchResult] = {
    val provider = getProvider(control)
    val data = provider.scanner(control.stream)

    val batchComplete = Promise[BatchResult]()
    if (log.isDebugEnabled())
      log debug burstLocMsg(s"starting (guid=${control.stream.guid}, batchId=${control.id})")
    TeslaRequestFuture {
      try {
        if (!data.hasNext) {
          if (log.isDebugEnabled())
            log debug burstLocMsg(s"emtpy batch (guid=${control.stream.guid}, batchId=${control.id})")
          batchComplete.success(BatchResult(control, 0, skipped = false))
        } else {
          val inFlightItemCount = new AtomicInteger(0)
          val readItemCount = new AtomicInteger(0)
          val scanDone = new AtomicBoolean(!(data.hasNext && control.continueProcessing))
          while (!scanDone.get) {
            val item = data.next()
            scanDone.set(!(data.hasNext && control.continueProcessing))
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
                if (defaultManualBatchSpanProperty.get) {
                  Span.current.recordException(t)
                }
                ScanningStoreReporter.onReadReject(control.feedControl)
            }.andThen {
              case _ =>
                if (inFlightItemCount.decrementAndGet() == 0 && scanDone.get()) {
                  if (log.isDebugEnabled)
                    log debug burstLocMsg(s"(guid=${control.stream.guid}, batchId=${control.id}, readItemCount=$readItemCount) completed")

                  batchComplete.success(BatchResult(finalizeBatch(control), readItemCount.get(), skipped = false))
                }
            }
          }
        }
      } catch {
        case ex: Throwable =>
          log error(burstLocMsg(s"batch exception (guid=${control.stream.guid}, batchId=${control.id})", ex), ex)
          if (defaultManualBatchSpanProperty.get) {
            Span.current().recordException(ex)
          }
          batchComplete.failure(ex)
      }
    }

    batchComplete.future
  }

  override def putBroadcastVars(metadata: MetadataParameters): Unit = {}
}
