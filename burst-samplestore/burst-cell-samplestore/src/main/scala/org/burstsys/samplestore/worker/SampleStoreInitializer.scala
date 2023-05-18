/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.worker

import org.burstsys.fabric.wave.data.model.slice.state.FabricDataState
import org.burstsys.fabric.wave.data.model.snap.FabricSnap
import org.burstsys.fabric.wave.data.worker.store.FabricWorkerLoader
import org.burstsys.fabric.wave.execution.model.pipeline.publishPipelineEvent
import org.burstsys.samplestore.model.SampleStoreSlice
import org.burstsys.samplestore.trek._
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._
import org.burstsys.vitals.uid.VitalsUid

import scala.language.postfixOps

/**
 * Sample Store Worker Side data management
 */
trait SampleStoreInitializer extends FabricWorkerLoader {

  final override protected
  def initializeSlice(snap: FabricSnap): FabricDataState = {
    lazy val hdr = s"SampleStoreInitializer.initializeSlice(guid=${snap.guid} slice=${snap.slice.guid})"
    log info burstStdMsg(s"$hdr")
    val slice = snap.slice.asInstanceOf[SampleStoreSlice]
    val guid = slice.guid

    val sslSpan = SampleStoreLoadTrekMark.begin(guid)
    val state = try {
      // make sure metadata is in a clean state for the load
      snap.metadata.reset()

      val loader = SampleStoreLoader(snap, slice)

      initializeLoader(guid, loader)

      openSnapForWrites(guid, snap)

      try {
        acquireStreams(guid, loader)
        publishPipelineEvent(ParticleStreamsAcquired(slice.guid))

        try {
          val state = processStreamData(guid, loader)
          publishPipelineEvent(ParticleStreamsFinished(slice.guid))

          waitForWritesToComplete(guid, snap)
          publishPipelineEvent(ParticleWritesFinished(slice.guid))

          state
        } finally releaseStreams(guid, loader)

      } finally {
        closeSnapForWrites(guid, snap)
        processLoadCompletion(guid, loader)
      }

    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"$hdr exception", t)
        SampleStoreLoadTrekMark.fail(sslSpan)
        throw t
    }
    SampleStoreLoadTrekMark.end(sslSpan)
    state
  }

  private def initializeLoader(guid: VitalsUid, loader: SampleStoreLoader): Unit = {
    val span = SampleStoreLoaderInitializeTrekMark.begin(guid)
    loader.initializeLoader()
    SampleStoreLoaderInitializeTrekMark.end(span)
  }

  private def openSnapForWrites(guid: VitalsUid, snap: FabricSnap): Unit = {
    val span = SampleStoreLoaderOpenTrekMark.begin(guid)
    snap.data.openForWrites()
    SampleStoreLoaderOpenTrekMark.end(span)
  }

  // get nexus servers ready to feed data
  private def acquireStreams(guid: VitalsUid, loader: SampleStoreLoader): Unit = {
    val span = SampleStoreLoaderAcquireTrekMark.begin(guid)
    loader.acquireStreams()
    SampleStoreLoaderAcquireTrekMark.end(span)
  }

  // fetch stream data, this call blocks until all stream data has been received
  private def processStreamData(guid: VitalsUid, loader: SampleStoreLoader): FabricDataState = {
    val span = SampleStoreLoaderProcessStreamTrekMark.begin(guid)
    val state = loader.processStreamData()
    SampleStoreLoaderProcessStreamTrekMark.end(span)
    state
  }

  // wait for fabric cache to finish writes
  private def waitForWritesToComplete(guid: VitalsUid, snap: FabricSnap): Unit = {
    val span = SampleStoreLoaderWaitForWritesTrekMark.begin(guid)
    snap.data.waitForWritesToComplete()
    SampleStoreLoaderWaitForWritesTrekMark.end(span)
  }

  // clean up nexus connections
  private def releaseStreams(guid: VitalsUid, loader: SampleStoreLoader): Unit = {
    val span = SampleStoreLoaderReleaseStreamsTrekMark.begin(guid)
    loader.releaseStreams()
    SampleStoreLoaderReleaseStreamsTrekMark.end(span)
  }

  // clean up fabric cache writers
  private def closeSnapForWrites(guid: VitalsUid, snap: FabricSnap): Unit = {
    val span = SampleStoreLoaderCloseWritesTrekMark.begin(guid)
    snap.data.closeForWrites()
    SampleStoreLoaderCloseWritesTrekMark.end(span)
  }

  private def processLoadCompletion(guid: VitalsUid, loader: SampleStoreLoader): Unit = {
    val span = SampleStoreLoaderProcessCompletionTrekMark.begin(guid)
    loader.processCompletion()
    SampleStoreLoaderProcessCompletionTrekMark.end(span)
  }
}
