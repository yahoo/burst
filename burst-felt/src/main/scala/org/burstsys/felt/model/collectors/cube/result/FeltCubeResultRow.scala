/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.result

import org.burstsys.brio.types.BrioTypes._
import org.burstsys.fabric.execution.model.result.row.{FabricColumnCell, FabricResultCell, FabricResultRow, FeltCubeRowData}
import org.burstsys.felt.model.collectors.cube.plane.FeltCubePlane
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

trait FeltCubeResultRow extends FabricResultRow {

  /**
   *
   * @return
   */
  def extractCells: FeltCubeResultRow

}

/**
 * constructors
 */
object FeltCubeResultRow {

  def apply(cube: FeltCubePlane, row: FeltCubeRowData): FeltCubeResultRow =
    FeltCubeResultRowContext(cube, row)

}

private final case
class FeltCubeResultRowContext(cube: FeltCubePlane, row: FeltCubeRowData) extends FeltCubeResultRow {

  //////////////////////////////////////////////////////////////////////
  // state
  //////////////////////////////////////////////////////////////////////

  private[this]
  val _cells = new ArrayBuffer[FabricResultCell]

  //////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////

  override
  def cells: Array[FabricResultCell] = _cells.toArray

  override
  def apply(columnNumber: Int): FabricResultCell = cells(columnNumber)

  override
  def apply[C <: BrioDataType](relationName: BrioRelationName)(implicit t: ClassTag[C]): C = {
    val fieldKey = cube.planeBuilder.fieldKeyMap(relationName)
    cells(fieldKey).bData.asInstanceOf[C]
  }

  override
  def isNull(relationName: BrioRelationName): Boolean = {
    val fieldKey = cube.planeBuilder.fieldKeyMap(relationName)
    cells(fieldKey).isNull
  }

  override
  def isNaN(relationName: BrioRelationName): Boolean = {
    val fieldKey = cube.planeBuilder.fieldKeyMap(relationName)
    cells(fieldKey).isNan
  }

  override
  def extractCells: this.type = {
    try {
      row foreach (_cells += FabricColumnCell(_))
      this
    } catch safely {
      case t: Throwable =>
        val msg = burstStdMsg(t)
        log error msg
        throw new RuntimeException(msg, t)
    }
  }

  //////////////////////////////////////////////////////////////////////
  // JSON
  //////////////////////////////////////////////////////////////////////

  override def toJson: FabricResultRow = new FabricResultRow {
    override val cells: Array[FabricResultCell] = FeltCubeResultRowContext.this.cells.map(_.toJson)
  }

}
