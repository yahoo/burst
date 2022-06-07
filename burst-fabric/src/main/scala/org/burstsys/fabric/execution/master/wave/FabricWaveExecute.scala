/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.master.wave

import org.burstsys.fabric.container.master.FabricMasterContainer
import org.burstsys.fabric.execution.model.execute.group._
import org.burstsys.fabric.execution.model.gather.control.FabricFaultGather
import org.burstsys.fabric.execution.model.gather.data.{FabricDataGather, FabricEmptyGather}
import org.burstsys.fabric.execution.model.result.group._
import org.burstsys.fabric.execution.model.result.status.{FabricFaultResultStatus, FabricNoDataResultStatus}
import org.burstsys.fabric.execution.model.scanner._
import org.burstsys.fabric.execution.model.wave._
import org.burstsys.fabric.metadata.model.over.FabricOver
import org.burstsys.fabric.trek.FabricMasterWaveTrekMark
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.instrument._
import org.burstsys.vitals.logging._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

/**
 * support for master side group scan execution
 */
trait FabricWaveExecute extends Any {

  def container: FabricMasterContainer

  /**
   * suck internal results into final form
   *
   * @param gather
   * @return
   */
  protected
  def extractResults(gather: FabricDataGather): FabricResultGroup

  /**
   * execute/cleanup -
   *
   * @param group
   * @param scanner
   * @param duration
   * @param over
   * @return
   */
  final private[execution]
  def waveExecute(
                   group: FabricGroupKey,
                   scanner: FabricScanner,
                   duration: Duration,
                   over: FabricOver
                 ): Future[FabricResultGroup] = {
    lazy val tag = s"FabricWaveExecute.waveExecute($group, $over)"

    val start = System.nanoTime
    FabricMasterWaveTrekMark.begin(group.groupUid)
    container.data.slices(group.groupUid, scanner.datasource) map { slices =>
      FabricWave(group.groupUid, slices.map(FabricParticle(group.groupUid, _, scanner)))
    } chainWithFuture { wave =>
      container.execution.executionWaveOp(wave)
    } andThen {
      case Success(_) =>
        val elapsedNanos = System.nanoTime - start
        log info s"WAVE_EXECUTE_SUCCESS elapsedNs=elapsedNanos (${prettyTimeFromNanos(elapsedNanos)}) $tag"
        FabricMasterWaveTrekMark.end(group.groupUid)
      case Failure(t) =>
        FabricMasterWaveTrekMark.fail(group.groupUid)
        log error burstStdMsg(s"WAVE_EXECUTE_FAIL $t $tag", t)
    } map {
      case gather: FabricFaultGather =>
        FabricResultGroup(group, FabricFaultResultStatus, gather.resultMessage, FabricResultGroupMetrics(gather))

      case gather: FabricEmptyGather =>
        FabricResultGroup(group, FabricNoDataResultStatus, "NO_DATA", FabricResultGroupMetrics(gather))

      case gather: FabricDataGather =>
        extractResults(gather)
    } andThen { case Success(results) =>
      results.releaseResourcesOnMaster() // this is a no-op for a generic FabricResultGroup
    }
  }

}
