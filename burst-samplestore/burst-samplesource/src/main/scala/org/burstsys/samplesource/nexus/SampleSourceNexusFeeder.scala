/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource.nexus

import org.burstsys.nexus.NexusGlobalUid
import org.burstsys.nexus.server.NexusStreamFeeder
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.samplesource.configuration._
import org.burstsys.samplesource.handler.SampleSourceHandlerRegistry
import org.burstsys.samplesource.service.SampleSourceWorkerService
import org.burstsys.samplesource.trek.SampleSourceFeedStreamTrek
import org.burstsys.samplestore.api._
import org.burstsys.samplestore.api.configuration.burstSampleStoreHeartbeatInterval
import org.burstsys.tesla.parcel
import org.burstsys.tesla.thread.request.{TeslaRequestFuture, teslaRequestExecutor}
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net.VitalsHostName
import org.burstsys.vitals.properties._
import org.burstsys.vitals.sysinfo.SystemInfoComponent
import org.jctools.queues.MpmcArrayQueue

import java.time.Clock
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success}

/**
 * The SampleSourceNexusFeeder is used by the NexusServer (which is run in the context of a samplestore worker)
 * to press data and send it via nexus to the cell worker that initiated the request. The stream designates which
 * sample source can fulfill the request by providing the [[SampleStoreSourceNameProperty]]
 */
final case class SampleSourceNexusFeeder() extends NexusStreamFeeder with SystemInfoComponent {
  /**
   * dispatch nexus feed stream request to appropriate sample source implementation
   *
   */
  override def feedStream(stream: NexusStream): Unit = {
    log info burstStdMsg(s"SampleSourceNexusFeeder feed parcel stream $stream")
    val start = System.nanoTime()
    val handler: SampleSourceWorkerService = handlerForStream(stream)
    // call handler
    log info burstStdMsg(s"Sending feed request to handler ${handler.name} for parcel stream $stream")
    SampleSourceFeedStreamTrek.begin(stream.guid, stream.suid) { stage =>
      stream.startHeartbeat(burstSampleStoreHeartbeatInterval.get)
      recordFeedStream(stream)
      handler.feedStream(stream) andThen { case _ =>
        stream.stopHeartbeat()
      } andThen {
        case Failure(t) =>
          log warn burstStdMsg(s"$handler failed to process parcel stream $stream", t)
          SampleSourceFeedStreamTrek.fail(stage, t)
        case Success(_) =>
          log info burstStdMsg(s"$handler finished processing parcel stream $stream in ${System.nanoTime() - start} nanos")
          SampleSourceFeedStreamTrek.end(stage)
      }
    }
  }

  private def handlerForStream(stream: NexusStream): SampleSourceWorkerService = {
    val sourceName = stream.properties.getValueOrThrow[VitalsPropertyKey](SampleStoreSourceNameProperty)
    SampleSourceHandlerRegistry.getWorker(sourceName)
  }

  // probably should have a way to abort the handler...
  override def abortStream(stream: NexusStream, status: parcel.TeslaParcelStatus): Unit = {
  }

  private case class FeedStreamRequest(time: String, guid: NexusGlobalUid, hostname: VitalsHostName, properties: VitalsPropertyMap)

  private val _requests = new MpmcArrayQueue[FeedStreamRequest](sampleStoreNexusFeedStreamLogSize.get)

  private def recordFeedStream(stream: NexusStream): Unit = {
    val req = FeedStreamRequest(
      java.time.OffsetTime.now(Clock.systemUTC()).toString,
      stream.guid,
      stream.clientHostname,
      stream.properties
    )

    while (!_requests.offer(req)) {
      // while we are unable to put this request on the queue, pull the first one out
      _requests.poll()
    }
  }

  /**
   * @return name of component
   */
  override def name: String = "SampleSourceNexusFeeder"

  /**
   * System info about component.
   *
   * @return Case classs that will be serialized to Json
   */
  override def status(level: Int): AnyRef = {
    case class StatusResponse(feedStreamRequests: Array[FeedStreamRequest] = _requests.asScala.toArray)
    StatusResponse()
  }
}
