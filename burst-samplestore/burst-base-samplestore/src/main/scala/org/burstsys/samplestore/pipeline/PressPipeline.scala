/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.pipeline

import org.burstsys.brio.blob.BrioBlobEncoder
import org.burstsys.brio.configuration.brioPressThreadsProperty
import org.burstsys.brio.dictionary.factory._
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.press.{BrioPressSink, BrioPressSource, BrioPresser}
import org.burstsys.brio.types.BrioTypes.BrioVersionKey
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.samplestore.{SampleStoreReporter, pipeline}
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.buffer.factory._
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._
import org.burstsys.vitals.reporter.instrument.{prettyByteSizeString, prettyRateString, prettyTimeFromNanos}
import org.burstsys.vitals.uid._

import java.util.concurrent.{ArrayBlockingQueue, LinkedBlockingQueue}
import scala.concurrent.{Future, Promise}
import scala.language.postfixOps
import scala.util.Success

/**
 * A Fixed set of worker threads pressing items to blobs...
 * Metrics are tracked
 */
trait PressPipeline extends AnyRef {

  /**
   * establish a queue to allow fixed size pool to grab chunks of work
   */
  private[this]
  lazy val pressJobQueue = new LinkedBlockingQueue[PressPipelineJob](brioPressThreadsProperty.get*10) {

    pipeline.log info burstStdMsg(s"start ${brioPressThreadsProperty.get} press worker thread(s)")

    /**
     * startup a fixed worker pool for parallel pressing
     */
    (0 until brioPressThreadsProperty.get).foreach {
      i =>
        TeslaRequestFuture { // for waiting around
          Thread.currentThread setName f"brio-presser-$i%02d"
          val pressBuffer = TeslaWorkerCoupler(grabBuffer(pressBufferSize))
          val dictionary = TeslaWorkerCoupler(grabMutableDictionary())
          val presser = BrioPresser(BrioPressSink(pressBuffer, dictionary))

          while (true) {
            // grab a job as they come in
            val job = this.take // all waiting in request thread
            pipeline.log debug burstStdMsg(s"taking ${job.jobId} on queue (size=${this.size()})")
            var blobBuffer = TeslaWorkerCoupler(grabBuffer(job.maxItemSize + brioPressDefaultDictionarySize + (10 * SizeOfInteger)))
            TeslaWorkerCoupler { // worker thread for CPU bound pressing
              try {
                val jobId = job.jobId
                val start = System.nanoTime
                pipeline.log debug burstStdMsg(s"taking ${job.jobId} on queue (size=${this.size()})")
                blobBuffer = job.press(presser, blobBuffer)
                val elapsedNs = System.nanoTime - start
                if (elapsedNs > slowPressDuration.toNanos) {
                  SampleStoreReporter.onPressSlow()
                  val byteCount = pressBuffer.currentUsedMemory
                  pipeline.log info burstStdMsg(
                    s"BrioPressPipeline($i) guid=${job.guid}, jobId=$jobId SLOW PRESS elapsedNs=$elapsedNs (${
                      prettyTimeFromNanos(elapsedNs)
                    }) byteCount=$byteCount  (${prettyByteSizeString(byteCount)}) ${
                      prettyRateString("byte", byteCount, elapsedNs)
                    }"
                  )
                }
              } catch safely {
                case t: Throwable =>
                  log error(burstStdMsg(s"press failed", t), t)
              } finally {
                pressBuffer.reset
                dictionary.reset()
              }
            }
            // put the buffer on the stream outside of the worker thread in case we block on the stream
            if (blobBuffer != null) {
              job.stream.put(blobBuffer)
              job.p.complete(Success(job.jobId))
            }
          }
        }
    }
  }

  /**
   * The context for a press job
   *
   * @param jobId       the job number
   * @param guid        the guid of the request that need this press
   * @param p           a promise to complete when the press is done
   * @param pressSource the source that provides the object to press
   * @param schema      the schema to use
   * @param version     the schema version of the blob
   * @param maxItemSize the maximum number of bytes to press
   */
  private case class
  PressPipelineJob(
                        jobId: Long,
                        stream: NexusStream,
                        p: Promise[Long],
                        pressSource: BrioPressSource,
                        schema: BrioSchema,
                        version: BrioVersionKey,
                        maxItemSize: Int
                      )
  {
    def press(presser: BrioPresser, blobBuffer: TeslaMutableBuffer): TeslaMutableBuffer = {
      val start = System.nanoTime
      try {
        pipeline.log trace burstLocMsg(s"pressing job $jobId into buffer ${blobBuffer.basePtr}")
        val sink = presser.press(schema, pressSource)
        BrioBlobEncoder.encodeV2Blob(sink.buffer, version, sink.dictionary, blobBuffer)
        SampleStoreReporter.onPressComplete(System.nanoTime - start, blobBuffer.currentUsedMemory)
        blobBuffer
      } catch safely {
        case t: Throwable =>
          log warn burstLocMsg(s"jobId=$jobId, guid=$guid -- discarding press job... $t", t)
          SampleStoreReporter.onPressReject()
          releaseBuffer(blobBuffer)
          p.failure(t)
          null
      }
    }

    def guid: VitalsUid = stream.guid
  }


  /**
   * Take a press source and press using parallel threading
   *
   * @param maxItemSize the maximum number of bytes to press
   */
  def pressToFuture(stream: NexusStream, pressSource: BrioPressSource, schema: BrioSchema, version: BrioVersionKey, maxItemSize: Int): Future[Long] = {
    val p = Promise[Long]()
    val thing = PressPipelineJob(jobId.getAndIncrement, stream, p, pressSource, schema, version, maxItemSize)
    pipeline.log trace burstLocMsg(s"putting ${thing.jobId} on queue (size=${pressJobQueue.size()})")
    pressJobQueue put thing
    pipeline.log trace burstLocMsg(s"returning ${thing.jobId} future")
    p.future
  }

}
