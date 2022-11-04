/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.benchmark

import java.util.concurrent.atomic.LongAdder

import org.burstsys.brio.blob.BrioBlobEncoder
import org.burstsys.brio.press.{BrioPressSink, BrioPresser}
import org.burstsys.nexus.client.NexusClient
import org.burstsys.nexus.server.{NexusServer, NexusStreamFeeder}
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.nexus.{NexusUid, newNexusUid}
import org.burstsys.tesla.buffer
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.tesla.parcel.{TeslaEndMarkerParcel, TeslaParcelStatus, TeslaTimeoutMarkerParcel}
import org.burstsys.tesla.thread.request.{TeslaRequestFuture, teslaRequestExecutor}
import org.burstsys.alloy.views.unity.UnityGeneratedDataset
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._
import org.burstsys.vitals.host
import org.burstsys.vitals.instrument.{prettyByteSizeString, prettyRateString, prettySizeString, prettyTimeFromNanos}
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net.{getPublicHostAddress, getPublicHostName}
import org.burstsys.vitals.properties.{BurstMotifFilter, VitalsPropertyMap}
import org.burstsys.{brio, tesla}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

object NexusBenchmarkMain {


  final case class NexusBenchmarkerArguments(servers: Int = 2, clients: Int = 2, density: Int = 6, bytes: Long = 10e9.toLong) {
    def printArgs: String =
      s"""
         |  ${host.osName}
         |  $servers server(s) X $clients client(s) = ${servers * clients} concurrent connection(s)
         |  ${prettyByteSizeString(bytes)} xfer size
         |  density=$density""".stripMargin
  }

  def main(args: Array[String]): Unit = {

    VitalsLog.configureLogging("supervisor")

    var defaultArguments = NexusBenchmarkerArguments()

    val parser = new scopt.OptionParser[NexusBenchmarkerArguments]("NexusBenchmarker") {
      opt[Int]('d', "density") text s"density of sessions (default '${defaultArguments.density}')" maxOccurs 1 action {
        case (newValue, arguments) => arguments.copy(density = newValue)
      }
      opt[Long]('b', "bytes") text s"size in bytes (default '${defaultArguments.bytes}')" maxOccurs 1 action {
        case (newValue, arguments) => arguments.copy(bytes = newValue)
      }
      opt[Int]('s', "servers") text s"number of servers (default '${defaultArguments.servers}')" maxOccurs 1 action {
        case (newValue, arguments) => arguments.copy(servers = newValue)
      }
      opt[Int]('c', "clients") text s"number of clients (default '${defaultArguments.clients}')" maxOccurs 1 action {
        case (newValue, arguments) => arguments.copy(clients = newValue)
      }
      help("help")
    }

    parser.parse(args.toSeq, defaultArguments) match {
      case None =>
        parser.showUsageOnError
        System.exit(-1)
      case Some(args) => executeBwRun(args)
    }

  }

  final
  def createReferenceBlob(density: Int = 6): TeslaMutableBuffer = {
    val dataset = UnityGeneratedDataset(
      domainKey = 1,
      viewKey = 1,
      numberOfItems = 1,
      numberOfSessions = density
    )
    val item = dataset.items.head
    val pressBuffer = tesla.buffer.factory.grabBuffer(30e6.toInt)
    val blobBuffer = tesla.buffer.factory.grabBuffer(30e6.toInt)
    val dictionary = brio.dictionary.factory.grabMutableDictionary()
    val sink = BrioPressSink(pressBuffer, dictionary)
    val presser = BrioPresser(dataset.schema, sink, dataset.presser(item))
    try {
      presser.press
      BrioBlobEncoder.encodeV2Blob(sink.buffer, dataset.rootVersion, sink.dictionary, blobBuffer)
      blobBuffer
    } finally {
      tesla.buffer.factory.releaseBuffer(pressBuffer)
      brio.dictionary.factory.releaseMutableDictionary(dictionary)
    }
  }


  final
  def executeBwRun(args: NexusBenchmarkerArguments): Unit = {

    log info s"\nNEXUS BW TEST: ${args.printArgs}"

    val start = System.nanoTime

    val guid = newNexusUid

    val byteCount = new LongAdder
    val itemCount = new LongAdder

    val referenceBlob = createReferenceBlob(density = args.density)
    val memorySize = referenceBlob.currentUsedMemory
    try {
      val connectionOutcomes = for (serverId <- 0 until args.servers) yield {
        TeslaRequestFuture {
          try {
            singleServerMultipleClientRun(serverId, args, memorySize, referenceBlob, guid, itemCount, byteCount)
          } catch safely {
            case t: Throwable =>
              log error t
          }
        }
      }
      Await.result(Future.sequence(connectionOutcomes), 10 minutes)
    } finally buffer.factory releaseBuffer referenceBlob

    val elapsedNs = System.nanoTime - start
    log info
      s"""
         |NEXUS BW TEST: ${args.printArgs}
         |  ${prettySizeString(itemCount.sum())} items
         |  ${prettyTimeFromNanos(elapsedNs)} elapsed
         |  ${prettyByteSizeString(byteCount.sum())} total bytes
         |  ${prettyRateString("byte", byteCount.sum(), elapsedNs)} (duplex: ${prettyRateString("byte", 2 * byteCount.sum(), elapsedNs)})
         |  ${prettyRateString("item", itemCount.sum(), elapsedNs)} (duplex: ${prettyRateString("item", 2 * itemCount.sum(), elapsedNs)})
         |  ${prettyByteSizeString(byteCount.sum() / itemCount.sum())}/item
       """.stripMargin
  }

  private final val buffersPerParcel = 300

  private
  def singleServerMultipleClientRun(
                                     serverId: Int,
                                     args: NexusBenchmarkerArguments,
                                     memorySize: Int,
                                     referenceBlob: TeslaMutableBuffer,
                                     guid: NexusUid,
                                     itemCount: LongAdder,
                                     byteCount: LongAdder
                                   ): Unit = {

    val server = NexusServer(getPublicHostAddress) fedBy new NexusStreamFeeder {

      override def feedStream(stream: NexusStream): Unit = {
        TeslaRequestFuture {
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

            while (byteCount.sum() <= args.bytes) {
              val buffer = tesla.buffer.factory.grabBuffer(memorySize + 1e5.toInt)
              try {
                itemCount.increment()
                bufferTally += 1
                byteCount.add(memorySize)
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

    }

    server.start

    try {
      val clientOutcomes = for (clientId <- 0 until args.clients) yield {
        TeslaRequestFuture {
          startParcelClient(serverId, clientId, guid, server)
        }
      }
      Await.result(Future.sequence(clientOutcomes), 10 minutes)
    } finally server.stop
  }

  private
  def startParcelClient(serverId: Int, clientId: Int, guid: NexusUid, server: NexusServer): Unit = {
    val client = NexusClient(getPublicHostAddress, server.serverPort).start
    try {
      val suid = newNexusUid
      val pipe = TeslaParcelPipe(name = "mock", guid = guid, suid = suid, depth = 10e3.toInt).start
      val streamProperties: VitalsPropertyMap = Map("someKey" -> "someValue")
      val motifFilter: BurstMotifFilter = Some("someMotifFilter")
      val stream = client.startStream(guid, suid, streamProperties, "unity", motifFilter, pipe, 0, getPublicHostName, getPublicHostName)

      var continue = true
      while (continue) {
        val parcel = pipe.take
        if (parcel == TeslaEndMarkerParcel) {
          continue = false
        } else if (parcel == TeslaTimeoutMarkerParcel) {
          throw VitalsException(s"TeslaParcelPipeTimeout")
        } else {
          tesla.parcel.factory releaseParcel parcel
        }

      }
      log info s"server #$serverId, client #$clientId finished"

      Await.result(stream.receipt, 60 seconds)
    } finally client.stop
  }
}
