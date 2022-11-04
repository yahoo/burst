/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.supervisor.wave

import org.burstsys.fabric.wave.execution.supervisor.wave.request.FabricParticleRequest
import org.burstsys.fabric.wave.execution.model.gather.FabricGather
import org.burstsys.fabric.wave.execution.model.gather.control.FabricFaultGather
import org.burstsys.fabric.wave.execution.model.gather.data.FabricDataGather
import org.burstsys.tesla.scatter.slot.{TeslaScatterSlotBegin, TeslaScatterSlotCancel, TeslaScatterSlotFail, TeslaScatterSlotProgress, TeslaScatterSlotRetry, TeslaScatterSlotSucceed, TeslaScatterSlotTardy}
import org.burstsys.tesla.scatter.{TeslaScatter, TeslaScatterBegin, TeslaScatterCancel, TeslaScatterFail, TeslaScatterSucceed, TeslaScatterTimeout}
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

import scala.concurrent.{Await, Future}
import scala.language.postfixOps

/**
 * ==Wave Executions using the Tesla Scatter Framework ==
 * Waves are the basic unit of group analysis execution in Burst. They are a familiar scatter/gather model with
 * intelligent runtime decisions about which worker(s) to choose to each host a
 * data [[org.burstsys.fabric.data.model.slice.FabricSlice]]
 * and execute a [[org.burstsys.fabric.execution.model.scanner.FabricScanner]] against it.
 * <br/><br/>
 * Each synchronous wave is a set of individual [[org.burstsys.tesla.scatter.TeslaScatterRequest]]
 * each responsible for a [[org.burstsys.fabric.execution.model.wave.FabricParticle]] instances that goes
 * out  asynchronously, each assigned to a [[org.burstsys.tesla.scatter.slot.TeslaScatterSlot]].
 * The wave execution then waits for all of those requests to either succeed, fail, or timeout.
 * [[org.burstsys.fabric.execution.model.wave.FabricParticle]] failures or timeouts are handled in various ways
 * including retries, scatter failure promotion, or moving the
 * [[org.burstsys.fabric.execution.model.wave.FabricParticle]] to a different worker.
 */
trait FabricWaveLoop extends Any with FabricWaveListener {

  /**
   * single threaded event/update loop handling. Each asynchronous update is handled here and all
   * runtime decisions about what to do in reaction are made.
   */
  final
  def scatterUpdateEventLoop(scatter: TeslaScatter): Future[FabricGather] = {
    val tag = s"FabricWaveLoop.scatterUpdateEventLoop(guid=${scatter.guid})"
    val seqNum = newWaveSeqNum // get a sequence number for those tracking/monitoring these waves...
    val waveState = FabricWaveState(scatter) // we could pool these I guess

    TeslaRequestFuture {
      var gather: FabricGather = null

      while (gather == null) {
        val update = scatter.nextUpdate()
        log debug s"FAB_WAVE_LOOP_UPDATE ($update) $tag"

        update match {
          //////////////////////////////////////////////////////////////////////////////////////////////////
          // scatter level updates
          //////////////////////////////////////////////////////////////////////////////////////////////////
          case _: TeslaScatterBegin => onWaveBegin(seqNum, waveState.guid)

          case update: TeslaScatterCancel =>
            waveState.shutdownMerge()
            onWaveCancel(seqNum, waveState.guid, update.message) // currently there is no way to cancel a scatter

          case update: TeslaScatterTimeout =>
            waveState.shutdownMerge()
            onWaveTimeout(seqNum, scatter.guid, update.message)
            val msg = s"FAB_WAVE_SCATTER_TIMEOUT $update.message"
            log error burstStdMsg(s"FAB_WAVE_LOOP_FAIL $msg $tag")
            throw VitalsException(msg).fillInStackTrace()

          case _: TeslaScatterSucceed =>
            waveState.shutdownMerge() // tell merge pipeline no more coming
            gather = Await.result(waveState.mergedResult, mergeWait) // wait for background merging to complete
            gather match {
              case gather: FabricDataGather =>
                onWaveSucceed(seqNum, waveState.guid, gather)
              case gather: FabricFaultGather =>
                onWaveFail(seqNum, waveState.guid, gather.fault.getMessage)
            }

          // well that happened - make sure its clear what went wrong.
          // We should have a [[FabricException]] that came from either the worker side or the network.
          // Either way it has the a merged supervisor and if applicable worker side stack information merged together
          case update: TeslaScatterFail =>
            waveState.shutdownMerge()
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
                waveState.mergeGather(gather)
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
            waveState.shutdownMerge()
            val msg = s"FAB_WAVE_UNKNOWN_UPDATE request=${updateOfUnknownType}"
            log error burstStdMsg(s"FAB_WAVE_LOOP_FAIL $msg $tag")
            throw VitalsException(msg).fillInStackTrace()
        }
      }
      gather
    }
  }

}
