/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.supervisor.wave

import org.burstsys.fabric.wave.exception.FabricException
import org.burstsys.fabric.wave.execution.supervisor.FabricWaveSupervisorExecutionContext
import org.burstsys.fabric.wave.execution.supervisor.wave.request.FabricParticleRequest
import org.burstsys.fabric.wave.execution.model.gather.FabricGather
import org.burstsys.fabric.wave.execution.model.wave.FabricWave
import org.burstsys.tesla
import org.burstsys.tesla.scatter.TeslaScatter
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.errors._

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

/**
 * ==Wave Executions using the Tesla Scatter Framework ==
 * Waves are the basic unit of group analysis execution in Burst. They are a familiar scatter/gather model with
 * intelligent runtime decisions about which worker(s) to choose to each host a
 * data [[org.burstsys.fabric.data.model.slice.FabricSlice]]
 * and execute a [[org.burstsys.fabric.execution.model.scanner.FabricScanner]] against it.
 * <br/><br/>
 * Each synchronous wave is a set of individual [[org.burstsys.tesla.scatter.TeslaScatterRequest]]
 * each responsible for a [[org.burstsys.fabric.execution.model.wave.FabricParticle]] instances that goes out  asynchronously,
 * each assigned to a [[org.burstsys.tesla.scatter.slot.TeslaScatterSlot]]. The wave execution then waits for all of those requests to either succeed,
 * fail, or timeout. [[org.burstsys.fabric.execution.model.wave.FabricParticle]] failures or timeouts are handled in various ways including retries, scatter failure
 * promotion, or moving the [[org.burstsys.fabric.execution.model.wave.FabricParticle]] to a different worker.
 * <hr/>
 * '''Note''' that each particle contains all the information, analysis and data specification, that is required to execute the request. The worker may
 * in fact cache both/either execution and data.
 */
trait FabricWaveScatter extends Any {

  self: FabricWaveSupervisorExecutionContext =>

  final override
  def executionWaveOp(wave: FabricWave): Future[FabricGather] = {
    val tag = s"FabricWaveScatter.executionWaveOp($wave)"
    val start = System.nanoTime
    val promise = Promise[FabricGather]()

    def FAIL(t: Throwable): Unit = {
      t match {
        // make sure remote stack is stitched in
        case fe: FabricException => promise.failure(fe.stitch)
        case t: Throwable => promise.failure(t)
      }
    }

    val scatter = tesla.scatter.pool.grabScatter(wave.guid)
    // ensure that we give up on this wave after 5 minutes, even if we're >< this close
    scatter.timeout = scatterTimeout
    Try(slotParticles(scatter, wave, tag)) match {
      case Failure(t) =>
        tesla.scatter.pool releaseScatter scatter
        FAIL(t)
      case Success(()) =>
        scatter.execute()
        // poll for events on a singled threaded update loop
        scatterUpdateEventLoop(scatter) onComplete {
          case Success(gather) =>
            FabricWaveReporter.successfulAnalysis(System.nanoTime - start, gather)
            tesla.scatter.pool releaseScatter scatter
            promise.success(gather)
          case Failure(t) =>
            // pick the first, but merge all failures together
            if (scatter.failures.length > 1)
              scatter.failures.tail.filter(_.failure != null).foreach(slot => t.addSuppressed(slot.failure))
            FabricWaveReporter.failedWave(System.nanoTime - start, scatter.failures)
            tesla.scatter.pool releaseScatter scatter
            FAIL(t)
        }

    }
    promise.future
  }

  /**
   * spawn all the wave particles
   *
   * @param scatter
   * @param wave
   * @param tag
   * @return
   */
  private def slotParticles(scatter: TeslaScatter, wave: FabricWave, tag: String): Unit = {
    wave.particles foreach {
      particle =>
        container.topology.getWorker(particle.slice.worker) match {
          case Some(worker) =>
            scatter.addRequestSlot(FabricParticleRequest(container, worker, particle))
          case None =>
            throw VitalsException(s"WAVE_PART_SPAWN_FAIL ${particle.slice.worker} not found $tag ")
        }
    }
  }
}
