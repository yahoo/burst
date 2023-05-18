/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.test.source

import org.burstsys.nexus.stream.NexusStream
import org.burstsys.samplesource.service.{MetadataParameters, SampleSourceWorkerService}
import org.burstsys.samplestore.test.configuration
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.properties._

import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.{Future, TimeoutException}
import scala.language.postfixOps

case class TestSampleSourceWorker() extends SampleSourceWorkerService {

  /**
   * @return The name of this sample source worker.
   */
  override def name: String = TestSampleSourceName

  /**
   * Instantiate an instance of a synthetic data provider and use it to feed the stream.
   *
   * @param stream the stream of the incoming request
   * @return a future that completes when the stream has been fed
   */
  override def feedStream(stream: NexusStream): Future[Unit] = {
    testListener.foreach(_.onFeedStream(stream))
    TeslaRequestFuture {
      val props = stream.properties.extend
      val timeout = props.getValueOrProperty(configuration.defaultPressTimeoutProperty)
      try {
        val itemCount = 0
        val rejectedItemCounter = new AtomicInteger()

        stream.complete(itemCount, expectedItemCount = itemCount, potentialItemCount = itemCount, rejectedItemCounter.longValue)

      } catch safely {
        case _: TimeoutException =>
          stream.timedOut(timeout)

        case t =>
          log error("Synthetic samplesource feedStream failed", t)
          stream.completeExceptionally(t)
      }
    }
  }

  override def putBroadcastVars(metadata: MetadataParameters): Unit = {
    testListener.foreach(_.onPutBroadcastVars(metadata))
  }
}
