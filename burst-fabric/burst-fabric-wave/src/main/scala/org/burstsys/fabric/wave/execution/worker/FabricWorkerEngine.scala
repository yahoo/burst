/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.worker

import org.burstsys.fabric.container.FabricWorkerService
import org.burstsys.fabric.wave.container.worker.FabricWaveWorkerContainer
import org.burstsys.fabric.wave.data
import org.burstsys.fabric.wave.data.model.snap.{FabricSnap, FailedSnap, HotSnap, NoDataSnap}
import org.burstsys.fabric.wave.execution.model.gather.FabricGather
import org.burstsys.fabric.wave.execution.model.gather.control.FabricFaultGather
import org.burstsys.fabric.wave.execution.model.gather.data.FabricDataGather
import org.burstsys.fabric.wave.execution.model.pipeline.publishPipelineEvent
import org.burstsys.fabric.wave.execution.model.wave.FabricParticle
import org.burstsys.fabric.wave.execution.{ParticleExecutionDataReady, ParticleExecutionFinished, ParticleExecutionStart}
import org.burstsys.fabric.wave.trek.{FabricWorkerFetchTrekMark, FabricWorkerScanInitTrekMark, FabricWorkerScanTrekMark}
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsStandardServer}
import org.burstsys.vitals.errors._
import org.burstsys.vitals.healthcheck.VitalsHealthMonitoredService
import org.burstsys.vitals.logging._
import org.burstsys.vitals.uid.VitalsUid

/**
 * Client (worker) side execution
 */
trait FabricWorkerEngine extends FabricWorkerService {

  /**
   * execute one worker's part of a wave operation
   */
  def executionParticleOp(ruid: VitalsUid, particle: FabricParticle): FabricGather

}

object FabricWorkerEngine {
  def apply(container: FabricWaveWorkerContainer, mode: VitalsServiceModality = VitalsStandardServer): FabricWorkerEngine =
    FabricWorkerContextEngine(container, mode: VitalsServiceModality)
}

private final case
class FabricWorkerContextEngine(container: FabricWaveWorkerContainer, modality: VitalsServiceModality)
  extends FabricWorkerEngine with VitalsHealthMonitoredService {

  override def serviceName: String = s"fabric-worker-engine"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def executionParticleOp(ruid: VitalsUid, particle: FabricParticle): FabricGather = {
    val slice = sliceFetch(particle)
    sliceScan(particle, slice)
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // INTERNAL
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * the data fetch part of the particle op
   */
  private def sliceFetch(particle: FabricParticle): FabricSnap = {
    val tag = s"FabricWorkerEngine.sliceFetch(guid=${particle.guid})"
    log info burstStdMsg(s"$tag start")
    val stage = FabricWorkerFetchTrekMark.beginSync(particle.slice.guid)
    try {
      val start = System.nanoTime
      publishPipelineEvent(ParticleExecutionStart(particle.slice.guid))

      val snap = data.worker.cache.instance.loadSnapWithReadLock(particle.slice)

      snap.state match {

        case HotSnap | NoDataSnap =>
          FabricEngineReporter.snapFetch(elapsedNs = System.nanoTime - start)
          FabricWorkerFetchTrekMark.end(stage)
          publishPipelineEvent(ParticleExecutionDataReady(particle.slice.guid))

        case FailedSnap => throw snap.lastFail.get

        case _ => throw VitalsException(s"unexpected state for fetched snap ${snap.state}")
      }

      snap
    } catch safely {
      case t: Throwable =>
        FabricWorkerFetchTrekMark.fail(stage, t)
        log error burstStdMsg(s"FAB_SLICE_FETCH_FAIL $t $tag", t)
        throw t
    } finally stage.closeScope()
  }

  /**
   * the data scan part of the particle op
   */
  private def sliceScan(particle: FabricParticle, snap: FabricSnap): FabricGather = {
    val tag = s"FabricWorkerEngine.sliceScan(snap=${snap.guid}, guid=${particle.guid})"
    log info s"FAB_SLICE_SCAN_START $tag"
    val start = System.nanoTime
    val stage = FabricWorkerScanTrekMark.beginSync(particle.slice.guid)
    val scanner = particle.scanner
    try {
      // things like compilation happen in beforeAllScans
      val initStage = FabricWorkerScanInitTrekMark.beginSync(particle.slice.guid)
      try {
        scanner.beforeAllScans(snap)
        FabricWorkerScanInitTrekMark.end(initStage)
      } catch safely {
        case t: Throwable =>
          FabricWorkerScanInitTrekMark.fail(initStage, t)
          throw t
      }

      scanner.scanMergeRegionsInSlice(snap.data.iterators) match {
        case gather: FabricFaultGather => throw gather.fault // throw to outer catch
        case gather: FabricDataGather =>
          log info s"FAB_SLICE_SCAN_SUCCESS $tag"
          FabricEngineReporter.successfulScan(elapsedNs = System.nanoTime - start, gather)
          FabricWorkerScanTrekMark.end(stage)
          publishPipelineEvent(ParticleExecutionFinished(particle.slice.guid))
          gather
      }
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"FAB_SLICE_SCAN_FAIL $t $tag", t)
        publishPipelineEvent(ParticleExecutionFinished(particle.slice.guid))
        FabricWorkerScanTrekMark.fail(stage, t)
        FabricEngineReporter.failedScan()
        FabricFaultGather(particle.scanner, t)
    } finally {
      stage.closeScope()
      scanner.afterAllScans(snap)
      snap.releaseSnapReadLock()
    }

  }


  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    synchronized {
      ensureNotRunning
      log info startingMessage
      markRunning
      this
    }
  }

  override
  def stop: this.type = {
    synchronized {
      ensureRunning
      log info stoppingMessage
      markNotRunning
      this
    }
  }

}
