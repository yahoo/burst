/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.supervisor

import io.opentelemetry.api.trace.Span
import org.burstsys.fabric.container.FabricSupervisorService
import org.burstsys.fabric.wave.container.supervisor.{FabricWaveSupervisorContainer, FabricWaveSupervisorListener}
import org.burstsys.fabric.wave.execution.model.gather.FabricGather
import org.burstsys.fabric.wave.execution.model.gather.control.FabricFaultGather
import org.burstsys.fabric.wave.execution.model.gather.data.FabricDataGather
import org.burstsys.fabric.wave.execution.model.wave.FabricWave
import org.burstsys.fabric.wave.execution.supervisor.wave._
import org.burstsys.fabric.wave.execution.supervisor.wave.request.FabricParticleRequest
import org.burstsys.tesla
import org.burstsys.tesla.scatter.slot._
import org.burstsys.tesla.scatter._
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.logging.{burstLocMsg, burstStdMsg}

import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
 * ==Wave Executions using the Tesla Scatter Framework ==
 * Waves are the basic unit of group analysis execution in Burst. They are a familiar scatter/gather model with
 * intelligent runtime decisions about which worker(s) to choose to each host a
 * data [[org.burstsys.fabric.wave.data.model.slice.FabricSlice]]
 * and execute a [[org.burstsys.fabric.wave.execution.model.scanner.FabricScanner]] against it.
 * <br/>
 * Each synchronous wave is a set of individual [[org.burstsys.tesla.scatter.TeslaScatterRequest]]
 * each responsible for a [[org.burstsys.fabric.wave.execution.model.wave.FabricParticle]] instances that goes out asynchronously,
 * each assigned to a [[org.burstsys.tesla.scatter.slot.TeslaScatterSlot]]. The wave execution then waits for all of those requests to either succeed,
 * fail, or timeout. [[org.burstsys.fabric.wave.execution.model.wave.FabricParticle]] failures or timeouts are handled in various ways including retries, scatter failure
 * promotion, or moving the [[org.burstsys.fabric.wave.execution.model.wave.FabricParticle]] to a different worker.
 * <hr/>
 * '''Note''' that each particle contains all the information, analysis and data specification, that is required to execute the request. The worker may
 * in fact cache both/either execution and data.
 */
trait FabricSupervisorExecution extends FabricSupervisorService {

  /**
   * execute a scatter/gather wave operation
   * All errors are thrown as a [[org.burstsys.fabric.wave.exception.FabricException]]
   */
  def dispatchExecutionWave(wave: FabricWave): Future[FabricGather]

}

object FabricSupervisorExecution {

  def apply(container: FabricWaveSupervisorContainer): FabricSupervisorExecution =
    FabricWaveSupervisorExecutionContext(container: FabricWaveSupervisorContainer)

}

private[fabric] final case
class FabricWaveSupervisorExecutionContext(container: FabricWaveSupervisorContainer) extends FabricSupervisorExecution
  with FabricWaveSupervisorListener with FabricWaveTalker {

  override def serviceName: String = s"fabric-supervisor-execution"

  override def modality: VitalsServiceModality = container.bootModality

  override def dispatchExecutionWave(wave: FabricWave): Future[FabricGather] = {
    val tag = s"FabricWaveScatter.executionWaveOp(wave=$wave, traceId=${Span.current.getSpanContext.getTraceId})"
    val start = System.nanoTime
    log info burstLocMsg(s"$tag dispatch execution wave")

    val scatter = tesla.scatter.pool.grabScatter(wave.guid)
    scatter.timeout = scatterTimeout // ensure that we give up on this wave after 5 minutes, even if we're >< this close
    TeslaRequestFuture {
      wave.particles foreach {
        particle =>
          container.topology.getWorker(particle.slice.worker) match {
            case Some(worker) =>
              scatter.addRequestSlot(FabricParticleRequest(container, worker, particle))
            case None =>
              throw VitalsException(s"WAVE_PART_SPAWN_FAIL ${particle.slice.worker} not found $tag ")
          }
      }
      log info burstLocMsg(s"$tag future wave")
      scatter.execute()
      processScatterEvents(scatter)
    } andThen {
      case Success(gather) =>
        FabricWaveReporter.successfulAnalysis(System.nanoTime - start, gather)
      case Failure(t) =>
        // pick the first, but merge all failures together
        if (scatter.failures.length > 1)
          scatter.failures.tail.filter(_.failure != null).foreach(slot => t.addSuppressed(slot.failure))
        FabricWaveReporter.failedWave(System.nanoTime - start, scatter.failures)
    } andThen { case _ =>
      tesla.scatter.pool releaseScatter scatter
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    ensureNotRunning
    log info startingMessage
    container talksTo this
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

  /**
   * ==Wave Executions using the Tesla Scatter Framework ==
   * Waves are the basic unit of group analysis execution in Burst. They are a familiar scatter/gather model with
   * intelligent runtime decisions about which worker(s) to choose to each host a
   * data [[org.burstsys.fabric.wave.data.model.slice.FabricSlice]]
   * and execute a [[org.burstsys.fabric.wave.execution.model.scanner.FabricScanner]] against it.
   * <br/><br/>
   * Each synchronous wave is a set of individual [[org.burstsys.tesla.scatter.TeslaScatterRequest]]
   * each responsible for a [[org.burstsys.fabric.wave.execution.model.wave.FabricParticle]] instances that goes
   * out  asynchronously, each assigned to a [[org.burstsys.tesla.scatter.slot.TeslaScatterSlot]].
   * The wave execution then waits for all of those requests to either succeed, fail, or timeout.
   * [[org.burstsys.fabric.wave.execution.model.wave.FabricParticle]] failures or timeouts are handled in various ways
   * including retries, scatter failure promotion, or moving the
   * [[org.burstsys.fabric.wave.execution.model.wave.FabricParticle]] to a different worker.
   */
  private def processScatterEvents(scatter: TeslaScatter): FabricGather = {
    val tag = s"FabricWaveLoop.scatterUpdateEventLoop(guid=${scatter.guid})"
    val seqNum = newWaveSeqNum // get a sequence number for those tracking/monitoring these waves...
    val waveMerge = WaveGatherMerge(scatter) // we could pool these I guess

    var gather: FabricGather = null

    while (gather == null) {
      val update = scatter.nextUpdate()
      log debug s"FAB_WAVE_LOOP_UPDATE ($update) $tag"

      update match {
        //////////////////////////////////////////////////////////////////////////////////////////////////
        // scatter level updates
        //////////////////////////////////////////////////////////////////////////////////////////////////
        case _: TeslaScatterBegin => onWaveBegin(seqNum, waveMerge.guid)

        case update: TeslaScatterCancel =>
          waveMerge.shutdownMerge()
          onWaveCancel(seqNum, waveMerge.guid, update.message) // currently there is no way to cancel a scatter

        case update: TeslaScatterTimeout =>
          waveMerge.shutdownMerge()
          onWaveTimeout(seqNum, scatter.guid, update.message)
          val msg = s"FAB_WAVE_SCATTER_TIMEOUT $update.message"
          log error burstStdMsg(s"FAB_WAVE_LOOP_FAIL $msg $tag")
          throw VitalsException(msg).fillInStackTrace()

        case _: TeslaScatterSucceed =>
          waveMerge.shutdownMerge() // tell merge pipeline no more coming
          gather = Await.result(waveMerge.mergedResult, mergeWait) // wait for background merging to complete
          gather match {
            case gather: FabricDataGather =>
              onWaveSucceed(seqNum, waveMerge.guid, gather)
            case gather: FabricFaultGather =>
              onWaveFail(seqNum, waveMerge.guid, gather.fault.getMessage)
          }

        // well that happened - make sure its clear what went wrong.
        // We should have a [[FabricException]] that came from either the worker side or the network.
        // Either way it has the a merged supervisor and if applicable worker side stack information merged together
        case update: TeslaScatterFail =>
          waveMerge.shutdownMerge()
          log error s"FAB_WAVE_SCATTER_FAIL ${update.throwable.getMessage} $tag"
          onWaveFail(seqNum, scatter.guid, update.throwable.getMessage)
          throw scatter.failures.head.failure

        //////////////////////////////////////////////////////////////////////////////////////////////////
        // slot level updates
        //////////////////////////////////////////////////////////////////////////////////////////////////

        case update: TeslaScatterSlotBegin =>
          update.slot.request match {
            case particle: FabricParticleRequest => onParticleBegin(seqNum, scatter.guid, update.slot.ruid, particle.worker.nodeMoniker)
            case _ =>
              val msg = s"FAB_WAVE_UNKNOWN_REQUEST request=${update.slot.request}"
              log error burstStdMsg(s"FAB_WAVE_LOOP_FAIL $msg $tag")
              throw VitalsException(msg).fillInStackTrace()
          }

        case update: TeslaScatterSlotProgress => onParticleProgress(seqNum, scatter.guid, update.slot.ruid, update.message)

        case update: TeslaScatterSlotSucceed => // most returns (including remote handled faults) should come in here...
          update.slot.request.result match {
            case gather: FabricGather =>
              waveMerge.mergeGather(gather)
              if (gather.succeeded)
                onParticleSucceed(seqNum, scatter.guid, update.slot.ruid)
              else
                onParticleFail(seqNum, scatter.guid, update.slot.ruid, gather.messages.mkString("; "))
          }


        case update: TeslaScatterSlotFail => // this is generally a network failure or unhandled remote fault...
          onParticleFail(seqNum, scatter.guid, update.slot.ruid, update.throwable.getLocalizedMessage)
          // its our choice here as to what to do - simple model is to force exception to scatter level...
          scatter.scatterFail(update.throwable)

        // received when a particle is considered late - generally this is recoverable through retry or redirect
        case update: TeslaScatterSlotTardy =>
          onParticleTardy(seqNum, scatter.guid, update.slot.ruid, update.message)

        case update: TeslaScatterSlotRetry =>
          onParticleRetry(seqNum, scatter.guid, update.slot.ruid, update.message)

        case update: TeslaScatterSlotCancel =>
          onParticleCancelled(seqNum, scatter.guid, update.slot.ruid, "Particle canceled")

        case updateOfUnknownType =>
          waveMerge.shutdownMerge()
          val msg = s"FAB_WAVE_UNKNOWN_UPDATE request=$updateOfUnknownType"
          log error burstStdMsg(s"FAB_WAVE_LOOP_FAIL $msg $tag")
          throw VitalsException(msg).fillInStackTrace()
      }
    }
    gather
  }
}

