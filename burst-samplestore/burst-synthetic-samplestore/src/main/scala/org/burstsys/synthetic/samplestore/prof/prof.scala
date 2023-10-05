/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.synthetic.samplestore.prof

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator
import org.burstsys.brio.types.BrioTypes.BrioSchemaName
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.nexus.{NexusGlobalUid, NexusSliceKey, NexusStreamUid}
import org.burstsys.synthetic.samplestore.source.SyntheticSampleSourceWorker
import org.burstsys.tesla
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.tesla.parcel.TeslaParcel
import org.burstsys.vitals.logging.{VitalsLog, VitalsLogger}
import org.burstsys.vitals.net.VitalsHostName
import org.burstsys.vitals.properties.{BurstMotifFilter, VitalsPropertyMap}
import org.burstsys.vitals.uid

import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, Future}

object prof extends VitalsLogger {
  val count = 100000000
  private val waitDuration = 5.days

  def main(args: Array[String]): Unit = {
    VitalsLog.configureLogging("synthetic-samplestore", consoleOnly = true)
    Configurator.setLevel(org.burstsys.tesla.part.log.getName, Level.INFO)
    // Configurator.setLevel(org.burstsys.samplestore.log, Level.DEBUG) // see press queue
    // Configurator.setLevel(org.burstsys.brio.flurry.provider.unity.log.getName, Level.TRACE) // see data created
    // Configurator.setLevel(org.burstsys.tesla.buffer.log.getName, Level.TRACE)
    // Configurator.setLevel(org.burstsys.brio.dictionary.factory.log.getName, Level.TRACE) // see dictionary

    val props = Map(
      "synthetic.samplestore.press.dataset" -> "simple-unity",
      "synthetic.samplestore.press.item.count" -> s"$count",
      "synthetic.samplestore.press.item.batchcount" -> "160",
      "synthetic.samplestore.load.max.bytes" -> s"${10e12.toLong}",
      "synthetic.samplestore.press.timeout" -> waitDuration.toString,
    )
    val stream = MockNexusStream("unity", props)
    val worker = SyntheticSampleSourceWorker()
    Await.result(worker.feedStream(stream), waitDuration.plus(1.second))
  }

  private case class MockNexusStream(
                              schema: BrioSchemaName,
                              properties: VitalsPropertyMap,
                              guid: NexusGlobalUid = uid.newBurstUid,
                              suid: NexusStreamUid = uid.newBurstUid,
                              var itemCount: Long = 0,
                              var expectedItemCount: Long = 0,
                              var potentialItemCount: Long = 0,
                              var rejectedItemCount: Long = 0,
                            ) extends NexusStream {

    override def filter: BurstMotifFilter = ???

    override def sliceKey: NexusSliceKey = ???

    override def clientHostname: VitalsHostName = ???

    override def serverHostname: VitalsHostName = ???

    override def completion: Future[NexusStream] = ???

    override def put(chunk: TeslaParcel): Unit = ???

    override def putItemCount: Long = counter.get()
    private val counter = new java.util.concurrent.atomic.AtomicLong(0)

    override def putBytesCount: Long = bytes.get()
    private val bytes = new java.util.concurrent.atomic.AtomicLong(0)

    override def put(buffer: TeslaMutableBuffer): Unit = {
      val c = counter.incrementAndGet()
      bytes.addAndGet(buffer.currentUsedMemory)
      if (c % 100000 == 0)
        log info s"$c/$count(${((c.toDouble/count.toDouble)*1000.0).floor/10.0}%) $putBytesCount bytes put on stream"
      tesla.buffer.factory.releaseBuffer(buffer)
    }

    override def take: TeslaParcel = ???

    override def startHeartbeat(interval: Duration): Unit = {}

    override def stopHeartbeat(): Unit = {}

    /**
     * Called by the server to mark the stream as complete and to send the appropriate signoff to the client
     *
     * @param itemCount          the number of items sent
     * @param expectedItemCount  the number of items we expected to send
     * @param potentialItemCount the number of items that exist in the dataset
     * @param rejectedItemCount  the number of items that failed to press
     */
    override def complete(itemCount: Long, expectedItemCount: Long, potentialItemCount: Long, rejectedItemCount: Long, parcel: TeslaParcel): Unit = {
      this.itemCount = itemCount
      this.expectedItemCount = expectedItemCount
      this.potentialItemCount = potentialItemCount
      this.rejectedItemCount = rejectedItemCount
    }

    /**
     * Called by the server to mark the stream as timed out, usually because pressing took too long, and send
     * the appropriate signoff to the client
     *
     * @param limit the timeout used by the server
     */
    override def timedOut(limit: Duration): Unit = {
      log error s"timed out after $limit"
    }

    override def completeExceptionally(exception: Throwable): Unit = ???

    override def start: MockNexusStream.this.type = this

    override def stop: MockNexusStream.this.type = this

    override def abort(): Unit = ???
  }
}
