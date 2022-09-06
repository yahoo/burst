/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.press.pipeline

import java.util.concurrent.LinkedBlockingQueue

import org.burstsys.brio.BrioReporter
import org.burstsys.brio.blob.BrioBlobEncoder
import org.burstsys.brio.configuration.brioPressThreadsProperty
import org.burstsys.brio.dictionary.factory
import org.burstsys.brio.dictionary.factory._
import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.press.{BrioPressSink, BrioPressSource, BrioPresser}
import org.burstsys.brio.types.BrioTypes.BrioVersionKey
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.buffer.factory._
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.vitals.errors._
import org.burstsys.vitals.instrument.{prettyByteSizeString, prettyRateString, prettyTimeFromNanos}
import org.burstsys.vitals.uid._

import scala.concurrent.{Future, Promise}
import scala.language.postfixOps
import scala.util.Success
import org.burstsys.vitals.logging._

/**
 * A Fixed set of worker threads pressing items to blobs...
 * Metrics are tracked
 */
trait BrioPressPipeline extends AnyRef {

  /**
   * establish a queue to allow fixed size pool to grab chunks of work
   */
  private[this]
  lazy val pressJobQueue = new LinkedBlockingQueue[BrioPressPipelineJob] {

    log info burstStdMsg(s"start ${brioPressThreadsProperty.getOrThrow} press worker thread(s)")

    /**
     * startup a fixed worker pool for parallel pressing
     */
    (0 until brioPressThreadsProperty.getOrThrow).foreach {
      i =>
        TeslaRequestFuture { // for waiting around
          Thread.currentThread setName f"brio-presser-$i%02d"
          val pressBuffer = TeslaWorkerCoupler(grabBuffer(pressBufferSize))
          val dictionary = TeslaWorkerCoupler(grabMutableDictionary())
          while (true) {
            // grab a job as they come in
            val job = this.take // all waiting in request thread
            TeslaWorkerCoupler { // worker thread for CPU bound pressing
              try {
                val jobId = job.jobId
                val start = System.nanoTime
                job.press(pressBuffer, dictionary)
                val elapsedNs = System.nanoTime - start
                if (elapsedNs > slowPressDuration.toNanos) {
                  BrioReporter.onPressSlow()
                  val byteCount = pressBuffer.currentUsedMemory
                  log info burstStdMsg(
                    s"BrioPressPipeline($i) guid=${job.guid}, jobId=$jobId SLOW PRESS elapsedNs=$elapsedNs (${
                      prettyTimeFromNanos(elapsedNs)
                    }) byteCount=${byteCount}  (${prettyByteSizeString(byteCount)}) ${
                      prettyRateString("byte", byteCount, elapsedNs)
                    }"
                  )
                }
              } catch safely {
                case t: Throwable =>
                  log error burstStdMsg(s"press failed", t)
              } finally {
                pressBuffer.reset
                dictionary.reset()
              }
            }
          }
        }
    }
  }

  sealed trait BrioPressPipelineJob {

    /**
     * tracking OP GUID
     *
     * @return
     */
    def guid: VitalsUid

    /**
     * logical id for job
     *
     * @return
     */
    def jobId: Long

    /**
     * TODO
     *
     * @param pressBuffer
     * @param dictionary
     * @return
     */
    def press(pressBuffer: TeslaMutableBuffer, dictionary: BrioMutableDictionary): BrioPressPipelineJob

  }

  /**
   * A single press job
   *
   * @param p
   * @param pressSource
   * @param schema
   * @param version
   * @param maxItemSize
   */
  private[this] case
  class BrioPressPipelineJobContext(jobId: Long, guid: VitalsUid, p: Promise[TeslaMutableBuffer], pressSource: BrioPressSource,
                                    schema: BrioSchema, version: BrioVersionKey, maxItemSize: Int) extends BrioPressPipelineJob {

    def press(pressBuffer: TeslaMutableBuffer, dictionary: BrioMutableDictionary): this.type = {
      val start = System.nanoTime
      val blobBuffer = grabBuffer(maxItemSize + brioPressDefaultDictionarySize + (10 * SizeOfInteger))
      try {
        val sink = BrioPresser(schema, BrioPressSink(pressBuffer, dictionary), pressSource).press
        BrioBlobEncoder.encodeV2Blob(sink.buffer, version, sink.dictionary, blobBuffer)
        BrioReporter.onPressComplete(System.nanoTime - start, blobBuffer.currentUsedMemory)
        p.complete(Success(blobBuffer))
      } catch safely {
        case t: Throwable =>
          log warn burstStdMsg(s"BrioPressPipelineJob.press() jobId=$jobId, guid=$guid -- discarding press job... $t", t)
          BrioReporter.onPressReject()
          releaseBuffer(blobBuffer)
          p.failure(t)
      }
      this
    }

  }

  /**
   * Take a press source and press using parallel threading
   *
   * @param maxItemSize
   */
  def pressToFuture(guid: VitalsUid, pressSource: BrioPressSource, schema: BrioSchema, version: BrioVersionKey, maxItemSize: Int): Future[TeslaMutableBuffer] = {
    val p = Promise[TeslaMutableBuffer]()
    pressJobQueue put BrioPressPipelineJobContext(jobId.getAndIncrement, guid, p, pressSource, schema, version, maxItemSize)
    p.future
  }

}
