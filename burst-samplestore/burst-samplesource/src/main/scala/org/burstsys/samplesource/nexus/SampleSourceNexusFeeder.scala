/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource.nexus

import org.burstsys.nexus.server.NexusStreamFeeder
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.samplesource.handler.SampleSourceHandlerRegistry
import org.burstsys.samplesource.handler.SampleSourceHandlerRegistry._
import org.burstsys.samplestore.api._
import org.burstsys.tesla.parcel
import org.burstsys.tesla.thread.request.{TeslaRequestFuture, teslaRequestExecutor}
import org.burstsys.vitals.errors._
import org.burstsys.vitals.properties._

import scala.util.{Failure, Success}
import org.burstsys.vitals.logging._

/**
 * The SampleSourceNexusFeeder is used by the NexusServer (which is run in the context of a samplestore worker)
 * to press data and send it via nexus to the cell worker that initiated the request. The stream designates which
 * sample source can fulfill the request by providing the [[SampleStoreSourceNameProperty]]
 */
final case class SampleSourceNexusFeeder() extends NexusStreamFeeder {

  /**
   * dispatch nexus feed stream request to appropriate sample source implementation
   *
   * @param stream
   */
  override
  def feedStream(stream: NexusStream): Unit = {
    TeslaRequestFuture {
      log info burstStdMsg(s"SampleSourceNexusFeeder feed parcel stream $stream")
      val start = System.nanoTime()
      val sourceName = stream.properties.getValueOrThrow[String](SampleStoreSourceNameProperty)
      val handler = SampleSourceHandlerRegistry.getWorker(sourceName)
      // call handler
      log info burstStdMsg(s"Sending feed request to handler ${handler.name} for parcel stream $stream")
      handler.feedStream(stream) onComplete {
        case Failure(t) =>
          log warn burstStdMsg(s"$handler failed to process parcel stream ")
          throw t
        case Success(_) =>
          val duration = System.nanoTime() - start
          log info burstStdMsg(s"$handler finished processing parcel stream in $duration nanos")
      }
    }
  }

  override
  def abortStream(stream: NexusStream, status: parcel.TeslaParcelStatus): Unit = {

  }

}
