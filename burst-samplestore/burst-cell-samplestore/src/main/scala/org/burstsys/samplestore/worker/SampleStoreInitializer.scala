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
      snap.metadata.reset() // make sure metadata is in a clean state for the load

      val loader = SampleStoreLoader(snap, slice)

      {
        val span = SampleStoreLoaderInitializeTrekMark.begin(guid)
        loader.initializeLoader()
        SampleStoreLoaderInitializeTrekMark.end(span)
      }
      {
        val span = SampleStoreLoaderOpenTrekMark.begin(guid)
        snap.data.openForWrites()
        SampleStoreLoaderOpenTrekMark.end(span)
      }
      try {
        // get nexus servers ready to feed data
        {
          val span = SampleStoreLoaderAcquireTrekMark.begin(guid)
          loader.acquireStreams()
          SampleStoreLoaderAcquireTrekMark.end(span)
        }
        publishPipelineEvent(ParticleStreamsAcquired(slice.guid))

        try {
          // fetch stream data, this call blocks until all stream data has been received
          val state = {
            val span = SampleStoreLoaderProcessStreamTrekMark.begin(guid)
            val state = loader.processStreamData()
            SampleStoreLoaderProcessStreamTrekMark.end(span)
            state
          }
          publishPipelineEvent(ParticleStreamsFinished(slice.guid))

          // wait for fabric cache to finish writes
          {
            val span = SampleStoreLoaderWaitForWritesTrekMark.begin(guid)
            snap.data.waitForWritesToComplete()
            SampleStoreLoaderWaitForWritesTrekMark.end(span)
          }
          publishPipelineEvent(ParticleWritesFinished(slice.guid))

          state
        } finally {
          // clean up nexus connections
          val span = SampleStoreLoaderReleaseStreamsTrekMark.begin(guid)
          loader.releaseStreams()
          SampleStoreLoaderReleaseStreamsTrekMark.end(span)
        }

      } finally {
        // clean up fabric cache writers
        {
          val span = SampleStoreLoaderCloseWritesTrekMark.begin(guid)
          snap.data.closeForWrites()
          SampleStoreLoaderCloseWritesTrekMark.end(span)
        }

        {
          val span = SampleStoreLoaderProcessCompletionTrekMark.begin(guid)
          loader.processCompletion()
          SampleStoreLoaderProcessCompletionTrekMark.end(span)
        }
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
}
