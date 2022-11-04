/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.result

import org.burstsys.fabric.wave.execution.model.execute.group.FabricGroupKey
import org.burstsys.fabric.wave.execution.model.gather.data.FabricDataGather
import org.burstsys.fabric.wave.execution.model.result.group.{FabricResultGroup, FabricResultGroupMetrics}
import org.burstsys.fabric.wave.execution.model.result.set.{FabricResultSet, FabricResultSetIndex}
import org.burstsys.fabric.wave.execution.model.result.status.FabricResultStatus
import org.burstsys.felt.model.collectors.cube.plane.FeltCubePlane
import org.burstsys.felt.model.collectors.cube.result.FeltCubeResultSet
import org.burstsys.felt.model.collectors.route.plane.FeltRoutePlane
import org.burstsys.felt.model.collectors.route.result.FeltRouteResultSet
import org.burstsys.felt.model.collectors.runtime.FeltCollectorGather
import org.burstsys.felt.model.collectors.shrub.plane.FeltShrubPlane
import org.burstsys.felt.model.collectors.shrub.result.FeltShrubResultSet
import org.burstsys.felt.model.collectors.tablet.plane.FeltTabletPlane
import org.burstsys.felt.model.collectors.tablet.result.FeltTabletResultSet
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.logging._

import scala.collection.mutable

trait FeltCollectorResultGroup extends FabricResultGroup {

  /**
   * the internal gather
   *
   * @return
   */
  def gather: FeltCollectorGather

}

/**
 * constructors
 */
object FeltCollectorResultGroup {

  def apply(
             groupKey: FabricGroupKey,
             gather: FeltCollectorGather
           ): FeltCollectorResultGroup =
    FeltCollectorResultGroupContext(
      groupKey: FabricGroupKey,
      gather: FeltCollectorGather
    )

}

private final case
class FeltCollectorResultGroupContext(
                                       groupKey: FabricGroupKey,
                                       gather: FeltCollectorGather
                                     ) extends FeltCollectorResultGroup {

  //////////////////////////////////////////////////////////////////////
  // state
  //////////////////////////////////////////////////////////////////////

  private[this] final
  val _resultSets = new mutable.HashMap[FabricResultSetIndex, FabricResultSet]

  private[this]
  var _groupMetrics: FabricResultGroupMetrics = _

  //////////////////////////////////////////////////////////////////////
  // JSON
  //////////////////////////////////////////////////////////////////////

  override def toJson: FabricResultGroup = new FabricResultGroup {
    override val groupKey: FabricGroupKey = FeltCollectorResultGroupContext.this.groupKey.toJson
    override val groupMetrics: FabricResultGroupMetrics = FeltCollectorResultGroupContext.this.groupMetrics.toJson
    override val rowCount: Int = FeltCollectorResultGroupContext.this.rowCount
    override val resultSets: Map[FabricResultSetIndex, FabricResultSet] = FeltCollectorResultGroupContext.this.resultSets.map {
      case (k, v) => k -> v.toJson
    }
    override val resultStatus: FabricResultStatus = FeltCollectorResultGroupContext.this.resultStatus
    override val resultMessage: String = FeltCollectorResultGroupContext.this.resultMessage
  }

  //////////////////////////////////////////////////////////////////////
  // api
  //////////////////////////////////////////////////////////////////////


  def resultStatus: FabricResultStatus = gather.resultStatus

  override
  def resultMessage: String = gather.resultMessage

  override
  def groupMetrics: FabricResultGroupMetrics = _groupMetrics

  override
  def resultSets: Map[FabricResultSetIndex, FabricResultSet] = _resultSets.toMap

  override
  def rowCount: Int = gather.totalRows

  override
  def releaseResourcesOnSupervisor(): Unit = {
    gather.releaseResourcesOnSupervisor()
  }

  override
  def releaseResourcesOnWorker(): Unit =
    throw VitalsException(
      s"FeltCollectorResultGroup.releaseResourcesOnWorker() can't do this on worker (supervisor?)!"
    )

  override
  def extractResults: FabricResultGroup = {
    try {
      var i = 0
      while (i < gather.activePlanes) {
        gather.planes(i) match {
          case p: FeltCubePlane =>
            _resultSets += p.planeId -> FeltCubeResultSet(p.planeName, p).extractRows
          case p: FeltRoutePlane =>
            _resultSets += p.planeId -> FeltRouteResultSet(p.planeName, p).extractRows
          case p: FeltShrubPlane =>
            _resultSets += p.planeId -> FeltShrubResultSet(p.planeName, p).extractRows
          case p: FeltTabletPlane =>
            _resultSets += p.planeId -> FeltTabletResultSet(p.planeName, p).extractRows
          case _ =>
            ???
        }
        i += 1
      }
      gather match {
        case g: FabricDataGather =>
          gather.gatherMetrics.executionMetrics.recordFinalMetricsOnSupervisor(g)
        case _ =>
      }
      _groupMetrics = FabricResultGroupMetrics(gather)
      this
    } catch safely {
      case t: Throwable =>
        val msg = burstStdMsg(
          s"FeltCollectorResultGroup.extractResults extraction of result(s) gather=$gather", t
        )
        log error msg
        throw new RuntimeException(msg, t)
    }
  }

}
