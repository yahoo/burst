/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.result.row

import org.burstsys.brio.types.BrioTypes
import org.burstsys.brio.types.BrioTypes._

/**
 * constructors
 */
object FabricColumnCell {

  def apply(cell: FabricResultColumn): FabricResultCell = {

    // hack for unit test simplicity null strings become empty strings with isNull set
    val c = if (cell.bType == BrioTypes.BrioStringKey && cell.value == null) {
      cell.copy(isNull = true, value = "")
    } else if (cell.cellType == FabricAggregationCell && cell.isNull) {
      // TODO this is a hack for aggregation nulls which now travel all the way through to output - we stop here until we are ready
      cell.copy(isNull = false, value = BrioTypes.defaultValueForBrioTypeKey(cell.bType))
    } else cell

    FabricColumnCellContext(c)
  }

}

private final case
class FabricColumnCellContext(column: FabricResultColumn) extends FabricResultCell {

  override def bType: BrioTypeKey = column.bType

  override def bName: BrioTypeName = BrioTypes.brioDataTypeNameFromKey(bType)

  override def isNull: Boolean = column.isNull

  override def isNan: Boolean = column.isNan

  override def bData: BrioDataType = column.value

  override def cellType: FabricResultCellType = column.cellType

  override def toJson: FabricResultCell = new FabricResultCell {
    override val bData: BrioDataType = column.value

    override val bType: BrioTypeKey = column.bType

    override val bName: BrioTypeName = BrioTypes.brioDataTypeNameFromKey(column.bType)

    override val isNull: Boolean = column.isNull

    override val isNan: Boolean = column.isNan

    override val cellType: FabricResultCellType = column.cellType
  }
  
  override def toString: String = if (column == null) "NULL" else column.toString

}
