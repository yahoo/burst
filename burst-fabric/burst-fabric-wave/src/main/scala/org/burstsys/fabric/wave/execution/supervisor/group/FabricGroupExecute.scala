/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.supervisor.group

import org.burstsys.fabric.container.FabricSupervisorService
import org.burstsys.fabric.wave.container.supervisor.FabricWaveSupervisorContainer
import org.burstsys.fabric.wave.data.model.generation.metrics.FabricGenerationMetrics
import org.burstsys.fabric.wave.execution.model.execute.group.FabricGroupKey
import org.burstsys.fabric.wave.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.wave.execution.model.gather.control.FabricFaultGather
import org.burstsys.fabric.wave.execution.model.gather.data.{FabricDataGather, FabricEmptyGather}
import org.burstsys.fabric.wave.execution.model.result.group.{FabricResultGroup, FabricResultGroupMetrics}
import org.burstsys.fabric.wave.execution.model.result.status.{FabricFaultResultStatus, FabricNoDataResultStatus}
import org.burstsys.fabric.wave.execution.model.scanner.FabricPlaneScanner
import org.burstsys.fabric.wave.execution.model.wave.{FabricParticle, FabricWave}
import org.burstsys.fabric.wave.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.wave.metadata.model.over.FabricOver
import org.burstsys.fabric.wave.trek.FabricSupervisorWaveTrekMark
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.logging.burstStdMsg
import org.burstsys.vitals.reporter.instrument._

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * support for group scan execution
 */
trait FabricGroupExecutor extends Any {

  /**
   *
   * @param name
   * @param datasource
   * @param groupKey
   * @param over
   * @param scanner
   * @param call
   * @param executionStart
   * @return
   */
  def doWaveExecute(name: String, datasource: FabricDatasource, groupKey: FabricGroupKey, over: FabricOver,
                    scanner: FabricPlaneScanner, call: Option[FabricCall], executionStart: Long): Future[FabricResultGroup]

}

abstract class FabricGroupExecuteContext(container: FabricWaveSupervisorContainer) extends FabricSupervisorService with FabricGroupExecutor {

  /**
   * suck internal results into final form
   */
  protected def extractResults(gather: FabricDataGather): FabricResultGroup

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def doWaveExecute(name: String, datasource: FabricDatasource, groupKey: FabricGroupKey, over: FabricOver,
                    scanner: FabricPlaneScanner, call: Option[FabricCall], executionStart: Long): Future[FabricResultGroup] = {
    lazy val tag = s"FabricWaveExecute.waveExecute(${scanner.group}, $over)"

    val start = System.nanoTime
    FabricSupervisorWaveTrekMark.begin(scanner.group.groupUid) { stage =>
      container.data.slices(scanner.group.groupUid, scanner.datasource) map { slices =>
        FabricWave(scanner.group.groupUid, slices.map(FabricParticle(scanner.group.groupUid, _, scanner)))
      } chainWithFuture {
        container.execution.dispatchExecutionWave
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
          FabricResultGroup(scanner.group, FabricFaultResultStatus, gather.resultMessage, FabricResultGroupMetrics(gather))

        case gather: FabricEmptyGather =>
          FabricResultGroup(scanner.group, FabricNoDataResultStatus, "NO_DATA", FabricResultGroupMetrics(gather))

        case gather: FabricDataGather =>
          extractResults(gather)
      } andThen { case Success(results) =>
        results.releaseResourcesOnSupervisor() // this is a no-op for a generic FabricResultGroup
      } andThen { case scan =>
        val (status, scanTime, rowCount, items, potentialItems, error) = scan match {
          case Failure(t) => ("failure", -1L, -1L, -1L, -1L, t)
          case Success(results) =>
            updateViewMetrics(scanner.datasource, results.groupMetrics.generationMetrics)
            val execution = results.groupMetrics.executionMetrics
            val generation = results.groupMetrics.generationMetrics
            (results.resultMessage, execution.scanTime, execution.rowCount, generation.itemCount, generation.potentialItemCount, null)
        }
        printResult(name, status, scanner.datasource, scanner.group, System.nanoTime - executionStart, scanTime, rowCount, items, potentialItems, error)
      }
    }
  }

  /**
   * make sure the most recent dataset generation metrics are recorded in the view
   *
   * @param datasource the datasource to update
   * @param generationMetrics the metrics for the most recent load
   */
  private def updateViewMetrics(datasource: FabricDatasource, generationMetrics: FabricGenerationMetrics): Unit = {
    // make sure we update the view
    val updatedProperties = datasource.postWaveMetricsUpdate(generationMetrics)
    container.metadata.lookup.recordViewLoad(datasource, updatedProperties) // update the view properties and result group based on the load
  }

  private def printResult(
                           header: String, status: String, datasource: FabricDatasource, groupKey: FabricGroupKey,
                           elapsedNanos: Long, scanTime: Long = -1, rowCount: Long = -1, items: Long = -1, potentialItems: Long = -1,
                           error: Throwable = null
                         ): Unit = {
    log info(
      s"""$header status=$status guid=${groupKey.groupUid} view=${datasource.view}
         |  elapsedNanos=$elapsedNanos (${prettyTimeFromNanos(elapsedNanos)}), scanTime=$scanTime (${prettyTimeFromNanos(scanTime)}), rowCount=$rowCount  (${prettyFixedNumber(rowCount)})
         |  itemCount=$items (${prettyFixedNumber(items)}) potentialItems=$potentialItems (${prettyFixedNumber(potentialItems)})""".stripMargin, error)

  }

}
