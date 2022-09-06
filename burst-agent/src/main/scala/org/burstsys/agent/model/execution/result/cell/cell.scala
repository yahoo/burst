/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.execution.result

import org.burstsys.agent.api.BurstQueryApiDatum._
import org.burstsys.agent.api.{BurstQueryApiResultCell, BurstQueryDataType}
import org.burstsys.agent.model.execution.result.cell.tipe._
import org.burstsys.brio.types.BrioTypes
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.fabric.execution.model.result.row._
import org.burstsys.vitals.errors.VitalsException

import scala.collection.mutable.ArrayBuffer
import scala.language.implicitConversions

package object cell {

  type AgentResultCell = BurstQueryApiResultCell.Proxy

  type AgentThriftResultCell = BurstQueryApiResultCell

  final case
  class AgentCellContext(_underlying_BurstQueryApiResultCell: BurstQueryApiResultCell)
    extends BurstQueryApiResultCell.Proxy

  def asBoolean(isNull: Boolean, value: Boolean, cellType: FabricResultCellType): AgentResultCell = {
    BurstQueryApiResultCell(BurstQueryDataType.BooleanType, isNull, isNan = false, BooleanData(value), cellType)
  }

  def asByte(isNull: Boolean, isNan: Boolean, value: Byte, cellType: FabricResultCellType): AgentResultCell = {
    BurstQueryApiResultCell(BurstQueryDataType.ByteType, isNull, isNan, ByteData(value), cellType)
  }

  def asShort(isNull: Boolean, isNan: Boolean, value: Short, cellType: FabricResultCellType): AgentResultCell = {
    BurstQueryApiResultCell(BurstQueryDataType.ShortType, isNull, isNan, ShortData(value), cellType)
  }

  def asInteger(isNull: Boolean, isNan: Boolean, value: Int, cellType: FabricResultCellType): AgentResultCell = {
    BurstQueryApiResultCell(BurstQueryDataType.IntegerType, isNull, isNan, IntegerData(value), cellType)
  }

  def asLong(isNull: Boolean, isNan: Boolean, value: Long, cellType: FabricResultCellType): AgentResultCell = {
    BurstQueryApiResultCell(BurstQueryDataType.LongType, isNull, isNan, LongData(value), cellType)
  }

  def asDouble(isNull: Boolean, isNan: Boolean, value: Double, cellType: FabricResultCellType): AgentResultCell = {
    BurstQueryApiResultCell(BurstQueryDataType.DoubleType, isNull, isNan, DoubleData(value), cellType)
  }

  def asString(isNull: Boolean, value: String, cellType: FabricResultCellType): AgentResultCell = {
    if (value == null) {
      BurstQueryApiResultCell(BurstQueryDataType.StringType, isNull = true, isNan = false, StringData(""), cellType)
    } else {
      BurstQueryApiResultCell(BurstQueryDataType.StringType, isNull, isNan = false, StringData(value), cellType)
    }
  }

  implicit def thriftToAgentResultCell(a: AgentThriftResultCell): AgentResultCell =
    AgentCellContext(a)

  implicit def agentToThriftResultCell(a: AgentResultCell): AgentThriftResultCell =
    BurstQueryApiResultCell(
      bType = a.bType,
      isNull = a.isNull,
      isNan = a.isNan,
      bData = a.bData,
      cellType = a.cellType
    )

  implicit
  def fabricToAgentResultCells(cells: Array[FabricResultCell]): Seq[AgentResultCell] = {
    val result = new ArrayBuffer[AgentResultCell]
    var i = 0
    while (i < cells.length) {
      val cell: FabricResultCell = cells(i)
      result += (cell.bType match {
        case BrioTypes.BrioBooleanKey => asBoolean(cell.isNull, cell.bData.asInstanceOf[Boolean], cell.cellType)
        case BrioTypes.BrioByteKey => asByte(cell.isNull, cell.isNan, cell.bData.asInstanceOf[Byte], cell.cellType)
        case BrioTypes.BrioShortKey => asShort(cell.isNull, cell.isNan, cell.bData.asInstanceOf[Short], cell.cellType)
        case BrioTypes.BrioIntegerKey => asInteger(cell.isNull, cell.isNan, cell.bData.asInstanceOf[Int], cell.cellType)
        case BrioTypes.BrioLongKey => asLong(cell.isNull, cell.isNan, cell.bData.asInstanceOf[Long], cell.cellType)
        case BrioTypes.BrioDoubleKey => asDouble(cell.isNull, cell.isNan, cell.bData.asInstanceOf[Double], cell.cellType)
        case BrioTypes.BrioStringKey => asString(cell.isNull, cell.bData.asInstanceOf[String], cell.cellType)
        case _ => ???
      })
      i += 1
    }
    result.toSeq
  }

  implicit def fabricToAgentResultCell(cell: FabricResultCell): AgentResultCell = {
    cell.bType match {
      case BrioTypes.BrioBooleanKey => asBoolean(cell.isNull, cell.bData.asInstanceOf[Boolean], cell.cellType)
      case BrioTypes.BrioByteKey => asByte(cell.isNull, cell.isNan, cell.bData.asInstanceOf[Byte], cell.cellType)
      case BrioTypes.BrioShortKey => asShort(cell.isNull, cell.isNan, cell.bData.asInstanceOf[Short], cell.cellType)
      case BrioTypes.BrioIntegerKey => asInteger(cell.isNull, cell.isNan, cell.bData.asInstanceOf[Int], cell.cellType)
      case BrioTypes.BrioLongKey => asLong(cell.isNull, cell.isNan, cell.bData.asInstanceOf[Long], cell.cellType)
      case BrioTypes.BrioDoubleKey => asDouble(cell.isNull, cell.isNan, cell.bData.asInstanceOf[Double], cell.cellType)
      case BrioTypes.BrioStringKey => asString(cell.isNull, cell.bData.asInstanceOf[String], cell.cellType)
      case _ => throw VitalsException(s"unknown cell type $cell")
    }
  }

  implicit def agentToFabricResultCell(cell: AgentResultCell): FabricResultCell = {
    cell.bType match {
      case BurstQueryDataType.BooleanType => FabricResultCell(
        cell.bData.asInstanceOf[BooleanData].booleanData,
        BrioBooleanKey, BrioBooleanName, cell.isNull, cell.isNan,
        cell.cellType
      )
      case BurstQueryDataType.ByteType => FabricResultCell(
        cell.bData.asInstanceOf[ByteData].byteData,
        BrioByteKey, BrioByteName, cell.isNull, cell.isNan,
        cell.cellType
      )
      case BurstQueryDataType.ShortType => FabricResultCell(
        cell.bData.asInstanceOf[ShortData].shortData,
        BrioShortKey, BrioShortName, cell.isNull, cell.isNan,
        cell.cellType
      )
      case BurstQueryDataType.IntegerType => FabricResultCell(
        cell.bData.asInstanceOf[IntegerData].integerData,
        BrioIntegerKey, BrioIntegerName, cell.isNull, cell.isNan,
        cell.cellType
      )
      case BurstQueryDataType.LongType => FabricResultCell(
        cell.bData.asInstanceOf[LongData].longData,
        BrioLongKey, BrioLongName, cell.isNull, cell.isNan,
        cell.cellType
      )
      case BurstQueryDataType.DoubleType => FabricResultCell(
        cell.bData.asInstanceOf[DoubleData].doubleData,
        BrioDoubleKey, BrioDoubleName, cell.isNull, cell.isNan,
        cell.cellType
      )
      case BurstQueryDataType.StringType => FabricResultCell(
        cell.bData.asInstanceOf[StringData].stringData,
        BrioStringKey, BrioStringName, cell.isNull, cell.isNan,
        cell.cellType
      )
      case _ => throw VitalsException(s"unknown cell type $cell")
    }
  }

}
