/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.synthetic.samplestore.test

import org.burstsys.brio.types.BrioTypes.BrioSchemaName
import org.burstsys.nexus.{NexusGlobalUid, NexusSliceKey, NexusStreamUid}
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.samplestore.test.BaseSampleStoreTest
import org.burstsys.synthetic.samplestore.source.SyntheticSampleSourceWorker
import org.burstsys.tesla
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.tesla.parcel.TeslaParcel
import org.burstsys.vitals.net.VitalsHostName
import org.burstsys.vitals.properties.{BurstMotifFilter, VitalsPropertyMap}
import org.burstsys.vitals.uid

import java.util.concurrent.ConcurrentLinkedQueue
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class SynteticSampleSourceWorkerSpec extends BaseSampleStoreTest {

  it should "generate the requested number of items" in {
    for (count <- Seq(1, 20, 100, 1000, 10000)) {
      val props = Map(
        "synthetic.samplestore.press.dataset" -> "simple-unity",
        "synthetic.samplestore.press.item.count" -> s"$count",
        "synthetic.unity.sessionCount" -> "0",
      )
      val stream = MockNexusStream("unity", props)
      val worker = SyntheticSampleSourceWorker()
      Await.result(worker.feedStream(stream), 5.seconds)

      stream.expectedItemCount shouldEqual count
      stream.itemCount shouldEqual count
      stream.potentialItemCount shouldEqual count
      stream.rejectedItemCount shouldEqual 0

      stream.buffers.size() shouldEqual count
      stream.buffers.forEach(tesla.buffer.factory.releaseBuffer)
    }
  }

}

case class MockNexusStream(
                            schema: BrioSchemaName,
                            properties: VitalsPropertyMap,
                            guid: NexusGlobalUid = uid.newBurstUid,
                            suid: NexusStreamUid = uid.newBurstUid,
                            var itemCount: Long = 0,
                            var expectedItemCount: Long = 0,
                            var potentialItemCount: Long = 0,
                            var rejectedItemCount: Long = 0,
                          ) extends NexusStream {

  val buffers = new ConcurrentLinkedQueue[TeslaMutableBuffer]()

  override def filter: BurstMotifFilter = ???

  override def sliceKey: NexusSliceKey = ???

  override def clientHostname: VitalsHostName = ???

  override def serverHostname: VitalsHostName = ???

  override def completion: Future[NexusStream] = ???

  override def put(chunk: TeslaParcel): Unit = ???

  override def put(buffer: TeslaMutableBuffer): Unit = buffers.offer(buffer)

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
  override def timedOut(limit: Duration): Unit = ???

  override def completeExceptionally(exception: Throwable): Unit = ???

  override def start: MockNexusStream.this.type = this

  override def stop: MockNexusStream.this.type = this

  override def abort(): Unit = ???
}
