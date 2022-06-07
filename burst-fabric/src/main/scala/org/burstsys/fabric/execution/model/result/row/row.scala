/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.model.result

package object row {

  sealed case class FabricResultCellType(code:Int)

  object FabricAggregationCell extends FabricResultCellType(1)

  object FabricDimensionCell extends FabricResultCellType(2)

  /**
   * Each row's data has an array of columns
   */
  type FeltCubeRowData = Array[FabricResultColumn]

  /**
   * Each cube's result consists of an array of rows of data
   */
  type FeltCubeResultData = Array[FeltCubeRowData]

}
