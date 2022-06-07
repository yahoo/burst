/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.result

import org.burstsys.brio.types.BrioTypes._
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.fabric.execution.model.result.set.FabricResultSet
import org.burstsys.fabric.execution.model.result.set.FabricResultSetIndex
import org.burstsys.fabric.execution.model.result.set.FabricResultSetMetrics
import org.burstsys.fabric.execution.model.result.set.FabricResultSetName
import org.burstsys.fabric.execution.model.result.state.FabricScanState
import org.burstsys.felt.model.collectors.runtime.FeltCollector
import org.burstsys.felt.model.collectors.runtime.FeltCollectorBuilder
import org.burstsys.felt.model.collectors.runtime.FeltCollectorPlane
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._
import org.burstsys.vitals.text.VitalsTextCodec

import scala.reflect.ClassTag

/**
 * zero or more result rows...
 */
trait FeltCollectorResultSet extends FabricResultSet {

  /**
   * what was final state of this scan
   *
   * @return
   */
  def scanState: FabricScanState

  /**
   * was the row limit exceeded?
   *
   * @return
   */
  def rowLimitExceeded: Boolean

  /**
   * Did the dictionary overflow?
   *
   * @return
   */
  def dictionaryOverflow: Boolean

  /**
   * here is where we extract all data and metrics from underlying dynamic structures.
   * Once this is called and data/metrics retrieved, then the underlying data
   * structures can be released.
   *
   * @return
   */
  def extractRows: FabricResultSet

}

abstract
class FeltCollectorResultSetContext[B <: FeltCollectorBuilder, C <: FeltCollector]
(resultName: FabricResultSetName, plane: FeltCollectorPlane[B, C])
  extends FeltCollectorResultSet {

  override def toString: FabricResultSetName = s"FeltCollectorResultSet(resultName=$resultName)"

  //////////////////////////////////////////////////////////////////////
  // state
  //////////////////////////////////////////////////////////////////////

  private[this]
  var _metrics: FabricResultSetMetrics = _

  protected
  implicit val text: VitalsTextCodec = VitalsTextCodec()

  //////////////////////////////////////////////////////////////////////
  // Accessors
  //////////////////////////////////////////////////////////////////////

  final override
  def metrics: FabricResultSetMetrics = _metrics

  final override
  def resultIndex: FabricResultSetIndex = plane.planeId

  final override
  val scanState: FabricScanState = plane.scanState

  final override
  val rowLimitExceeded: Boolean = plane.rowLimitExceeded

  final override
  val dictionaryOverflow: Boolean = plane.dictionaryOverflow

  override
  val columnNames: Array[BrioRelationName] = Array.empty

  override
  def columnTypeNames: Array[BrioTypeName] = Array.empty

  override
  def columnTypeKeys: Array[BrioTypeKey] = Array.empty

  final override
  val rowCount: Int = plane.planeCollector.rowCount

  override
  val dimensionCount: Int = 0

  override
  val aggregationCount: Int = 0

  override
  def rowSet: Array[FabricResultRow] = Array.empty

  //////////////////////////////////////////////////////////////////////
  // data access
  //////////////////////////////////////////////////////////////////////

  override
  def apply[T <: BrioDataType : ClassTag](column: String): Array[T] = Array.empty

  //////////////////////////////////////////////////////////////////////
  // JSON
  //////////////////////////////////////////////////////////////////////

  final override
  def toJson: FabricResultSet = FabricResultSet(
    resultIndex,
    resultName,
    metrics.toJson,
    dimensionCount,
    aggregationCount,
    columnNames,
    columnTypeNames,
    columnTypeKeys,
    rowCount,
    rowSet.map(_.toJson)
  )

  //////////////////////////////////////////////////////////////////////
  // extraction
  //////////////////////////////////////////////////////////////////////

  override
  def extractRows: FabricResultSet = {
    try {
      _metrics = FabricResultSetMetrics(
        succeeded = plane.resultStatus.isSuccess,
        rowCount = plane.rowCount,
        limited = plane.rowLimitExceeded,
        overflowed = plane.dictionaryOverflow,
        properties = Map.empty
      )
      this
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }


}
