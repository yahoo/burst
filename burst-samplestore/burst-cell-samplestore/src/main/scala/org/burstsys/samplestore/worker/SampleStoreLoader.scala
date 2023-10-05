/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.worker

import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.LongAdder
import org.burstsys.fabric.wave.data.model.slice.metadata.FabricSliceMetadata
import org.burstsys.fabric.wave.data.model.slice.state.{FabricDataFailed, FabricDataNoData, FabricDataState, FabricDataWarm}
import org.burstsys.fabric.wave.data.model.snap.FabricSnap
import org.burstsys.nexus
import org.burstsys.nexus.client.NexusClient
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.nexus.{configuration, _}
import org.burstsys.samplestore.api.configuration.burstSampleStoreHeartbeatDuration
import org.burstsys.samplestore.model.SampleStoreSlice
import org.burstsys.samplestore.trek.{SampleStoreLoaderProcessStreamTrekMark, SampleStoreLoaderReleaseStreamsTrekMark}
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.tesla.parcel.{TeslaAbortMarkerParcel, TeslaEndMarkerParcel, TeslaExceptionMarkerParcel, TeslaHeartbeatMarkerParcel, TeslaNoDataMarkerParcel, TeslaParcelStatus, TeslaTimeoutMarkerParcel}
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._
import org.burstsys.vitals.reporter.instrument._
import org.burstsys.vitals.net.{convertHostAddressToHostname, getPublicHostName}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.language.postfixOps
import org.burstsys.vitals.logging._

/**
 * load state and tracking semantics for worker side
 */
private[worker]
case class SampleStoreLoader(snap: FabricSnap, slice: SampleStoreSlice) {

  lazy val parameters = s"guid=${snap.guid}, suid=${_suids})"

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this] lazy val _suids: String = slice.loci.map(_.suid).mkString("_")

  private[this] lazy val _expectedTransfers: Int = slice.loci.length

  private[this] val _inputWaitNanos = new LongAdder

  private[this] val _outputWaitNanos = new LongAdder

  private[this] val _startNanos: Long = System.nanoTime

  private[this] val _startEpoch: Long = System.currentTimeMillis

  private[this] val _byteCount = new LongAdder

  private[this] val _itemCount = new LongAdder

  private[this] val _potentialItemCount = new LongAdder

  private[this] val _rejectedItemCount = new LongAdder

  private[this] val _reportedItemCount = new LongAdder

  private[this] val _expectedItemCount = new LongAdder

  private[this] val _exceptionStreamCount = new LongAdder

  private[this] var _pipe: TeslaParcelPipe = _

  private[this] val _nexusClients = new ArrayBuffer[NexusClient]

  private[this] val _streams = new ArrayBuffer[NexusStream]

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  final def initializeLoader(): Unit = {
    val tag = s"SampleStoreLoader.initializeLoader($parameters)"
    try {
      log debug burstStdMsg(tag)
      val depth = configuration.burstNexusPipeSizeProperty.get
      val timeout = 3 * burstSampleStoreHeartbeatDuration // no parcels in a 3 heartbeat interval means it's dead Jim
      _pipe = TeslaParcelPipe(s"samplestore.mux n=${slice.loci.length}", snap.guid, _suids, depth, timeout)
      _pipe.start
    } catch safely {
      case t: Throwable =>
        val msg = s"FAIL $t $tag"
        log error burstStdMsg(msg, t)
        throw VitalsException(msg, t)
    }
  }

  /**
   * start all streams from all the remote nodes and set them up to feed into our pipe
   */
  final def acquireStreams(): Unit = {
    lazy val tag = s"SampleStoreLoader.acquireStreams($parameters)"
    val metadata = snap.metadata
    try {
      log debug s"SAMPLE_STORE_STREAMS_ACQUIRE $tag"
      slice.loci foreach {
        l =>
          val client = nexus.grabClientFromPool(l.hostAddress, l.port)
          val serverHostname = convertHostAddressToHostname(l.hostAddress)
          _nexusClients += client
          log debug s"SAMPLE_STORE_ACQUIRE_STREAM serverHostname=$serverHostname serverIPAddress=${l.hostAddress} serverPort=${l.port} $tag"
          try {
            _streams += client.startStream(
              snap.guid, l.suid, l.partitionProperties, slice.datasource.view.schemaName, Some(slice.motifFilter), _pipe,
              slice.sliceKey, getPublicHostName, serverHostname
            )
          } catch safely {
            case t: Throwable =>
              log error burstStdMsg(s"SAMPLE_STORE_START_STREAM_FAILURE $tag", t)
              _exceptionStreamCount add 1
              metadata.failure(t)
          }
      }
    } catch safely {
      case t: Throwable =>
        val msg = s"SAMPLE_STORE_ACQUIRE_STREAM_FAILURE $t $tag"
        log error burstStdMsg(msg, t)
        throw VitalsException(msg, t)
    }
  }

  /**
   * release all stream resources
   */
  final def releaseStreams(): Unit = {
    lazy val tag = s"SampleStoreLoader.releaseStreams($parameters)"
    SampleStoreLoaderReleaseStreamsTrekMark.begin(snap.guid) { stage =>
      try {
        val metadata = snap.metadata
        log debug burstStdMsg(tag)
        // make sure all streams have completed
        // wait long period - we really do not want to give up easily at this point just for hygiene
        _streams foreach {
          stream =>
            try {
              // TODO: if `!stream.receipt.isCompleted`, send abort message telling the server to shut down.
              //       we hold the snap's write lock for as long as we wait so we should end quickly here
              Await.result(stream.completion, releaseStreamTimeout)
            } catch safely {
              case _: TimeoutException =>
                val msg = s"SAMPLE_STORE_RELEASE_STREAM_TIMEOUT timeout=$releaseStreamTimeout stream=$stream"
                log error burstStdMsg(msg)
                metadata.failure(msg)
              // TODO: we need a way to stop the NexusClientConnection from the stream (_isStreamingData = false).
              //       For now we just wind up throwing away the client that provided this stream
            }
        }
        _nexusClients foreach releaseClientToPool
        _pipe.stop // allow pipe to clean up
        SampleStoreLoaderReleaseStreamsTrekMark.end(stage)
      } catch safely {
        case t: Throwable =>
          val msg = s"SAMPLE_STORE_FAIL $t $tag"
          log error burstStdMsg(msg, t)
          SampleStoreLoaderReleaseStreamsTrekMark.fail(stage, t)
          throw VitalsException(msg, t)
      }
    }
  }

  /**
   * try to abort all incoming streams
   */
  final def abortStreams(status: TeslaParcelStatus): Unit = {
    _nexusClients foreach (_.abortStream(status))
  }

  /**
   * process all the parcels coming into the pipe from all the streams
   * send them to the cache write pipeline
   */
  final def processStreamData(): FabricDataState = {
    lazy val tag = s"SampleStoreLoader.processStreamData($parameters)"
    val metadata = snap.metadata
    val stage = SampleStoreLoaderProcessStreamTrekMark.beginSync(snap.guid)
    try {
      log debug burstStdMsg(tag)
      // process all streams
      val start = System.nanoTime

      var dataReceived = false
      var timeouts = 0
      var streamsCompleted = 0
      var streamsNoData = 0
      var streamsAborted = 0

      def xferStatus = s"transfers=${_expectedTransfers} completed=$streamsCompleted noData=$streamsNoData exceptions=${_exceptionStreamCount.longValue}"

      while (streamsCompleted < _streams.size && timeouts < TimeoutLimit) {
        ///////////////////////////////////////////////
        // track how long we spend waiting for parcels to arrive
        ///////////////////////////////////////////////
        val inputWaitStart = System.nanoTime
        val parcel = _pipe.take
        _inputWaitNanos add System.nanoTime - inputWaitStart

        /**
         * check for parcel 'markers'
         * we want to do what we can to fail-fast here i.e. abort the entire operation and throw away the data
         * however we don't want the non failing streams to start leaking parts
         */
        parcel match {
          // this is a special case marker that is generated when the pipe doesn't have anything for us
          case TeslaTimeoutMarkerParcel =>
            timeouts += 1
            val msg = s"SAMPLE_STORE_PIPE_TIMEOUT"
            log info s"$msg $tag duration=${prettyTimeFromNanos(start - System.nanoTime)} pipeTimeout=${_pipe.timeoutDuration} $xferStatus"

          // -------------------------------------
          // marker processing
          // -------------------------------------
          case TeslaEndMarkerParcel =>
            streamsCompleted += 1
            log debug s"SAMPLE_STORE_PIPE_END $tag $xferStatus"

          case TeslaNoDataMarkerParcel =>
            streamsCompleted += 1
            streamsNoData += 1
            log debug s"SAMPLE_STORE_PIPE_NO_DATA $tag $xferStatus"

          case TeslaExceptionMarkerParcel =>
            streamsCompleted += 1
            _exceptionStreamCount add 1
            val msg = s"SAMPLE_STORE_PIPE_EXCEPTION"
            log info s"$msg $tag $xferStatus"
            metadata.failure(s"$msg $xferStatus")

          case TeslaAbortMarkerParcel =>
            streamsCompleted += 1
            streamsAborted += 1
            val msg = s"SAMPLE_STORE_PIPE_ABORT"
            log info s"$msg $tag $xferStatus"
            metadata.failure(s"$msg $xferStatus")

          case TeslaHeartbeatMarkerParcel =>
            log debug s"SAMPLE_STORE_PIPE_HEARTBEAT $tag $xferStatus"

          // -----------------------------------
          // normal processing
          // -----------------------------------
          case _ =>
            dataReceived = true
            _itemCount add parcel.bufferCount
            _byteCount add parcel.inflatedSize

            // time how long it takes to push out a parcel
            val outputStartWait = System.nanoTime()
            snap.data queueParcelForWrite parcel
            _outputWaitNanos add System.nanoTime - outputStartWait

        }
      }

      lazy val countMsg = s"itemsReceived=${_itemCount}"

      // Aggregate the potential & rejected Size for SS load over all the loci
      _streams.foreach { s =>
        _expectedItemCount add s.expectedItemCount
        _potentialItemCount add s.potentialItemCount
        _rejectedItemCount add s.rejectedItemCount
        _reportedItemCount add s.itemCount
      }

      // -----------------------------------
      // anything bad happen here?
      // -----------------------------------
      if (timeouts >= TimeoutLimit) {
        metadata.failure(s"SAMPLE_STORE_STREAM_TIMEOUT $xferStatus $countMsg")

      } else if (streamsAborted > 0) {
        metadata.failure(s"SAMPLE_STORE_STREAM_ABORT $xferStatus $countMsg")

      } else if (_exceptionStreamCount.longValue > 0) {
        metadata.failure(s"SAMPLE_STORE_STREAM_EXCEPTION in ${_exceptionStreamCount}  $countMsg")

      } else if (_reportedItemCount.longValue != _itemCount.longValue) {
        metadata.failure(s"SAMPLE_STORE_STREAM_INCOMPLETE $countMsg reportedItems=${_reportedItemCount} expectedItems=${_expectedItemCount}")

      } else if (!dataReceived) { // we did not receive any data from any of the streams
        metadata.state = FabricDataNoData
        log info s"SAMPLE_STORE_STREAM_NO_DATA $tag $countMsg emptyStreams=$streamsNoData"

      } else { // all is well
        metadata.state = FabricDataWarm
        log info s"SAMPLE_STORE_STREAM_GOOD_DATA $tag $countMsg emptyStreams=$streamsNoData"
      }

      if (snap.metadata.state == FabricDataFailed) {
        SampleStoreLoaderProcessStreamTrekMark.fail(stage, VitalsException(snap.metadata.failure))
        log error burstStdMsg(s"FAIL ${snap.metadata.failure} $tag")
      } else {
        SampleStoreLoaderProcessStreamTrekMark.end(stage)
      }

      metadata.state
    } catch safely {
      case t: Throwable =>
        val msg = s"SAMPLE_STORE_FAIL $t $tag"
        log error burstStdMsg(msg, t)
        metadata.failure(msg)
        throw VitalsException(msg, t)
    }

  }

  /**
   * Finalize timings and print a status message
   */
  final def processCompletion(): Unit = {
    lazy val tag = s"SampleStoreLoader.processCompletion($parameters)"
    val metadata = snap.metadata
    val elapsedNanos = System.nanoTime - _startNanos

    val loadDurationMs = (elapsedNanos / 1e6).toLong
    metadata.state match {
      case FabricDataNoData =>
        metadata.generationMetrics.recordSliceEmptyColdLoad(loadDurationMs, snap.data.regionCount)
      case FabricDataWarm =>
        metadata.generationMetrics.recordSliceNormalColdLoad(
          loadDurationMs, snap.data.regionCount, _itemCount.longValue, _expectedItemCount.longValue,
          _potentialItemCount.longValue, _rejectedItemCount.longValue, _byteCount.longValue
        )
      case _ =>
    }
    log debug
      burstStdMsg(
        s"""$tag
           |  startEpoch=${_startEpoch} (${prettyDateTimeFromMillis(_startEpoch)})
           |  guid=${snap.guid}, suid=${_suids}
           |  state=${metadata.state}
           |  sliceKey=${metadata.sliceKey}
           |  clientHostname=$getPublicHostName
           |  loci=${slice.loci.map(_.hostName).mkString(", ")}
           |  ${metadata.generationMetrics.metricsString}
           |  inputWaitNanos=${_inputWaitNanos}  (${prettyTimeFromNanos(_inputWaitNanos.longValue)})
           |  inputStall=${prettyPercentage(_inputWaitNanos.longValue, elapsedNanos)} %
           |  outputWaitNanos=${_outputWaitNanos}  (${prettyTimeFromNanos(_outputWaitNanos.longValue)})
           |  outputStall=${prettyPercentage(_outputWaitNanos.longValue, elapsedNanos)} %
           |  failure=${metadata.failure}""".stripMargin
      )
  }

}
