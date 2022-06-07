/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.json.samplestore.worker

import org.burstsys.brio
import org.burstsys.brio.json.JsonPressSource
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.press.pipeline
import org.burstsys.json.samplestore.configuration.{alloyJsonVersionProperty, alloyLocationPropertyKey, alloyLociReplicationProperty, alloySkipIndexStreamPropertyKey}
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.samplesource.service.SampleSourceWorkerService
import org.burstsys.samplestore.api.configuration.burstSampleStoreHeartbeatInterval
import org.burstsys.tesla.buffer.mutable.endMarkerMutableBuffer
import org.burstsys.tesla.parcel.{TeslaEndMarkerParcel, TeslaExceptionMarkerParcel, TeslaHeartbeatMarkerParcel, TeslaNoDataMarkerParcel}
import org.burstsys.tesla.thread.request.{TeslaRequestFuture, teslaRequestExecutor}
import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.errors.{VitalsException, safely}
import org.burstsys.vitals.instrument.VitalsElapsedTimer
import org.burstsys.vitals.logging.burstStdMsg
import org.burstsys.vitals.properties._

import java.io.{FileInputStream, InputStream}
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.GZIPInputStream
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

trait JsonSampleSourceWorkerService extends SampleSourceWorkerService {

  override
  def feedStream(stream: NexusStream): Future[Unit] = {
    val guid = stream.guid
    val tag = s"AlloySampleSourceWorkerService.feedStream(guid=$guid)"
    TeslaRequestFuture {
      val heartbeat = newHeartbeater(stream).start
      try {
        JsonLoadThrottle(guid, pressToStream(stream))
      } catch safely {
        case e: Exception =>
          log.warn(s"$tag Unable to schedule stream feeder. reason=${e.getLocalizedMessage}")
          stream.put(TeslaExceptionMarkerParcel)
      } finally {
        log info burstStdMsg(s"stopping heartbeat")
        heartbeat.stop
      }
    }
  }

  private def pressToStream(stream: NexusStream): Unit = {
      val tag = s"AlloySampleSourceWorkerService.feedStream(guid=${stream.guid}, suid=${stream.suid})"
      log info burstStdMsg(s"$tag started")
      try {
        val timer = VitalsElapsedTimer("AlloySampleSourceFeedTimer")
        try {
          timer.start
          val itemReplication: Integer = Math.max(stream.properties.extend.getValueOrDefault[Int](alloyLociReplicationProperty.key, 0), 0)
          val itemSkip: Integer = Math.max(stream.properties.extend.getValueOrDefault[Int](alloySkipIndexStreamPropertyKey, 0), 0)
          val dataLocation: String = stream.properties.extend.getValueOrThrow[String](alloyLocationPropertyKey)
          val jsonVersion: Int = stream.properties.extend.getValueOrDefault[Int](alloyJsonVersionProperty.key, 1)

          log info burstStdMsg(s"$tag replication=$itemReplication skip=$itemSkip location=$dataLocation version=$jsonVersion")

          val jsonSource: InputStream = {
            val fileName = Paths.get(dataLocation)
            if (fileName.getFileName.toString.endsWith(".gz"))
              new GZIPInputStream(new FileInputStream(fileName.toFile))
            else
              new FileInputStream(fileName.toFile)
          }
          val schema = BrioSchema(stream.schema.toLowerCase)

          var skipCount = itemSkip
          var totalBytes = 0
          val itemCount = new AtomicInteger(0)
          val rejectedCount = new AtomicInteger(0)
          val futures = for (jb <- brio.json.getJsonSource(schema, jsonSource)) yield {
            val f = pipeline.pressToFuture(stream.guid, JsonPressSource(schema, jb), schema, 3, 10000000)
            f onComplete {
              case Success(buffer) =>
                log debug burstStdMsg(s"putting item in stream")
                totalBytes += buffer.currentUsedMemory
                itemCount.incrementAndGet()
                stream put buffer
              case Failure(t) =>
                log debug burstStdMsg(s"discarding item")
                rejectedCount.incrementAndGet()
            }
            f
          }
          Await.result(Future.sequence(futures), (1 minute))

          timer.stop
          log info burstStdMsg(s"$tag completed itemCount=$itemCount totalBytes=$totalBytes")
          stream put endMarkerMutableBuffer
          stream.itemCount = itemCount.intValue()
          stream.potentialItemCount = futures.size
          stream.rejectedItemCount = rejectedCount.intValue()
          if (itemCount.intValue() == 0) {
            stream put TeslaNoDataMarkerParcel
          } else {
            stream put TeslaEndMarkerParcel
          }
          JsonSampleSourceWorkerReporter.onWorkerCompletion(totalBytes, timer)
        } catch safely {
          case t: Throwable =>
            timer.stop
            log info burstStdMsg(s"$tag failed", t)
            stream put endMarkerMutableBuffer
            stream put TeslaExceptionMarkerParcel
            JsonSampleSourceWorkerReporter.onWorkerFailure(timer)
        }
      } catch safely {
        case t: Throwable =>
          val msg = s"$tag failed"
          stream put endMarkerMutableBuffer
          stream put TeslaExceptionMarkerParcel
          log error burstStdMsg(msg, t)
          throw VitalsException(msg, t)
      }
  }

  private val heartbeatInterval: FiniteDuration = burstSampleStoreHeartbeatInterval.getOrThrow milliseconds

  final def newHeartbeater(stream: NexusStream): VitalsBackgroundFunction =
    new VitalsBackgroundFunction(s"${stream.suid}-heartbeat", 0 seconds, heartbeatInterval, {
      try {
        stream.put(TeslaHeartbeatMarkerParcel)
      } catch safely {
        case t: Throwable =>
          log error burstStdMsg(t)
        // TODO should we throw here??
      }
    })
}
