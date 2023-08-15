/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.supervisor.wave

import org.burstsys.fabric.wave.container.supervisor.FabricWaveSupervisorContainer
import org.burstsys.fabric.wave.execution.model.execute.group._
import org.burstsys.fabric.wave.execution.model.gather.control.FabricFaultGather
import org.burstsys.fabric.wave.execution.model.gather.data.{FabricDataGather, FabricEmptyGather}
import org.burstsys.fabric.wave.execution.model.result.group._
import org.burstsys.fabric.wave.execution.model.result.status.{FabricFaultResultStatus, FabricNoDataResultStatus}
import org.burstsys.fabric.wave.execution.model.scanner._
import org.burstsys.fabric.wave.execution.model.wave._
import org.burstsys.fabric.wave.metadata.model.over.FabricOver
import org.burstsys.fabric.wave.trek.FabricSupervisorWaveTrekMark
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.logging._
import org.burstsys.vitals.reporter.instrument._

import scala.annotation.unused
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

/**
 * support for supervisor side group scan execution
 */
trait FabricWaveExecute extends Any {

  def container: FabricWaveSupervisorContainer

  /**
   * suck internal results into final form
   */
  protected
  def extractResults(gather: FabricDataGather): FabricResultGroup

  /**
   * execute/cleanup -
   */
  final private[execution]
  def waveExecute(
                   group: FabricGroupKey,
                   scanner: FabricScanner,
                   @unused duration: Duration,
                   over: FabricOver
                 ): Future[FabricResultGroup] = {
    lazy val tag = s"FabricWaveExecute.waveExecute($group, $over)"

    val start = System.nanoTime
    FabricSupervisorWaveTrekMark.begin(group.groupUid) { stage =>
      container.data.slices(group.groupUid, scanner.datasource) map { slices =>
        FabricWave(group.groupUid, slices.map(FabricParticle(group.groupUid, _, scanner)))
      } chainWithFuture { wave =>
        container.execution.executionWaveOp(wave)
      } andThen {
        case Success(_) =>
          val elapsedNanos = System.nanoTime - start
          log info s"WAVE_EXECUTE_SUCCESS elapsedNs=elapsedNanos (${prettyTimeFromNanos(elapsedNanos)}) $tag"
          FabricSupervisorWaveTrekMark.end(stage)
        case Failure(t) =>
          FabricSupervisorWaveTrekMark.fail(stage, t)
          log error burstStdMsg(s"WAVE_EXECUTE_FAIL $t $tag", t)
      } map {
        case gather: FabricFaultGather =>
          FabricResultGroup(group, FabricFaultResultStatus, gather.resultMessage, FabricResultGroupMetrics(gather))

        case gather: FabricEmptyGather =>
          FabricResultGroup(group, FabricNoDataResultStatus, "NO_DATA", FabricResultGroupMetrics(gather))

        case gather: FabricDataGather =>
          extractResults(gather)
      } andThen { case Success(results) =>
        results.releaseResourcesOnSupervisor() // this is a no-op for a generic FabricResultGroup
      }

    }
  }

}
