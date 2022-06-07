/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.result

import org.burstsys.brio.types.BrioTypes
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.fabric.execution.model.result.row.{FabricResultRow, FeltCubeRowData}
import org.burstsys.fabric.execution.model.result.set.FabricResultSetName
import org.burstsys.felt.model.collectors.cube.plane.FeltCubePlane
import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubeCollector}
import org.burstsys.felt.model.collectors.result.{FeltCollectorResultSet, FeltCollectorResultSetContext}
import org.burstsys.vitals.errors.{VitalsException, _}

import scala.collection.mutable
import scala.reflect.ClassTag

/**
 * zero or more result rows...
 */
trait FeltCubeResultSet extends FeltCollectorResultSet

/**
 * constructors
 */
object FeltCubeResultSet {

  def apply(name: FabricResultSetName, plane: FeltCubePlane): FeltCubeResultSet =
    FeltCubeResultSetContext(resultName = name, plane = plane)

}

private[result] final case
class FeltCubeResultSetContext(resultName: FabricResultSetName, plane: FeltCubePlane)
  extends FeltCollectorResultSetContext[FeltCubeBuilder, FeltCubeCollector](resultName, plane)
    with FeltCubeResultSet {

  override def toString: FabricResultSetName = s"FeltCubeResultSet(resultName=$resultName)"

  //////////////////////////////////////////////////////////////////////
  // state
  //////////////////////////////////////////////////////////////////////

  private[this]
  val _rows = new mutable.ArrayBuffer[FeltCubeResultRow]

  //////////////////////////////////////////////////////////////////////
  // Accessors
  //////////////////////////////////////////////////////////////////////

  override
  val columnNames: Array[BrioRelationName] = plane.planeBuilder.fieldNames

  override
  def columnTypeNames: Array[BrioTypeName] = columnTypeKeys.map(BrioTypes.brioDataTypeNameFromKey)

  override
  def columnTypeKeys: Array[BrioTypeKey] = plane.planeBuilder.dimensionFieldTypes ++ plane.planeBuilder.aggregationFieldTypes

  override
  val dimensionCount: Int = plane.planeBuilder.dimensionCount

  override
  val aggregationCount: Int = plane.planeBuilder.aggregationCount

  override
  def rowSet: Array[FabricResultRow] = _rows.toArray

  //////////////////////////////////////////////////////////////////////
  // data access
  //////////////////////////////////////////////////////////////////////

  override
  def apply(rowNumber: Int): FeltCubeResultRow = _rows(rowNumber)

  override
  def apply[T <: BrioDataType : ClassTag](column: String): Array[T] = {
    val list = new Array[T](rowCount)
    val fieldKey = plane.planeBuilder.fieldKeyMap(column)
    for (i <- 0 until rowCount) {
      list(i) = rowSet(i)(fieldKey).bData.asInstanceOf[T]
    }
    list
  }

  //////////////////////////////////////////////////////////////////////
  // extraction
  //////////////////////////////////////////////////////////////////////

  override
  def extractRows: FeltCubeResultSet = {
    try {
      plane.planeCollector.extractRows(plane.planeBuilder, plane.planeCollector, plane.planeDictionary) foreach {
        row: FeltCubeRowData => _rows += FeltCubeResultRow(plane, row).extractCells
      }
      super.extractRows
      this
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

}
