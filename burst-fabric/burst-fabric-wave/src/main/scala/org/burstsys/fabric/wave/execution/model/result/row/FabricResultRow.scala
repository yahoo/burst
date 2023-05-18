/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.result.row

import org.burstsys.brio.types.BrioTypes.{BrioDataType, BrioRelationName, BrioRelationOrdinal}
import org.burstsys.vitals.json.VitalsJsonObject
import org.burstsys.vitals.json.VitalsJsonRepresentable

import scala.language.implicitConversions
import scala.reflect.ClassTag

/**
 * a row in a result set
 */
trait FabricResultRow extends VitalsJsonRepresentable[FabricResultRow] {

  /**
   * the cells (columns) in a result set
   *
   * @return
   */
  def cells: Array[FabricResultCell]

  /**
   * get data from the row based on column (relation) ordinal (key)
   *
   * @param relationOrdinal
   * @return
   */
  def apply(relationOrdinal: BrioRelationOrdinal): FabricResultCell = cells(relationOrdinal)

  /**
   * get data from the row based on column (relation) name
   * '''NOTE:''' this is not performant, use only for unit tests
   *
   * @param relationName
   * @param t
   * @tparam C
   * @return
   */
  def apply[C <: BrioDataType](relationName: BrioRelationName)(implicit t: ClassTag[C]): C = ???

}

object FabricResultRow {

  def apply(cells: Array[FabricResultCell]): FabricResultRow = FabricResultRowContext(cells: Array[FabricResultCell])

}

/**
 * reference implementation - also for JSON
 *
 * @param cells
 */
private final case
class FabricResultRowContext(cells: Array[FabricResultCell]) extends FabricResultRow with VitalsJsonObject {
  override def toJson: FabricResultRow = FabricResultRowContext(cells.map(_.toJson))
}

