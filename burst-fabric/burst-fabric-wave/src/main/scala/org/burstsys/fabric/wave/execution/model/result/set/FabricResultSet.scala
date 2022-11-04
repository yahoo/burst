/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.result.set

import org.burstsys.brio.types.BrioTypes.BrioDataType
import org.burstsys.brio.types.BrioTypes.BrioTypeKey
import org.burstsys.brio.types.BrioTypes.BrioTypeName
import org.burstsys.fabric.wave.execution.model.result.row.FabricResultRow
import org.burstsys.vitals.json.VitalsJsonObject
import org.burstsys.vitals.json.VitalsJsonRepresentable

import scala.language.implicitConversions
import scala.reflect.ClassTag

/**
 * the results associated with one of the queries in an execution group
 */
trait FabricResultSet extends VitalsJsonRepresentable[FabricResultSet] {


  /**
   * the index for this result set with the result group
   *
   * @return
   */
  def resultIndex: FabricResultSetIndex

  /**
   * the name for this result set with the result group
   *
   * @return
   */
  def resultName: FabricResultSetName

  /**
   * the metrics associated with this result set
   *
   * @return
   */
  def metrics: FabricResultSetMetrics

  /**
   * the full set of column names
   *
   * @return
   */
  def columnNames: Array[String]

  /**
   * a map of column names to column index
   *
   * @return
   */
  final def columnNamesMap: Map[String, FabricResultSetIndex] = columnNames.zipWithIndex.toMap

  /**
   * the full set of column brio type names
   *
   * @return
   */
  def columnTypeNames: Array[BrioTypeName]

  /**
   * the full set of column brio type keys
   *
   * @return
   */
  def columnTypeKeys: Array[BrioTypeKey]

  /**
   * the number of dimension columns
   *
   * @return
   */
  def dimensionCount: Int

  /**
   * the number of aggregation columsn
   *
   * @return
   */
  def aggregationCount: Int

  /**
   * the number of rows in this result set
   *
   * @return
   */
  def rowCount: Int

  /**
   * the rows in this result set
   *
   * @return
   */
  def rowSet: Array[FabricResultRow]

  /**
   * Return a row by row order index
   *
   * @param rowNumber
   * @return
   */
  def apply(rowNumber: Int): FabricResultRow = ???

  /**
   * return an array that contains all the cells in a column, i.e. all across all rows.
   *
   * @param column
   * @param t
   * @tparam C
   * @return
   */
  def apply[T <: BrioDataType : ClassTag](column: String): Array[T] = Array.empty

}

object FabricResultSet {

  def apply(
             resultIndex: FabricResultSetIndex = 0,
             resultName: FabricResultSetName = "",
             metrics: FabricResultSetMetrics = FabricResultSetMetrics(),
             dimensionCount: Int = 0,
             aggregationCount: Int = 0,
             columnNames: Array[String] = Array.empty,
             columnTypeNames: Array[String] = Array.empty,
             columnTypeKeys: Array[BrioTypeKey] = Array.empty,
             rowCount: Int = 0,
             rowSet: Array[FabricResultRow] = Array.empty
           ): FabricResultSet =
    FabricResultSetContext(
      resultIndex,
      resultName,
      metrics,
      dimensionCount,
      aggregationCount,
      columnNames,
      columnTypeNames,
      columnTypeKeys,
      rowCount,
      rowSet
    )

}

private final case
class FabricResultSetContext(
                              resultIndex: FabricResultSetIndex,
                              resultName: FabricResultSetName,
                              metrics: FabricResultSetMetrics,
                              dimensionCount: Int,
                              aggregationCount: Int,
                              columnNames: Array[String],
                              columnTypeNames: Array[String],
                              columnTypeKeys: Array[BrioTypeKey],
                              rowCount: Int,
                              rowSet: Array[FabricResultRow]
                            ) extends FabricResultSet with VitalsJsonObject {
  override def toJson: FabricResultSet =
    FabricResultSetContext(resultIndex, resultName, metrics.toJson, dimensionCount, aggregationCount,
      columnNames, columnTypeNames, columnTypeKeys, rowCount, rowSet.map(_.toJson))
}

