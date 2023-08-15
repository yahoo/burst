/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.worker

import org.burstsys.fabric.wave.container.worker.FabricWaveWorkerContainer
import org.burstsys.fabric.wave.data.model.slice.state.FabricDataState
import org.burstsys.fabric.wave.data.model.snap.FabricSnap
import org.burstsys.fabric.wave.data.worker.store.FabricStoreWorker
import org.burstsys.fabric.wave.execution.model.pipeline.publishPipelineEvent
import org.burstsys.samplestore.SampleStoreName
import org.burstsys.samplestore.model.SampleStoreSlice
import org.burstsys.samplestore.trek.SampleStoreLoadTrekMark
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging.burstStdMsg

import scala.language.postfixOps

/**
  * the worker side of the sample store
  */
final case
class SampleStoreWorker(container: FabricWaveWorkerContainer) extends FabricStoreWorker {

  override lazy val storeName: String = SampleStoreName

  ///////////////////////////////////////////////////////////////////
  // LIFECYCLE
  ///////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    ensureNotRunning
    log info startingMessage
    markRunning
    this
  }

  override
  def stop: this.type = {
    ensureRunning
    log info stoppingMessage
    markNotRunning
    this
  }

  override protected def initializeSlice(snap: FabricSnap): FabricDataState = {
    lazy val hdr = s"SampleStoreInitializer.initializeSlice(guid=${snap.guid} slice=${snap.slice.guid})"
    log info burstStdMsg(s"$hdr")
    val slice = snap.slice.asInstanceOf[SampleStoreSlice]
    val guid = slice.guid

    val stage = SampleStoreLoadTrekMark.beginSync(guid)
    val state = try {
      // make sure metadata is in a clean state for the load
      snap.metadata.reset()

      val loader = SampleStoreLoader(snap, slice)
      loader.initializeLoader()
      stage.addEvent("Loader initialized")

      snap.data.openForWrites()
      stage.addEvent("Snap opened for writes")

      try {
        loader.acquireStreams()
        stage.addEvent("Streams acquired")
        publishPipelineEvent(ParticleStreamsAcquired(slice.guid))

        try {
          val state = loader.processStreamData()
          publishPipelineEvent(ParticleStreamsFinished(slice.guid))

          snap.data.waitForWritesToComplete()
          stage.addEvent("Writes completed")
          publishPipelineEvent(ParticleWritesFinished(slice.guid))

          state
        } finally loader.releaseStreams()

      } finally {
        snap.data.closeForWrites()
        stage.addEvent("Snap closed")

        loader.processCompletion()
        stage.addEvent("Loader completed")
      }

    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"$hdr exception", t)
        SampleStoreLoadTrekMark.fail(stage, t)
        throw t
    }
    SampleStoreLoadTrekMark.end(stage)
    state
  }


}
