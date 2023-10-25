/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource.test

import org.burstsys.brio.press.BrioPressInstance
import org.burstsys.brio.provider.BrioSchemaProvider
import org.burstsys.brio.types.BrioTypes.{BrioSchemaName, BrioVersionKey}
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.nexus.{NexusGlobalUid, NexusSliceKey, NexusStreamUid}
import org.burstsys.tesla
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.tesla.parcel.TeslaParcel
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net.VitalsHostName
import org.burstsys.vitals.properties.{BurstMotifFilter, VitalsPropertyMap}
import org.burstsys.vitals.uid

import scala.concurrent.Future
import scala.concurrent.duration.Duration

package object pipeline extends VitalsLogger {

  final case class BrioPressProvider() extends BrioSchemaProvider {

    val names: Array[String] = Array("presser")

    val schemaResourcePath: String = "/org/burstsys/brio/test/press"

  }

  trait PresserInstance extends BrioPressInstance {
    final override val schemaVersion: BrioVersionKey = 2
  }

  case
  class RootStructure(f0: String, f1: Long, f2: Short, f3: SecondLevelStructure, f4: Array[SecondLevelStructure],
                      added: AddedStructure, application: ApplicationStructure) extends PresserInstance

  final case
  class SecondLevelStructure(f0: Long, f1: Long, f2: Double, f3: Array[ThirdLevelStructure], f4: ThirdLevelStructure)
    extends PresserInstance

  final case
  class ThirdLevelStructure(f0: Long, f1: Long, f2: Map[String, String], f3: Array[Double]) extends PresserInstance

  final case
  class AddedStructure(f0: String, f1: Double, f2: Array[String], f3: Boolean, f4: Boolean) extends PresserInstance

  // For testing the Unity schema, containing multiple appearances of Use.

  final case class ApplicationStructure(firstUse: UseStructure, mostUse: UseStructure, lastUse: UseStructure) extends PresserInstance

  final case class UseStructure(tag: String) extends PresserInstance

  private[pipeline] case class MockNexusStream(
                                      schema: BrioSchemaName,
                                      properties: VitalsPropertyMap = Map.empty,
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

    override def putBytesCount: Long = bytes.get()

    private val counter = new java.util.concurrent.atomic.AtomicLong(0)
    private val bytes = new java.util.concurrent.atomic.AtomicLong(0)

    override def put(buffer: TeslaMutableBuffer): Unit = {
      val c = counter.incrementAndGet()
      bytes.addAndGet(buffer.currentUsedMemory)
      if (c % 100000 == 0 || c == expectedItemCount)
        log info s"$c/$expectedItemCount(${((c.toDouble / expectedItemCount.toDouble) * 1000.0).floor / 10.0}%) buffer ${buffer.basePtr} put on stream"
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
    override def timedOut(limit: Duration): Unit = ???

    override def completeExceptionally(exception: Throwable): Unit = ???

    override def start: MockNexusStream.this.type = this

    override def stop: MockNexusStream.this.type = this

    override def abort(): Unit = ???
  }

}
