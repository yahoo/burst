/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.result.row

import org.burstsys.brio.types.BrioTypes
import org.burstsys.brio.types.BrioTypes.{BrioDataType, BrioTypeKey, BrioTypeName}
import org.burstsys.vitals.json.VitalsJsonObject
import org.burstsys.vitals.json.VitalsJsonRepresentable

import scala.language.implicitConversions

/**
 * A single 'cell' or 'column' in a result row
 */
trait FabricResultCell extends VitalsJsonRepresentable[FabricResultCell] {

  /**
   * the brio data for this cell
   *
   * @return
   */
  def bData: BrioDataType

  /**
   * return this cell as a boolean
   *
   * @return
   */
  def asBoolean: Boolean = bData.asInstanceOf[Boolean]

  /**
   * return this cell as a byte
   *
   * @return
   */
  def asByte: Byte = bData.asInstanceOf[Byte]

  /**
   * return this cell as a short
   *
   * @return
   */
  def asShort: Short = bData.asInstanceOf[Short]

  /**
   * return this cell as an integer
   *
   * @return
   */
  def asInteger: Int = bData.asInstanceOf[Int]

  /**
   * return this cell as a long
   *
   * @return
   */
  def asLong: Long = bData.asInstanceOf[Long]

  /**
   * return this cell as a double
   *
   * @return
   */
  def asDouble: Double = bData.asInstanceOf[Double]

  /**
   * return this cell as a string
   *
   * @return
   */
  def asString: String = bData.asInstanceOf[String]

  /**
   * the brio datatype key for this cell
   *
   * @return
   */
  def bType: BrioTypeKey

  /**
   * the brio datatype name for this cell
   *
   * @return
   */
  def bName: BrioTypeName

  /**
   * is this cell null?
   *
   * @return
   */
  def isNull: Boolean

  /**
   * is this cell a NAN?
   *
   * @return
   */
  def isNan: Boolean

  /**
   * what sort of cell is this?
   *
   * @return
   */
  def cellType: FabricResultCellType

}

object FabricResultCell {

  def apply(
             bData: BrioDataType,
             bType: BrioTypeKey,
             bName: BrioTypeName,
             isNull: Boolean = false,
             isNan: Boolean = false,
             cellType: FabricResultCellType
           ): FabricResultCell = {

    // unit test simplicity hack
    if (bType == BrioTypes.BrioStringKey && bData == null)
      FabricResultCellContext(bData = "", bType, bName, isNull = true, isNan, cellType)
    else
      FabricResultCellContext(bData, bType, bName, isNull, isNan, cellType)
  }

  final
  def asBoolean(isNull: Boolean, value: Boolean, cellType: FabricResultCellType): FabricResultCell = {
    FabricResultCell(
      bData = value, bType = BrioTypes.BrioBooleanKey, bName = BrioTypes.BrioBooleanName, isNull, cellType = cellType
    )
  }

  final
  def asByte(isNull: Boolean, isNan: Boolean, value: Byte, cellType: FabricResultCellType): FabricResultCell = {
    FabricResultCell(
      bData = value, bType = BrioTypes.BrioByteKey, bName = BrioTypes.BrioByteName, isNull, isNan = isNan, cellType = cellType
    )
  }

  final
  def asShort(isNull: Boolean, isNan: Boolean, value: Short, cellType: FabricResultCellType): FabricResultCell = {
    FabricResultCell(
      value, BrioTypes.BrioShortKey, BrioTypes.BrioShortName, isNull, isNan, cellType
    )
  }

  final
  def asInteger(isNull: Boolean, isNan: Boolean, value: Int, cellType: FabricResultCellType): FabricResultCell = {
    FabricResultCell(
      value, BrioTypes.BrioIntegerKey, BrioTypes.BrioIntegerName, isNull, isNan, cellType
    )
  }

  final
  def asLong(isNull: Boolean, isNan: Boolean, value: Long, cellType: FabricResultCellType): FabricResultCell = {
    FabricResultCell(
      value, BrioTypes.BrioLongKey, BrioTypes.BrioLongName, isNull, isNan, cellType
    )
  }

  final
  def asDouble(isNull: Boolean, isNan: Boolean, value: Double, cellType: FabricResultCellType): FabricResultCell = {
    FabricResultCell(
      value, BrioTypes.BrioDoubleKey, BrioTypes.BrioDoubleName, isNull, isNan, cellType
    )
  }

  final
  def asString(isNull: Boolean, value: String, cellType: FabricResultCellType): FabricResultCell = {
    if (value == null) {
      FabricResultCell(
        bData = "", bType = BrioTypes.BrioStringKey, bName = BrioTypes.BrioStringName, isNull = true, cellType = cellType
      )
    } else {
      FabricResultCell(
        value, BrioTypes.BrioStringKey, BrioTypes.BrioStringName, cellType = cellType
      )
    }
  }

}

/**
 * reference implementation - also for JSON
 *
 * @param bData
 * @param bType
 * @param bName
 * @param isNull
 * @param isNan
 */
private final case
class FabricResultCellContext(
                               bData: BrioDataType,
                               bType: BrioTypeKey,
                               bName: BrioTypeName,
                               isNull: Boolean,
                               isNan: Boolean,
                               cellType: FabricResultCellType
                             ) extends FabricResultCell with VitalsJsonObject
