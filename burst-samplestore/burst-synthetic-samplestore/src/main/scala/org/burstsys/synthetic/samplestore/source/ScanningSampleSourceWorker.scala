/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.synthetic.samplestore.source

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.press.{BrioPressInstance, BrioPressSource}
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.samplesource.service.{MetadataParameters, SampleSourceWorkerService}
import org.burstsys.samplestore.pipeline
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

  protected trait DataProvider {
    def scanner(stream: NexusStream): Iterator[T]

    def pressInstance(item: T): BrioPressInstance

    def pressSource(item: BrioPressInstance): BrioPressSource

    def schema: BrioSchema
  }

  protected def getProvider(control: BatchControl): DataProvider


  def prepareBatchControl(stream: NexusStream): BatchControl

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
        val batchControl = prepareBatchControl(stream)

        log info burstStdMsg(s"(traceId=${stage.getTraceId}, batchCont=${batchControl.batchCount}) starting batches")
        val batches = (1 to batchControl.batchCount).map { i =>
          doBatch(batchControl, i)
        }
        val s = Future.sequence(batches)
        try {
          // wait for all batches to complete but not forever
          Await.ready(s, timeout)
          log info burstStdMsg(s"stream completed (traceId=${stage.getTraceId}, processedItemsCount=${batchControl.processedItemsCount}, " +
            s"rejectedItemsCount=${batchControl.rejectedItemsCount}, expectedItemsCount=${batchControl.expectedItemsCount})")
          stream.complete(
            itemCount = batchControl.processedItemsCount,
            expectedItemCount = batchControl.expectedItemsCount,
            potentialItemCount = batchControl.potentialItemsCount,
            batchControl.rejectedItemsCount)
          feedStreamComplete.success(())
        } catch {
          case t: java.util.concurrent.TimeoutException =>
            log error burstStdMsg("(traceId=${stage.getTraceId}) synthetic samplesource feedStream timedout")
            batchControl.cancel()
            stream.timedOut(timeout)
            feedStreamComplete.failure(t)
          case t =>
            log error("(traceId=${stage.getTraceId}) synthetic samplesource feedStream failed", t)
            batchControl.cancel()
            stream.completeExceptionally(t)
            feedStreamComplete.failure(t)
        }
      }
    }
    feedStreamComplete.future
  }


  private case class BatchResult(batchId: Int, itemCount: Int, skipped: Boolean)

  private def doBatch(control: BatchControl, batchId: Int) = {
    val provider = getProvider(control)
    val data = provider.scanner(control.stream)

    val batchComplete = Promise[BatchResult]()
    SyntheticBatchTrek.begin(control.stream.guid, control.stream.suid) {stage =>
      stage.span.setAttribute(BATCH_ID_KEY, batchId)
      TeslaRequestFuture {
        if (log.isDebugEnabled())
          log debug burstLocMsg(s"starting (guid=${control.stream.guid}, batchId=$batchId)")
        if (!data.hasNext) {
          if (log.isDebugEnabled())
            log debug burstLocMsg(s"skipping batch (guid=${control.stream.guid}, batchId=$batchId)")
          batchComplete.success(BatchResult(batchId, 0, skipped = false))
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
                  log debug burstLocMsg(s"(guid=${control.stream.guid}, batchId=$batchId, jobId=$jobId, " +
                    s"bytes=$pressSize, jobDuration=$jobDuration, pressDuration=$pressDuration, skipped=$skipped) on stream")
                }
                if (skipped) {
                  control.cancel()
                  ScanningStoreReporter.onReadSkipped(control.stats)
                  ScanningStoreReporter.onReadReject(control.stats)
                } else {
                  ScanningStoreReporter.onReadComplete(control.stats, jobDuration, pressSize)
                }
              case Failure(t) =>
                stage.span.recordException(t)
                ScanningStoreReporter.onReadReject(control.stats)
            }.andThen {
              case _ =>
                if (inFlightItemCount.decrementAndGet() == 0 && scanDone.get()) {
                  if (log.isDebugEnabled)
                    log debug burstLocMsg(s"(guid=${control.stream.guid}, batchId=$batchId, readItemCount=$readItemCount) completed")
                  batchComplete.success(BatchResult(batchId, readItemCount.get(), skipped = false))
                  control.addAttributes(stage.span)
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
        log info burstStdMsg(s"completed (batch=$batchId, itemCount=$itemCount, truncated=$skipped, cancelled=${control.isCancelled})")
      case Failure(ex) =>
        log error burstStdMsg(s"exception", ex)
    }
  }

  override def putBroadcastVars(metadata: MetadataParameters): Unit = {}
}
