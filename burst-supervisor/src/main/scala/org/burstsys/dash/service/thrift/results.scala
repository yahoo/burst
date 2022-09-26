/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.service.thrift

import org.burstsys.brio.types.BrioTypes
import org.burstsys.brio.types.BrioTypes.BrioDataType
import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row
import org.burstsys.fabric.execution.model.result.row.FabricResultCell
import org.burstsys.fabric.execution.model.result.row.FabricResultCellType
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.fabric.execution.model.result.set.FabricResultSet
import org.burstsys.fabric.execution.model.result.set.FabricResultSetIndex
import org.burstsys.fabric.execution.model.result.set.FabricResultSetMetrics
import org.burstsys.gen.thrift.api.client.BTDataType
import org.burstsys.gen.thrift.api.client.BTDatum
import org.burstsys.gen.thrift.api.client.query.BTCell
import org.burstsys.gen.thrift.api.client.query.BTCellType
import org.burstsys.gen.thrift.api.client.query.BTResult
import org.burstsys.gen.thrift.api.client.query.BTResultSet
import org.burstsys.gen.thrift.api.client.query.BTResultSetMeta
import org.burstsys.gen.thrift.api.client.query.BTViewGeneration

import java.util
import scala.jdk.CollectionConverters._

object results {

  def toThrift(domainUdk: String, viewUdk: String, result: FabricResultGroup): BTResult = {
    val guid = result.groupKey.groupUid
    val message = result.resultMessage
    val generation = new BTViewGeneration(domainUdk, viewUdk, result.groupMetrics.generationKey.generationClock)
    val (generationMetrics, executionMetrics) = metrics.toThrift(result.groupMetrics)
    val resultSets = result.resultSets.map(toResultSetEntry).asJava
    new BTResult(guid, message, generation, generationMetrics, executionMetrics, resultSets)
  }

  def toResultSetEntry(entry: (FabricResultSetIndex, FabricResultSet)): (String, BTResultSet) = {
    val resultSet = entry._2
    resultSet.resultName -> toThriftResultSet(resultSet)
  }

  def toThriftResultSet(resultSet: FabricResultSet): BTResultSet = {
    new BTResultSet(
      resultSet.resultName,
      toThriftResultSetMeta(resultSet.metrics),
      resultSet.columnNames.toList.asJava,
      resultSet.columnTypeKeys.map(toThriftDataType).toList.asJava,
      resultSet.rowSet.map(toThriftRow).toList.asJava
    )
  }

  def toThriftResultSetMeta(meta: FabricResultSetMetrics): BTResultSetMeta = {
    new BTResultSetMeta(meta.succeeded, meta.limited, meta.overflowed, meta.rowCount, meta.properties.asJava)
  }

  def toThriftDataType(t: BrioDataType): BTDataType = t match {
    case BrioTypes.BrioBooleanKey => BTDataType.BoolType
    case BrioTypes.BrioByteKey => BTDataType.ByteType
    case BrioTypes.BrioShortKey => BTDataType.ShortType
    case BrioTypes.BrioIntegerKey => BTDataType.IntType
    case BrioTypes.BrioLongKey => BTDataType.LongType
    case BrioTypes.BrioDoubleKey => BTDataType.DoubleType
    case BrioTypes.BrioStringKey => BTDataType.StringType
    case unknown => bailWith(s"Unknown brio type: $unknown")
  }

  def toThriftRow(row: FabricResultRow): util.List[BTCell] = {
    row.cells.map(toThriftCell).toList.asJava
  }

  def toThriftCell(cell: FabricResultCell): BTCell = {
    val (cType, dType, datum) = cell.bType match {
      case BrioTypes.BrioBooleanKey =>
        (toThriftCellType(cell.cellType), toThriftDataType(cell.bType), toThriftDatum(cell.asBoolean))
      case BrioTypes.BrioByteKey =>
        (toThriftCellType(cell.cellType), toThriftDataType(cell.bType), toThriftDatum(cell.asByte))
      case BrioTypes.BrioShortKey =>
        (toThriftCellType(cell.cellType), toThriftDataType(cell.bType), toThriftDatum(cell.asShort))
      case BrioTypes.BrioIntegerKey =>
        (toThriftCellType(cell.cellType), toThriftDataType(cell.bType), toThriftDatum(cell.asInteger))
      case BrioTypes.BrioLongKey =>
        (toThriftCellType(cell.cellType), toThriftDataType(cell.bType), toThriftDatum(cell.asLong))
      case BrioTypes.BrioDoubleKey =>
        (toThriftCellType(cell.cellType), toThriftDataType(cell.bType), toThriftDatum(cell.asDouble))
      case BrioTypes.BrioStringKey =>
        (toThriftCellType(cell.cellType), toThriftDataType(cell.bType), toThriftDatum(cell.asString))
      case unknown => bailWith(s"Unknown brio type: $unknown")
    }
    new BTCell(cType, dType, datum, cell.isNull, cell.isNan)
  }

  def toThriftCellType(t: FabricResultCellType): BTCellType = t match {
    case row.FabricAggregationCell => BTCellType.Aggregation
    case row.FabricDimensionCell => BTCellType.Aggregation
    case unknown => bailWith(s"Unknown cell type $unknown")
  }

  def toThriftDatum(value: Any): BTDatum = value match {
    case b: Boolean => BTDatum.boolVal(b)
    case b: Byte => BTDatum.byteVal(b)
    case s: Short => BTDatum.shortVal(s)
    case i: Integer => BTDatum.intVal(i)
    case l: Long => BTDatum.longVal(l)
    case d: Double => BTDatum.doubleVal(d)
    case s: String => BTDatum.stringVal(s)
    case other => bailWith(s"Unknown thrift datum type ${other.getClass.getSimpleName}=$other")
  }


}
