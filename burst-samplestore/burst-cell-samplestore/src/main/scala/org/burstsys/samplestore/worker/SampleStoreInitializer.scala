/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.worker

import org.burstsys.fabric.data.model.slice.state.FabricDataState
import org.burstsys.fabric.data.model.snap.FabricSnap
import org.burstsys.fabric.data.worker.store.FabricWorkerLoader
import org.burstsys.fabric.execution.model.pipeline.publishPipelineEvent
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

    SampleStoreLoadTrekMark.begin(guid)
    val state = try {
      snap.metadata.reset() // make sure metadata is in a clean state for the load

      val loader = SampleStoreLoader(snap, slice)

      SampleStoreLoaderInitializeTrekMark.begin(guid)
      loader.initializeLoader()
      SampleStoreLoaderInitializeTrekMark.end(guid)

      SampleStoreLoaderOpenTrekMark.begin(guid)
      snap.data.openForWrites()
      SampleStoreLoaderOpenTrekMark.end(guid)
      try {
        // get nexus servers ready to feed data
        SampleStoreLoaderAcquireTrekMark.begin(guid)
        loader.acquireStreams()
        SampleStoreLoaderAcquireTrekMark.end(guid)
        publishPipelineEvent(ParticleStreamsAcquired(slice.guid))

        try {
          // fetch stream data, this call blocks until all stream data has been received
          SampleStoreLoaderProcessStreamTrekMark.begin(guid)
          val state = loader.processStreamData()
          SampleStoreLoaderProcessStreamTrekMark.end(guid)
          publishPipelineEvent(ParticleStreamsFinished(slice.guid))

          // wait for fabric cache to finish writes
          SampleStoreLoaderWaitForWritesTrekMark.begin(guid)
          snap.data.waitForWritesToComplete()
          SampleStoreLoaderWaitForWritesTrekMark.end(guid)
          publishPipelineEvent(ParticleWritesFinished(slice.guid))

          state
        } finally {
          // clean up nexus connections
          SampleStoreLoaderReleaseStreamsTrekMark.begin(guid)
          loader.releaseStreams()
          SampleStoreLoaderReleaseStreamsTrekMark.end(guid)
        }

      } finally {
        // clean up fabric cache writers
        SampleStoreLoaderCloseWritesTrekMark.begin(guid)
        snap.data.closeForWrites()
        SampleStoreLoaderCloseWritesTrekMark.end(guid)

        SampleStoreLoaderProcessCompletionTrekMark.begin(guid)
        loader.processCompletion()
        SampleStoreLoaderProcessCompletionTrekMark.end(guid)
      }

    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"$hdr exception", t)
        SampleStoreLoadTrekMark.fail(guid)
        throw t
    }
    SampleStoreLoadTrekMark.end(guid)
    state
  }

}
