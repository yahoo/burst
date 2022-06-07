/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.bench

import org.burstsys.brio.blob.BrioBlobEncoder
import org.burstsys.nexus.client.NexusClient
import org.burstsys.nexus.newNexusUid
import org.burstsys.nexus.server.NexusStreamFeeder
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.tesla.parcel._
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._
import org.burstsys.vitals.instrument._
import org.burstsys.vitals.net.{VitalsHostAddress, getPublicHostName}
import org.burstsys.vitals.properties.VitalsPropertyMap
import org.burstsys.vitals.uid.newBurstUid
import org.burstsys.{brio, tesla}

import scala.concurrent.{Future, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success}
import org.burstsys.vitals.logging._

/**
 * ==purpose==
 * on a nexus server, when an incoming connection from a nexus client on a new host is received, this class is used
 * to run a quick benchmark on a stream to determine the max write speed for test parcels.
 * ==algorithm==
 */
trait NexusNetBench {

  /**
   * run a cache disk write benchmark and return the results as a human
   */
  def benchmark(client: NexusClient): Future[NexusNetBenchmark]
}

object NexusNetBench {

  def apply(byteSize: Long): NexusNetBench = NexusNetBenchContext(byteSize: Long)

  def apply(host: VitalsHostAddress, client: NexusClient): NexusClient = {
    log info burstStdMsg(s"NexusNetBench(host=$host)")
    // first time we see a host, we want to do a quick benchmark
    //    _hostCache.getOrElseUpdate(host, (cacheBenchmark(client)))
    client
  }

  private def cacheBenchmark(client: NexusClient): NexusClient = {
    TeslaRequestCoupler {
      NexusNetBenchContext(byteSize = 1 * GB).benchmark(client) onComplete {
        case Failure(t) => log error burstStdMsg(s"cache benchmark failed $t", t)
        case Success(result) => log info result.report
      }
      client
    }
  }

}

private final case
class NexusNetBenchContext(byteSize: Long) extends NexusNetBench with NexusStreamFeeder {

  private final val buffersPerParcel = 300

  val referenceBlob: TeslaMutableBuffer = createReferenceBlob(density = ???)
  val memorySize: TeslaMemorySize = referenceBlob.currentUsedMemory

  override
  def benchmark(client: NexusClient): Future[NexusNetBenchmark] = {
    val guid = newBurstUid
    val suid = newNexusUid
    val tag = s"NexusNetBench.benchmark(guid=$guid, suid=$suid)"

    val promise = Promise[NexusNetBenchmark]
    TeslaRequestFuture {
      val pipe = TeslaParcelPipe(name = loopback, guid = guid, suid = suid, depth = 10e3.toInt).start
      val properties: VitalsPropertyMap = Map(
        loopbackModeProperty -> "true",
        loopbackByteSizePropery -> byteSize.toString
      )
      client.startStream(guid, suid, properties, loopback, None, pipe, 0, getPublicHostName, client.serverHost)
      var continue = true
      var inflatedByteTally = 0
      var deflatedByteTally = 0
      var start = System.nanoTime
      while (continue) {
        val parcel = pipe.take
        if (parcel == TeslaEndMarkerParcel) {
          continue = false
          promise.success(NexusNetBenchmark(deflatedByteTally, inflatedByteTally, System.nanoTime - start))
        } else if (parcel == TeslaTimeoutMarkerParcel) {
          continue = false
          val msg = s"$tag TIMEOUT"
          log warn burstStdMsg(msg)
          promise.failure(VitalsException(msg).fillInStackTrace())
        } else {
          inflatedByteTally += parcel.inflatedSize
          deflatedByteTally += parcel.deflatedSize
          tesla.parcel.factory releaseParcel parcel
        }
      }
    }
    promise.future
  }

  override
  def feedStream(stream: NexusStream): Unit = {
    TeslaRequestFuture {
      var itemCount: Long = 0L
      var byteCount: Long = 0L
      try {
        var inflatedParcel = tesla.parcel.factory.grabParcel(10e6.toInt)
        inflatedParcel.startWrites()
        var bufferTally = 0

        def pushParcel(): Unit = {
          bufferTally = 0
          if (inflatedParcel.currentUsedMemory == 0)
            throw VitalsException(s"")
          stream put inflatedParcel
          inflatedParcel = tesla.parcel.factory.grabParcel(10e6.toInt)
          inflatedParcel.startWrites()
        }

        while (byteCount <= byteSize) {
          val buffer = tesla.buffer.factory.grabBuffer(memorySize + 1e5.toInt)
          try {
            itemCount += 1
            bufferTally += 1
            byteCount += memorySize
            buffer.loadBytes(referenceBlob.dataPtr, memorySize)
            inflatedParcel.writeNextBuffer(buffer)

            if (bufferTally > buffersPerParcel) pushParcel()

          } finally tesla.buffer.factory.releaseBuffer(buffer)
        }

        if (bufferTally > 0) pushParcel()

        stream put TeslaEndMarkerParcel
      } catch safely {
        case t: Throwable =>
          log error t
      }
    }
  }

  override
  def abortStream(_stream: NexusStream, status: TeslaParcelStatus): Unit = {
  }

  val schemaVersion = 1

  def createReferenceBlob(density: Int = 6): TeslaMutableBuffer = {
    val blobBuffer = tesla.buffer.factory.grabBuffer(30e6.toInt)
    val brioTreeBuffer = tesla.buffer.factory.grabBuffer(30e6.toInt)
    // load random bytes
    val dictionary = brio.dictionary.factory.grabMutableDictionary()
    // load random words
    try {
      BrioBlobEncoder.encodeV2Blob(brioTreeBuffer, schemaVersion, dictionary, blobBuffer)
      blobBuffer
    } finally {
      tesla.buffer.factory.releaseBuffer(brioTreeBuffer)
      brio.dictionary.factory.releaseMutableDictionary(dictionary)
    }
  }


}
