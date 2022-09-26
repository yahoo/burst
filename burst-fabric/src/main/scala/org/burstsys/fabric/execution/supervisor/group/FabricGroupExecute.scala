/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.supervisor.group

import org.burstsys.fabric.container.FabricSupervisorService
import org.burstsys.fabric.data.model.generation.metrics.FabricGenerationMetrics
import org.burstsys.fabric.execution.supervisor.wave.FabricWaveExecute
import org.burstsys.fabric.execution.model.execute.group.FabricGroupKey
import org.burstsys.fabric.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.scanner.FabricPlaneScanner
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.metadata.model.over.FabricOver
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.instrument._

import scala.concurrent.duration.Duration
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

/**
 * support for group scan execution
 *
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

abstract
class FabricGroupExecuteContext extends FabricSupervisorService with FabricGroupExecutor with FabricWaveExecute {

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def doWaveExecute(name: String, datasource: FabricDatasource, groupKey: FabricGroupKey, over: FabricOver,
                    scanner: FabricPlaneScanner, call: Option[FabricCall], executionStart: Long): Future[FabricResultGroup] = {
    waveExecute(scanner.group, scanner, duration = Duration.Inf, over) andThen { case scan =>
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
